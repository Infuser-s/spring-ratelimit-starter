package in.infusers.library.ratelimit.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.StringRedisTemplate;

import in.infusers.library.ratelimit.alert.NoOpSecurityAlertListener;
import in.infusers.library.ratelimit.alert.SecurityAlertListener;
import in.infusers.library.ratelimit.core.EndpointNormalizer;
import in.infusers.library.ratelimit.core.NoOpRateLimitEventListener;
import in.infusers.library.ratelimit.core.RateLimitEventListener;

@AutoConfiguration
@ConditionalOnClass(StringRedisTemplate.class)
@EnableConfigurationProperties({RateLimitStarterProperties.class, RateLimitProperties.class, IpSecurityProperties.class})
@ComponentScan(basePackages = "in.infusers.library.ratelimit")
public class RateLimitAutoConfiguration {

    @Bean
    public EndpointNormalizer endpointNormalizer(RateLimitStarterProperties properties) {
        return new EndpointNormalizer(properties);
    }

    @Bean
    @ConditionalOnMissingBean(RateLimitEventListener.class)
    public RateLimitEventListener noOpRateLimitEventListener() {
        return new NoOpRateLimitEventListener();
    }

    @Bean
    @ConditionalOnMissingBean(SecurityAlertListener.class)
    public SecurityAlertListener noOpSecurityAlertListener() {
        return new NoOpSecurityAlertListener();
    }
}
