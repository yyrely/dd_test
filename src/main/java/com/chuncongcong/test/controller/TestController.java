package com.chuncongcong.test.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.chuncongcong.test.config.DingProperties;
import com.chuncongcong.test.utils.DingTalkEncryptException;
import com.chuncongcong.test.utils.DingTalkEncryptor;
import com.chuncongcong.test.utils.JacksonUtils;
import com.chuncongcong.test.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

/**
 * @author HU
 * @date 2020/4/16 21:53
 */

@Slf4j
@RestController
public class TestController {

    private static Map<String, String> publicMap = new HashMap<>();

    @Autowired
    private DingProperties dingProperties;

    @PostMapping("/callback")
    public Map<String, String> test(@RequestBody JsonNode jsonNode, String signature, String timestamp, String nonce) {

        String decryptText = decryptText(signature, timestamp, nonce, jsonNode.get("encrypt").textValue());
        JsonNode decryJsonNode = JacksonUtils.jsonToTree(decryptText);

        Map<String, String> resultMap = new HashMap<>();

        switch (decryJsonNode.get("EventType").textValue()) {
            case "check_create_suite_url":
                log.info("[callback] 验证回调地址有效性质:{}", decryJsonNode);
                resultMap = encryptText("success");
                break;
			case "suite_ticket":
				log.info("[callback] 验证回调地址有效性质:{}", decryJsonNode);
                String suiteTicket = decryJsonNode.get("SuiteTicket").textValue();
                publicMap.put("suiteTicket", suiteTicket);
                resultMap = encryptText("success");
                break;
            default:
                break;
        }

        return resultMap;
    }

    private String decryptText(String signature, String timestamp, String nonce, String encryptMsg) {
        String plainText = "";
        try {
            DingTalkEncryptor dingTalkEncryptor = new DingTalkEncryptor(dingProperties.getSuiteToken(),
                dingProperties.getEncodingAESKey(), dingProperties.getSuiteKey());
            plainText = dingTalkEncryptor.getDecryptMsg(signature, timestamp, nonce, encryptMsg);
        } catch (DingTalkEncryptException e) {
            log.error("钉钉消息体解密错误, signature: {}, timestamp: {}, nonce: {}, encryptMsg: {}, e: {}", signature, timestamp,
                nonce, encryptMsg, e);
        }
        log.debug("钉钉消息体解密, signature: {}, timestamp: {}, nonce: {}, encryptMsg: {}, 解密结果: {}", signature, timestamp,
            nonce, encryptMsg, plainText);
        return plainText;
    }

    private Map<String, String> encryptText(String text) {
        Map<String, String> resultMap = new LinkedHashMap<>();
        try {
            DingTalkEncryptor dingTalkEncryptor = new DingTalkEncryptor(dingProperties.getSuiteToken(),
                dingProperties.getEncodingAESKey(), dingProperties.getSuiteKey());
            resultMap = dingTalkEncryptor.getEncryptedMap(text, System.currentTimeMillis(), Utils.getRandomStr(8));
        } catch (DingTalkEncryptException e) {
            log.error("钉钉消息体加密,text: {}, e: {}", text, e);
        }
        log.debug("钉钉消息体加密,text: {}, resultMap: {}", text, resultMap);
        return resultMap;
    }
}
