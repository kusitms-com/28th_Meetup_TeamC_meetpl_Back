package com.kusitms.socketservice.global.config;

import com.kusitms.socketservice.global.common.AuthenticationInterceptor;
import com.kusitms.socketservice.global.config.auth.UserIdArgumentResolver;
import com.kusitms.socketservice.global.error.handler.MessageErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
@EnableWebSocketMessageBroker
@Configuration
public class SocketConfig implements WebSocketMessageBrokerConfigurer {
    private final AuthenticationInterceptor authenticationInterceptor;
    private final MessageErrorHandler messageErrorHandler;
    private final UserIdArgumentResolver userIdArgumentResolver;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*");
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
        registry.setErrorHandler(messageErrorHandler);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authenticationInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(userIdArgumentResolver);
    }
}
