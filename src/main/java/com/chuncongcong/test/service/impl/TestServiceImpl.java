package com.chuncongcong.test.service.impl;

import com.chuncongcong.test.controller.TestController;
import com.chuncongcong.test.service.TestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author HU
 * @date 2020/4/17 9:57
 */

@Service
public class TestServiceImpl implements TestService {

	@Override
	public String test() {
		return "hahahaha";
	}
}
