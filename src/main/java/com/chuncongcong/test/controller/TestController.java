package com.chuncongcong.test.controller;

import com.chuncongcong.test.service.TestService;
import com.chuncongcong.test.vo.DDVo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author HU
 * @date 2020/4/16 21:53
 */

@RestController
public class TestController {

	@Autowired
	private TestService testService;

	@PostMapping("/callback")
	public String test(@RequestBody DDVo ddVo, String signature, Long timestamp, String nonce) {
		System.out.println(ddVo.getEncrypt());
		System.out.println(signature);
		System.out.println(timestamp);
		System.out.println(nonce);
		return null;
	}
}
