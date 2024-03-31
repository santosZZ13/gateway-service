package org.gateway.filter;

import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * This is a global filter that will be applied to all routes.
 * The order of the filter is set to the highest precedence. Which means it will be executed first.
 * <a href="https://www.springcloud.io/post/2022-03/record-request-and-response-bodies/#gsc.tab=0">...</a>
 */
@Component
@Log4j2
public class GlobalFilterImpl implements GlobalFilter, Ordered {
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		//  Maybe save the request to the database
		return chain.filter(exchange).then(Mono.fromRunnable(() -> {
			// Maybe save the response to the database
			log.info("Post Filter Logic: Response code: {}", exchange.getResponse().getStatusCode());
		}));

//		return getBodyFromRequest(exchange.getRequest())
//				.flatMap(body -> {
//					String bd = body;
//					log.info("Request body: {}", bd);
//					return chain.filter(exchange);
//				})
//				.then(Mono.fromRunnable(
//						() -> log.info("Post Filter Logic: Response code: {}", exchange.getResponse().getStatusCode())
//				));
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}


	private Mono<String> getBodyFromRequest(ServerHttpRequest request) {
		return DataBufferUtils.join(request.getBody())
				.flatMap(dataBuffer -> {
					byte[] bytes = new byte[dataBuffer.readableByteCount()];
					dataBuffer.read(bytes);
					DataBufferUtils.release(dataBuffer);
					return Mono.just(new String(bytes, StandardCharsets.UTF_8));
				})
				.onErrorMap(throwable -> {
					log.error("Error in reading request body", throwable);
					return throwable;
				});
	}
}
