package com.github.shicloud.bridge.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

	private static Logger log = LoggerFactory.getLogger(SystemController.class);

	@RequestMapping(value = "/ping")
	public String ping() {
		log.debug("ping");
		return "ok";
	}

	
}
