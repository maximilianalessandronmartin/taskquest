package org.novize.api.config;

import jakarta.servlet.http.HttpServletRequest;
import org.novize.api.exceptions.UserNotFoundException;
import org.novize.api.repository.UserRepository;
import org.novize.api.services.JwtService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@Component
public class JwtWebSocketHandshakeHandler extends DefaultHandshakeHandler {
    private final JwtService jwtService;
    private final UserRepository userRepository; // Annahme, dass Sie einen solchen Repository haben

    public JwtWebSocketHandshakeHandler(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        String token = extractTokenFromRequest(servletRequest);

        // Prüfe, ob Token null oder leer ist, bevor jwtService.extractUsername aufgerufen wird
        if (token == null || token.isEmpty()) {
            return null;
        }

        String username = jwtService.extractUsername(token);

        // Prüfe, ob Username extrahiert werden konnte
        if (username == null || username.isEmpty()) {
            return null;
        }

        var userdetails = userRepository.findByEmail(username).orElse(null);
        if (userdetails == null) {
            throw new UserNotFoundException("User Not Found");
        }

        if (jwtService.isTokenValid(token, userdetails)) {
            return new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        }

        return null;
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        // Extrahieren Sie den Token aus den Query-Parametern oder Headers
        String token = request.getParameter("token");
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        return token;
    }
}