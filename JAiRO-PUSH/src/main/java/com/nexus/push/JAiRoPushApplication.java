package com.nexus.push;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableAsync
public class JAiRoPushApplication {

	public static void main(String[] args) {
		SpringApplication.run(JAiRoPushApplication.class, args);
	}

	@Bean(name="carrey")
    public TaskExecutor taskExecutor() {

        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(100);
        taskExecutor.setQueueCapacity(100);
        taskExecutor.setMaxPoolSize(300);
        taskExecutor.setThreadNamePrefix("carrey thread-");
        taskExecutor.initialize();
        return taskExecutor;
    }
}
