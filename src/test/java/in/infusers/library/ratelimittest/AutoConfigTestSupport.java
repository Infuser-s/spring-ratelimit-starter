package in.infusers.library.ratelimittest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import in.infusers.library.ratelimit.alert.SecurityAlertListener;

/**
 * Deliberately outside the {@code in.infusers.library.ratelimit} package tree.
 * RateLimitAutoConfiguration component-scans that whole tree, so any
 * {@code @Configuration} class declared inside it — including test helpers —
 * gets swept up and silently becomes a real bean in every test's context.
 */
public final class AutoConfigTestSupport {

    private AutoConfigTestSupport() {
    }

    @Configuration
    public static class RedisStubConfig {
        @Bean
        RedisConnectionFactory redisConnectionFactory() {
            return new LettuceConnectionFactory();
        }

        @Bean
        StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
            return new StringRedisTemplate(factory);
        }
    }

    @Configuration
    public static class CustomListenerConfig {
        @Bean
        SecurityAlertListener customSecurityAlertListener() {
            return (subject, body) -> { };
        }
    }
}
