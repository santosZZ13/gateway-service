package org.gateway.filter;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.gateway.util.JwtUtil;
import org.gateway.util.RouterValidator;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@RefreshScope
@Component
@AllArgsConstructor
public class AuthenticationFilter implements GatewayFilter {

	private final RouterValidator routerValidator;
	private final JwtUtil jwtUtil;

	@SneakyThrows
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();

		if (routerValidator.isSecured.test(request)) {
			if (this.isAuthMissing(request)) {
				return this.onError(exchange, HttpStatus.UNAUTHORIZED);
			}

			final String token = this.getAuthHeader(request);


			Claims allClaimsFromToken = this.jwtUtil.getAllClaimsFromToken(token);

			if (jwtUtil.isInvalid(token)) {
				return this.onError(exchange, HttpStatus.FORBIDDEN);
			}

//			this.updateRequest(exchange, allClaimsFromToken);
		}

		return chain.filter(exchange);

	}

	/**
	 * This method is in charge of updating the request with the information of the token.
	 * Refer to <a href="https://medium.com/@rajithgama/spring-cloud-gateway-security-with-jwt-23045ba59b8a">...</a>
	 *
	 * @param request
	 * @return
	 */
//	private void updateRequest(ServerWebExchange exchange, Claims allClaimsFromToken) {
//		exchange.getRequest().mutate()
//				.header("email", String.valueOf(allClaimsFromToken.get("email")))
//				.build();
//	}
	private String getAuthHeader(ServerHttpRequest request) {
		return Objects.requireNonNull(request.getHeaders().get("Authorization")).get(0)
				.substring(7);
	}


	private boolean isAuthMissing(ServerHttpRequest request) {
		return !request.getHeaders().containsKey("Authorization");
	}

	private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);
		return response.setComplete();
	}
}
