package org.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

	@Override
	public GatewayFilter apply(Config config) {
		//Custom Pre-Filter. Suppose we can extract JWT and perform Authentication
		return (exchange, chain) -> {
			//Custom Post-Filter.Suppose we can call error response handler based on error code.
			return chain.filter(exchange).then(Mono.fromRunnable(() -> {
				System.out.println("Response status code: " + exchange.getResponse().getStatusCode());
				System.out.println("Response headers: " + exchange.getResponse().getHeaders());

			}));
		};
	}

	public static class Config {
		// Put the configuration properties
	}
}
