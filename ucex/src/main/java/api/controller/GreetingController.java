package api.controller;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import api.domain.Greeting;
import api.domain.Rabbit;

@RestController
public class GreetingController {

	@Autowired
	private Rabbit r;

	private static Logger logger = LoggerFactory.getLogger(GreetingController.class);
	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@RequestMapping(value="/ucex/hello")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		logger.info("greeting GET {}" );

		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}
}
