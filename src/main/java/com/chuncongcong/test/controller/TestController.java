package com.chuncongcong.test.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.chuncongcong.test.domain.DecryptMsg;
import com.chuncongcong.test.service.TestService;
import com.chuncongcong.test.utils.DingTalkEncryptException;
import com.chuncongcong.test.utils.DingTalkEncryptor;
import com.chuncongcong.test.vo.DDVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author HU
 * @date 2020/4/16 21:53
 */

@RestController
public class TestController {

	@Autowired
	private TestService testService;

	@Autowired
	private ObjectMapper objectMapper;

	@PostMapping("/callback")
	public String test(@RequestBody DDVo ddVo, String signature, String timestamp, String nonce) throws JsonProcessingException {
		String decryptMsgString = null;
		DecryptMsg decryptMsg = null;
		DingTalkEncryptor dingTalkEncryptor = null;
		try {
			dingTalkEncryptor = new DingTalkEncryptor("123", "23b0rye8v70u6ucrt38wtm9wkvtqrw9dk2k8em5t1id", "suitevboo2acr6jp0ufce");
			decryptMsgString = dingTalkEncryptor.getDecryptMsg(signature, timestamp, nonce, ddVo.getEncrypt());
			decryptMsg = objectMapper.readValue(decryptMsgString, DecryptMsg.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(decryptMsg == null) {
			throw new RuntimeException("decryptMsg is null");
		}

		switch (decryptMsg.getEventType()) {
			case "check_create_suite_url":
				//可返回表明服务端“收到了”的字段
				break;
			default:
				break;
		}

		long timeStampLong = Long.parseLong(timestamp);
		Map<String, String> jsonMap = null;
		try {
			jsonMap = dingTalkEncryptor.getEncryptedMap("success", timeStampLong, nonce);
		} catch (DingTalkEncryptException e) {
			e.printStackTrace();
		}

		return objectMapper.writeValueAsString(jsonMap);
	}
}
