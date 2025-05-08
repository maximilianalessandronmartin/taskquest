package org.novize.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                .simpDestMatchers("/app/**").authenticated()
                .simpSubscribeDestMatchers("/user/**").authenticated()
                .anyMessage().permitAll();
    }



    @Override
    protected boolean sameOriginDisabled() {
        return true; // Für Entwicklung, in Produktion auf false setzen
    }
}
