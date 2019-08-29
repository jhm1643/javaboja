package api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import api.interceptor.AccessLogInterceptor;

@Configuration
@EnableWebMvc
@ComponentScan
public class WebApplicationConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(accessLogInterceptor())
				.addPathPatterns("/ucex/api/v1/**")
				.addPathPatterns("/ucex/api/cloud/**")
				.excludePathPatterns("/ucex/api/v1_test/**")
				.excludePathPatterns("/ucex/api/v1/users/admin/login");
	}

	@Bean
	public HandlerInterceptor accessLogInterceptor() {
		return new AccessLogInterceptor();
	}

}
