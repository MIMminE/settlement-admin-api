package nuts.commerce.settlement.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppClockConfig {

    public static final ZoneId APP_ZONE = ZoneId.of("Asia/Seoul");

    @Bean
    public ZoneId appZoneId() {
        return APP_ZONE;
    }

    @Bean
    public Clock appClock() {
        return Clock.system(APP_ZONE);
    }
}