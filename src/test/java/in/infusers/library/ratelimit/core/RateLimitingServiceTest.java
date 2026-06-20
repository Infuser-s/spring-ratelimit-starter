package in.infusers.library.ratelimit.core;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import in.infusers.library.ratelimit.config.RateLimitStarterProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitingServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private RateLimitEventListener eventListener;

    private RateLimitingService service;

    @BeforeEach
    void setUp() {
        service = new RateLimitingService(redisTemplate, new RateLimitStarterProperties(), eventListener);
    }

    @Test
    void allowsRequestUnderLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("ratelimit:key1")).thenReturn(1L);

        assertThat(service.isAllowed("key1", 5, Duration.ofMinutes(1))).isTrue();
        verify(redisTemplate).expire(eq("ratelimit:key1"), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    void blocksRequestOverLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("ratelimit:key1")).thenReturn(6L);

        assertThat(service.isAllowed("key1", 5, Duration.ofMinutes(1))).isFalse();
    }

    @Test
    void onlySetsExpiryOnFirstRequestInWindow() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("ratelimit:key1")).thenReturn(2L);

        service.isAllowed("key1", 5, Duration.ofMinutes(1));

        verify(redisTemplate, never()).expire(any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void blocksWhenRedisIncrementReturnsNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("ratelimit:key1")).thenReturn(null);

        assertThat(service.isAllowed("key1", 5, Duration.ofMinutes(1))).isFalse();
    }

    @Test
    void logRateLimitEventForwardsToListener() {
        service.logRateLimitEvent("API_PATH", "/foo", "exceeded", "1.2.3.4", "curl");

        verify(eventListener).onRateLimitEvent("API_PATH", "/foo", "exceeded", "1.2.3.4", "curl");
    }

    @Test
    void threeArgOverloadPassesNullsForIpAndUserAgent() {
        service.logRateLimitEvent("API_PATH", "/foo", "exceeded");

        verify(eventListener).onRateLimitEvent("API_PATH", "/foo", "exceeded", null, null);
    }
}
