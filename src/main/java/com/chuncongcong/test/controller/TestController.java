package com.chuncongcong.test.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/callback")
    public Map<String, String> test(@RequestBody JsonNode jsonNode, String signature, String timestamp, String nonce) {

        System.out.println(jsonNode);
        System.out.println(signature);
        System.out.println(timestamp);
        System.out.println(nonce);

        String decryptText = decryptText(signature, timestamp, nonce, jsonNode.get("encrypt").textValue());
        JsonNode decryJsonNode = JacksonUtils.jsonToTree(decryptText);

        Map<String, String> resultMap = new HashMap<>();

        switch (decryJsonNode.get("EventType").textValue()) {
            case "check_create_suite_url":
                log.info("[callback] 验证回调地址有效性质:{}", decryJsonNode);
                resultMap = encryptText("success");
                break;
            case "suite_ticket":
                log.info("[callback] 推送suite_ticket:{}", decryJsonNode);
                String suiteTicket = decryJsonNode.get("SuiteTicket").textValue();
                publicMap.put("suiteTicket", suiteTicket);
                resultMap = encryptText("success");
                break;
            case "tmp_auth_code":
                log.info("[callback] 授权开通:{}", decryJsonNode);
                log.info("suiteTicket: {}", publicMap.get("suiteTicket"));
                String authCorpId = decryJsonNode.get("AuthCorpId").textValue();
                String authCode = decryJsonNode.get("AuthCode").textValue();
                // 获取accessToken
                Map<String, String> tokenBody = new HashMap<>();
                tokenBody.put("suite_key", dingProperties.getSuiteKey());
                tokenBody.put("suite_secret", dingProperties.getSuiteSecret());
                tokenBody.put("suite_ticket", publicMap.get("suiteTicket"));
                HttpEntity<Map<String, String>> tokenRequest = new HttpEntity<>(tokenBody);
                ResponseEntity<String> tokenResponseEntity = restTemplate
                    .postForEntity("https://oapi.dingtalk.com/service/get_suite_token", tokenRequest, String.class);
                JsonNode tokenResponse = JacksonUtils.jsonToTree(tokenResponseEntity.getBody());
                String accessToken = tokenResponse.get("suite_access_token").textValue();
                log.info("get access token success, accessToken{}", accessToken);

                // 获取permanentCode
                Map<String, String> permanentCodeBody = new HashMap<>();
                permanentCodeBody.put("tmp_auth_code", authCode);
                HttpEntity<Map<String, String>> permanentCodeRequest = new HttpEntity<>(permanentCodeBody);
                ResponseEntity<String> permanentCodeResponseEntity = restTemplate.postForEntity(
                    "https://oapi.dingtalk.com/service/get_permanent_code?suite_access_token=" + accessToken,
                    permanentCodeRequest, String.class);
                log.info("permanentCodeResponseEntity: {}", permanentCodeResponseEntity);
                JsonNode permanentCodeResponse = JacksonUtils.jsonToTree(permanentCodeResponseEntity.getBody());
                String permanentCode = permanentCodeResponse.get("permanent_code").textValue();
                log.info("get permanentCode success, permanentCode{}", permanentCode);

                // 激活应用
                Map<String, String> activateBody = new HashMap<>();
                activateBody.put("suite_key", dingProperties.getSuiteKey());
                activateBody.put("auth_corpid", authCorpId);
                activateBody.put("permanent_code", permanentCode);
                HttpEntity<Map<String, String>> activateRequest = new HttpEntity<>(activateBody);
                ResponseEntity<String> activateResponseEntity = restTemplate.postForEntity(
                    "https://oapi.dingtalk.com/service/activate_suite?suite_access_token=" + accessToken, activateRequest, String.class);
                JsonNode activateResponse = JacksonUtils.jsonToTree(activateResponseEntity.getBody());
                Boolean isActive = "ok".equals(activateResponse.get("errmsg"));
                resultMap = encryptText(isActive ? "success" : "failed");
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
