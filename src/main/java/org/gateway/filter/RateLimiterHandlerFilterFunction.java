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
	private static Long MAX_REQUESTS_PER_MINUTE = 5L;

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

		return redisTemplate.opsForValue().get(key)
				.flatMap(value -> value >= MAX_REQUESTS_PER_MINUTE ? ServerResponse.status(429).build() : incrAndExpireKey(key, request, next))
				.switchIfEmpty(incrAndExpireKey(key, request, next));
	}


	/**
	 * This method is responsible for incrementing a counter in Redis and setting an expiration time for the key.
	 * It is used to limit the number of requests a client can make within a certain time frame.
	 *
	 * @param key     The unique identifier for the client and the current minute.
	 * @param request The current server request.
	 * @param next    The handler function to be executed if the rate limit has not been exceeded.
	 * @return A Mono of ServerResponse. If the rate limit has not been exceeded, it will continue the processing of the request.
	 * If the rate limit has been exceeded, the request will not be processed and a 429 status code (Too Many Requests) will be returned to the client.
	 */
	private Mono<ServerResponse> incrAndExpireKey(String key, ServerRequest request,
												  HandlerFunction<ServerResponse> next) {

		return redisTemplate.execute(new ReactiveRedisCallback<List<Object>>() {

					/**
					 * This callback is used to perform two operations on Redis: incrementing the value of the key and setting an expiration time for the key.
					 * The incr command is used to increment the value of the key. If the key does not exist, Redis will create it with a value of 0 before performing the increment operation.
					 * The expire command is used to set an expiration time for the key. In this case, the key will expire after 59 seconds
					 **/
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
