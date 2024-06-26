package org.gateway.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {
	public static final List<String> openApiEndpoints = List.of(
			"/api/auth/login", "/api/auth/register"
	);

	public Predicate<ServerHttpRequest> isSecured = request -> openApiEndpoints
			.stream()
			.noneMatch(uri -> request.getURI().getPath().contains(uri));
}
