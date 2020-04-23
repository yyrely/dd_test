package com.chuncongcong.test.controller;

import com.chuncongcong.test.service.TestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author HU
 * @date 2020/4/16 21:53
 */

@RestController
public class TestController {

	@Autowired
	private TestService testService;

	@GetMapping("/test")
	public String test() {
		return testService.test();
	}
}
