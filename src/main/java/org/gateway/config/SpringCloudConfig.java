package org.gateway.config;

import lombok.AllArgsConstructor;
import org.gateway.filter.CustomFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
@AllArgsConstructor
public class SpringCloudConfig {

	private final CustomFilter customFilter;

	@Bean
	public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(r -> r.path("/api/auth/**")
						.filters(f -> f.filter(customFilter.apply(new CustomFilter.Config())))
						.uri("http://localhost:8081/")
						.id("gateway-service")
				)
				.build();
	}


	@Bean
	public GlobalFilter globalFilter() {
		return (exchange, chain) -> {
			System.out.println("First Global filter");
			return chain.filter(exchange).then(Mono.fromRunnable(() -> {
				System.out.println("Second Global filter");
			}));
		};
	}
}
