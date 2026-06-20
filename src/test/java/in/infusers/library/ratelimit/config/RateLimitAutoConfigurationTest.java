package in.infusers.library.ratelimit.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import in.infusers.library.ratelimit.alert.NoOpSecurityAlertListener;
import in.infusers.library.ratelimit.alert.SecurityAlertListener;
import in.infusers.library.ratelimit.core.EndpointNormalizer;
import in.infusers.library.ratelimit.core.NoOpRateLimitEventListener;
import in.infusers.library.ratelimit.core.RateLimitEventListener;
import in.infusers.library.ratelimit.core.RateLimitingService;
import in.infusers.library.ratelimit.filter.IPSecurityRateLimitFilter;
import in.infusers.library.ratelimit.filter.RateLimitingFilter;
import in.infusers.library.ratelimittest.AutoConfigTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AutoConfigTestSupport.RedisStubConfig.class)
            .withConfiguration(AutoConfigurations.of(RateLimitAutoConfiguration.class));

    @Test
    void autoConfigurationWiresAllExpectedBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RateLimitingService.class);
            assertThat(context).hasSingleBean(EndpointNormalizer.class);
            assertThat(context).hasSingleBean(IPSecurityRateLimitFilter.class);
            assertThat(context).hasSingleBean(RateLimitingFilter.class);
        });
    }

    @Test
    void defaultsToNoOpListenersWhenNoneRegistered() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RateLimitEventListener.class);
            assertThat(context.getBean(RateLimitEventListener.class)).isInstanceOf(NoOpRateLimitEventListener.class);
            assertThat(context).hasSingleBean(SecurityAlertListener.class);
            assertThat(context.getBean(SecurityAlertListener.class)).isInstanceOf(NoOpSecurityAlertListener.class);
        });
    }

    @Test
    void customListenerOverridesNoOpDefault() {
        contextRunner.withUserConfiguration(AutoConfigTestSupport.CustomListenerConfig.class).run(context -> {
            assertThat(context.getBean(SecurityAlertListener.class)).isNotInstanceOf(NoOpSecurityAlertListener.class);
        });
    }
}
