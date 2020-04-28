package com.chuncongcong.test.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.chuncongcong.test.config.DingProperties;
import com.chuncongcong.test.utils.JacksonUtils;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

/**
 * @author HU
 * @date 2020/4/28 20:02
 */

@Slf4j
@RestController
public class CallBackRegisterController {

    private static final String APP_KEY = "dinggwcpfmr9u4lg9esm";

    private static final String APP_SECRET = "k6i8xIqngf91qcJVbW_aRJFNus-D_sSwIGcLlZMOSTwr1cqz1yoGM3zYFEEsuyTt";

    @Autowired
	private DingProperties dingProperties;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/callBack/register")
    public void register() {
        ResponseEntity<String> tokenResponseEntity = restTemplate.getForEntity(
            "https://oapi.dingtalk.com/gettoken?appkey={1}&appsecret={2}", String.class, APP_KEY, APP_SECRET);
		JsonNode tokenResponseBody = JacksonUtils.jsonToTree(tokenResponseEntity.getBody());
		String accessToken = tokenResponseBody.get("access_token").textValue();

		// 注册业务事件回调
		Map<String, String> registerCallBody = new HashMap<>();
		registerCallBody.put("call_back_tag", JacksonUtils.toJson(new String[] {"bpms_instance_change","bpms_task_change"}));
		registerCallBody.put("token", dingProperties.getSuiteToken());
		registerCallBody.put("aes_key", dingProperties.getSuiteKey());
		registerCallBody.put("url", "http://www.chuncongcong.com:8030/callback");
		HttpEntity<Map<String, String>> registerCallRequest = new HttpEntity<>(registerCallBody);
		ResponseEntity<String> registerCallEntity = restTemplate.postForEntity(
				"https://oapi.dingtalk.com/call_back/register_call_back?access_token={accessToken}",
				registerCallRequest, String.class, accessToken);
		JsonNode registerCallResponse = JacksonUtils.jsonToTree(registerCallEntity.getBody());
		log.info("registerCallResponse: {}", registerCallResponse);

		ResponseEntity<String> getCallBackEntity = restTemplate.getForEntity(
				"https://oapi.dingtalk.com/call_back/get_call_back?access_token={accessToken}", String.class, accessToken);
		JsonNode getCallBackResponse = JacksonUtils.jsonToTree(getCallBackEntity.getBody());
		log.info("getCallBackResponse: {}", getCallBackResponse);
	}
}
