package com.chuncongcong.test.controller;

import com.chuncongcong.test.service.TestService;
import com.chuncongcong.test.utils.DingTalkEncryptException;
import com.chuncongcong.test.utils.DingTalkEncryptor;
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
	public String test(@RequestBody DDVo ddVo, String signature, String timestamp, String nonce) {
		String decryptMsg = null;
		try {
			DingTalkEncryptor dingTalkEncryptor = new DingTalkEncryptor("123", "23b0rye8v70u6ucrt38wtm9wkvtqrw9dk2k8em5t1id", "suitevboo2acr6jp0ufce");
			decryptMsg = dingTalkEncryptor.getDecryptMsg(signature, timestamp, nonce, ddVo.getEncrypt());
		} catch (DingTalkEncryptException e) {
			e.printStackTrace();
		}
		return decryptMsg;
	}
}
