package org.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

	@Override
	public GatewayFilter apply(Config config) {
		//Custom Pre-Filter. Suppose we can extract JWT and perform Authentication
		return (exchange, chain) -> {
			//Custom Post-Filter.Suppose we can call error response handler based on error code.
			// from exachange object, i want to get the username and password

			ServerHttpRequest request = exchange.getRequest();

			Flux<DataBuffer> body = request.getBody();

			// print request body
			body.subscribe(buffer -> {
				byte[] bytes = new byte[buffer.readableByteCount()];
				buffer.read(bytes);
				DataBufferUtils.release(buffer);
				String bodyString = new String(bytes, StandardCharsets.UTF_8);
				System.out.println(bodyString);
			});

			System.out.println(request);

			return chain.filter(exchange).then(Mono.fromRunnable(() -> {
//				System.out.println("Response status code: " + exchange.getResponse().getStatusCode());
//				System.out.println("Response headers: " + exchange.getResponse().getHeaders());
				ServerHttpResponse response = exchange.getResponse();
			}));
		};
	}

	public static class Config {
		// Put the configuration properties
	}
}
