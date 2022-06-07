package top.xlaoer.nowcodercommunity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Xlaoer
 * @date 2022/6/7 15:35
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
