package org.gateway.filter;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisCallback;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


@Component
@AllArgsConstructor
public class RateLimiterHandlerFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {

	private final ReactiveRedisTemplate<String, Long> redisTemplate;

//	@Value("${MAX_REQUESTS_PER_MINUTE}")
	private static Long MAX_REQUESTS_PER_MINUTE = 20L;

	@Override
	public HandlerFilterFunction<ServerResponse, ServerResponse> andThen(HandlerFilterFunction<ServerResponse, ServerResponse> after) {
		return HandlerFilterFunction.super.andThen(after);
	}

	@Override
	public HandlerFunction<ServerResponse> apply(HandlerFunction<ServerResponse> handler) {
		return HandlerFilterFunction.super.apply(handler);
	}

	@Override
	public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {

		int currentMinute = LocalTime.now().getMinute();
		String key = String.format("rl_%s:%s", requestAddress(request.remoteAddress()), currentMinute);

		return	redisTemplate.opsForValue().get(key)
				.flatMap(value -> value >= MAX_REQUESTS_PER_MINUTE ? ServerResponse.status(429).build() : incrAndExpireKey(key, request, next))
				.switchIfEmpty(incrAndExpireKey(key, request, next));
	}


	private Mono<ServerResponse> incrAndExpireKey(String key, ServerRequest request,
												  HandlerFunction<ServerResponse> next) {
		return redisTemplate.execute(new ReactiveRedisCallback<List<Object>>() {
					@Override
					public Publisher<List<Object>> doInRedis(ReactiveRedisConnection reactiveRedisConnection) throws DataAccessException {

						ByteBuffer keyWrap = ByteBuffer.wrap(key.getBytes());

						return Mono.zip(
								reactiveRedisConnection.numberCommands().incr(keyWrap),
								reactiveRedisConnection.keyCommands().expire(keyWrap, Duration.ofSeconds(59L))
						).then(Mono.empty());
					}
				})
				.then(next.handle(request));
	}


	private String requestAddress(Optional<InetSocketAddress> maybeAddress) {
		return maybeAddress.isPresent() ? maybeAddress.get().getHostName() : "";
	}
}
