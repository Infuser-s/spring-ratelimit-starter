package in.infusers.library.ratelimit.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.StringRedisTemplate;

import in.infusers.library.ratelimit.core.EndpointNormalizer;

@AutoConfiguration
@ConditionalOnClass(StringRedisTemplate.class)
@EnableConfigurationProperties({RateLimitStarterProperties.class, RateLimitProperties.class, IpSecurityProperties.class})
@ComponentScan(basePackages = "in.infusers.library.ratelimit")
public class RateLimitAutoConfiguration {

    @Bean
    public EndpointNormalizer endpointNormalizer(RateLimitStarterProperties properties) {
        return new EndpointNormalizer(properties);
    }
}
