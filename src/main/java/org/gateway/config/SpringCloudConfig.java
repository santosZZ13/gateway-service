package org.gateway.config;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gateway.filter.AuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
@Log4j2
public class SpringCloudConfig {

	private final AuthenticationFilter authenticationFilter;

	@Bean
	public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(r -> r.path("/api/auth/**")
						.filters(f -> f.filter(authenticationFilter))
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

}
