package org.gateway.config;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gateway.filter.AuthenticationFilter;
import org.gateway.filter.RateLimiterHandlerFilterFunction;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@AllArgsConstructor
@Log4j2
public class SpringCloudConfig {

	private final AuthenticationFilter authenticationFilter;
	private final RateLimiterHandlerFilterFunction rateLimiterHandlerFilterFunction;
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
	RouterFunction<ServerResponse> routerFunction() {
		return route().GET("/api/gateway", serverRequest ->
						ServerResponse.ok()
								.contentType(org.springframework.http.MediaType.TEXT_PLAIN)
								.body(Mono.just("Hello from Gateway"), String.class)
				)
				.filter(rateLimiterHandlerFilterFunction)
				.build();
	}

	@Bean
	public RedisRateLimiter redisRateLimiter() {
		return new RedisRateLimiter(1, 5);
	}

	@Bean
	public KeyResolver keyResolver() {
		return exchange -> {
			log.info("KeyResolver: {}", exchange.getRequest().getRemoteAddress());
			return Mono.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getHostName());
		};
	}

	/**
	 * By default, the RequestRateLimiterGatewayFilterFactory will return a 429 status code when the rate limit is exceeded.
	 * We can customize the response by creating a bean of GatewayFilterFactory<RequestRateLimiterGatewayFilterFactory.Config> type.
	 * We can override the denyResponse method to customize the response.
	 * In this example, we are returning a JSON response with a 429 status code and a message.
	 *
	 * @Reference: <a href="https://www.linkedin.com/advice/0/how-do-you-implement-rate-limiting-spring-boot">...</a>
	 * @Reference: <a href="https://stackoverflow.com/questions/44042412/how-to-set-rate-limit-for-each-user-in-spring-boot">...</a>
	 * @Keywords: Spring Cloud Gateway, Rate Limiting, Rate Limiter,
	 * Rate Limit, Rate Limit Exceeded, 429, Too Many Requests, How to set rate limit for each user in Spring Boot
	 */
	@Bean
	public GatewayFilterFactory<RequestRateLimiterGatewayFilterFactory.Config> rateLimiterFilter() {
//		return new RequestRateLimiterGatewayFilterFactory() {
//			@Override
//			public Mono<Void> denyResponse(ServerWebExchange exchange, String key) {
//				ServerHttpResponse response = exchange.getResponse();
//				response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
//				response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
//				DataBuffer buffer = response.bufferFactory().wrap("{\"error\": \"RATE_LIMIT_EXCEEDED\", \"message\": \"Too many requests. Please try again later.\"}".getBytes());
//				return response.writeWith(Mono.just(buffer));
//			}
//		};
		return new RequestRateLimiterGatewayFilterFactory(redisRateLimiter(), keyResolver());
	}
}
