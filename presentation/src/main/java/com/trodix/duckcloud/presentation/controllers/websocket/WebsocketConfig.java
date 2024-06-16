package com.trodix.duckcloud.presentation.controllers.websocket;

import com.trodix.duckcloud.security.converters.KeycloakJwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.StringTokenizer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
@Slf4j
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.auth.allowed-origins}")
    private String[] allowedOrigins;

    private final KeycloakJwtAuthenticationConverter authenticationConverter;

    private final JwtDecoder jwtDecoder;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // config.setApplicationDestinationPrefixes("/app");
        config.enableSimpleBroker("/topic", "/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authToken = accessor.getFirstNativeHeader("Authorization");
                    if (authToken != null && authToken.startsWith("Bearer ")) {
                        authToken = authToken.substring(7);
                        try {
                            Principal user = authenticateToken(authToken);
                            if (user != null) {
                                accessor.setUser(user);
                                JwtAuthenticationToken auth = (JwtAuthenticationToken) user;
                                log.info("Utilisateur connecté via websocket: {} - {}", user.getName(), auth.getToken().getClaim("preferred_username").toString());
                            } else {
                                log.error("Une erreur est survenue lors de la convertion du token. Principal est null");
                                publishDisconnectEvent(message, accessor.getSessionId(), CloseStatus.BAD_DATA);
                            }
                        } catch (JwtException e) {
                            log.error("Une erreur est survenue lors de la convertion du token", e);
                            publishDisconnectEvent(message, accessor.getSessionId(), CloseStatus.BAD_DATA);
                        }

                    } else {
                        log.info("Bearer token absent ou non valide");
                    }
                } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination();
                    JwtAuthenticationToken auth = (JwtAuthenticationToken) accessor.getUser();
                    if (!checkPrivateUserChannelAccess(auth, destination)) {
                        log.error("Utilisateur {} non autorisé à accéder à la destination {}", auth.getName(), destination);
                        publishDisconnectEvent(message, accessor.getSessionId(), CloseStatus.BAD_DATA);
                    } else {
                        log.debug("Utilisateur {} autorisé à accéder à la destination {}", auth.getName(), destination);
                    }
                }
                return message;
            }
        });
    }

    private void publishDisconnectEvent(Message<?> message, String sessionId, CloseStatus closeStatus) {
        SessionDisconnectEvent event = new SessionDisconnectEvent(
                this,
                MessageBuilder.withPayload(message.getPayload().toString().getBytes()).build(),
                sessionId,
                closeStatus
        );
        eventPublisher.publishEvent(event);
    }

    private Principal authenticateToken(String authToken) {
        Jwt jwt = jwtDecoder.decode(authToken);
        return authenticationConverter.convert(jwt);
    }

    private boolean checkPrivateUserChannelAccess(JwtAuthenticationToken jwt, String destination) {
        // /user/{userId|username}/{topic}
        StringTokenizer tokenizer = new StringTokenizer(destination, "/");
        if (tokenizer.nextToken().equals("user")) {
            String username = tokenizer.nextToken();
            return username.equals(jwt.getName()) || username.equals(jwt.getToken().getClaim("preferred_username").toString());
        }

        return true;
    }

}
