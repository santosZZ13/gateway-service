package org.gateway.config;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gateway.filter.AuthenticationFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
@AllArgsConstructor
@Log4j2
public class SpringCloudConfig {

	private final AuthenticationFilter authenticationFilter;

	@Bean
	public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(r -> r.path("/api/auth/**")
						.filters(f -> f.filter(authenticationFilter)
								.requestRateLimiter(c -> c
										.setRateLimiter(redisRateLimiter())
										.setKeyResolver(keyResolver()))
						)
						.uri("http://localhost:8081/")
						.id("gateway-service")
				)
				.route(r -> r.path("/api/test/**")
						.filters(f -> f.filter(authenticationFilter))
						.uri("http://localhost:8081/")
						.id("test-service")
				)
				.build();
	}


	@Bean
	public RedisRateLimiter redisRateLimiter() {
		return new RedisRateLimiter(10, 20);
	}

	@Bean
	public KeyResolver keyResolver() {
		return exchange -> {
			log.info("KeyResolver: {}", exchange.getRequest().getRemoteAddress());
			return Mono.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getHostName());
		};
	}
}
