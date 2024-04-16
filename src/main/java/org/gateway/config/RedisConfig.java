package org.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {


	@Bean
	public ReactiveRedisTemplate<String, Long> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {

		JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
		StringRedisSerializer stringRedisSerializer = StringRedisSerializer.UTF_8;
		GenericToStringSerializer<Long> longToStringSerializer = new GenericToStringSerializer<>(Long.class);

		return new ReactiveRedisTemplate<>(factory,
				RedisSerializationContext.<String, Long>newSerializationContext(jdkSerializationRedisSerializer)
						.key(stringRedisSerializer)
						.value(longToStringSerializer).build()
		);
	}

}
