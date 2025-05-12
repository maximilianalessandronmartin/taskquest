package org.novize.api.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.novize.api.repository.UserRepository;
import org.novize.api.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LogManager.getLogger(WebSocketChannelInterceptor.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            // Log alle STOMP-Befehle
            logger.debug("STOMP {} received", accessor.getCommand());

            // Detaillierte Logs je nach Befehl
            switch (accessor.getCommand()) {
                case CONNECT:
                    List<String> authorization = accessor.getNativeHeader("Authorization");
                    if (authorization != null && !authorization.isEmpty()) {
                        String token = authorization.get(0).replace("Bearer ", "");
                        String username = jwtService.extractUsername(token);

                        if (username != null) {
                            userRepository.findByEmail(username).ifPresent(user -> {
                                accessor.setUser(new UsernamePasswordAuthenticationToken(
                                        user, null, user.getAuthorities()
                                ));
                                logger.debug("User authenticated via WebSocket: {}", username);
                            });
                        }
                    }
                    break;
                case SUBSCRIBE:
                    logger.debug("User subscribed to: {}", accessor.getDestination());
                    break;
                case SEND:
                    // Payload extrahieren und loggen (vorsichtig mit großen Payloads)
                    Object payload = message.getPayload();
                    if (payload instanceof byte[]) {
                        try {
                            String payloadStr = new String((byte[]) payload, StandardCharsets.UTF_8);
                            logger.debug("Message sent to {}: {}", accessor.getDestination(),
                                    payloadStr.length() > 100 ? payloadStr.substring(0, 100) + "..." : payloadStr);
                        } catch (Exception e) {
                            logger.debug("Message sent to {} (binary content)", accessor.getDestination());
                        }
                    } else {
                        logger.debug("Message sent to {}: {}", accessor.getDestination(), payload);
                    }
                    break;
                case DISCONNECT:
                    logger.debug("User disconnected: {}", accessor.getUser());
                    break;
                default:
                    break;
            }
        }
        return message;
    }
}