package api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadpoolConfig {
	
	@Bean
	public ThreadPoolTaskExecutor taskExecutator() {
		ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
		pool.setCorePoolSize(100);
		pool.setMaxPoolSize(1000);
		pool.setQueueCapacity(3000);
		pool.setKeepAliveSeconds(2);
//		pool.setWaitForTasksToCompleteOnShutdown(true);
		return pool;
	}
}
