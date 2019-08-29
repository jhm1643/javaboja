package api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import api.domain.Rabbit;

@RestController
public class ExternalInterfaceController {

	@Autowired
	private Rabbit r;
	
	private static Logger logger = LoggerFactory.getLogger(ExternalInterfaceController.class);
	
}
