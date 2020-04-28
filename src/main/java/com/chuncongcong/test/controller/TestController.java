package com.chuncongcong.test.controller;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
import org.apache.commons.codec.binary.Base64;

/**
 * @author HU
 * @date 2020/4/16 21:53
 */

@Slf4j
@RestController
public class TestController {

    private static Map<String, String> publicMap = new HashMap<>();

    static {
        publicMap.put("suiteTicket","IvXrFbyP1xvfOfYbqnj8s36HGq4cYQba0TgwAOUpaSLcCJBKIieFTFFndOCLJGlpofmcgNmGnPbDbhoQMPzbRN");
    }

    @Autowired
    private DingProperties dingProperties;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/callback")
    public Map<String, String> test(@RequestBody JsonNode jsonNode, String signature, String timestamp, String nonce) throws Exception {

        String decryptText = decryptText(signature, timestamp, nonce, jsonNode.get("encrypt").textValue());
        JsonNode decryJsonNode = JacksonUtils.jsonToTree(decryptText);

        log.info("decryJsonNode: {}", decryJsonNode);

        Map<String, String> resultMap = new HashMap<>();

        switch (decryJsonNode.get("EventType").textValue()) {
            case "bpms_instance_change":
                log.info("[callback] 审批事件回调:{}", decryJsonNode);
                String processInstanceId = decryJsonNode.get("processInstanceId").textValue();
                String type = decryJsonNode.get("type").textValue();
                log.info("processInstanceId: {}", processInstanceId);
                log.info("type: {}", type);
                break;
            case "check_url":
                log.info("[callback] 验证注册回调地址有效性质:{}", decryJsonNode);
                resultMap = encryptText("success");
                break;
            case "check_create_suite_url":
                log.info("[callback] 验证回调地址有效性质:{}", decryJsonNode);
                resultMap = encryptText("success");
                break;
            case "check_update_suite_url":
                log.info("[callback] 验证更新回调地址有效性质:{}", decryJsonNode);
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
                String suiteAccessToken = tokenResponse.get("suite_access_token").textValue();
                log.info("get access token success, accessToken{}", suiteAccessToken);

                // 获取permanentCode
                Map<String, String> permanentCodeBody = new HashMap<>();
                permanentCodeBody.put("tmp_auth_code", authCode);
                HttpEntity<Map<String, String>> permanentCodeRequest = new HttpEntity<>(permanentCodeBody);
                ResponseEntity<String> permanentCodeResponseEntity = restTemplate.postForEntity(
                    "https://oapi.dingtalk.com/service/get_permanent_code?suite_access_token=" + suiteAccessToken,
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
                    "https://oapi.dingtalk.com/service/activate_suite?suite_access_token=" + suiteAccessToken,
                    activateRequest, String.class);
                JsonNode activateResponse = JacksonUtils.jsonToTree(activateResponseEntity.getBody());
                log.info("activateResponse: {}", activateResponse);
                boolean isActive = "ok".equals(activateResponse.get("errmsg"));
                log.info("active result: {}", isActive);

                // 获取企业凭证
                long corpTokenTimestamp = System.currentTimeMillis();
                String corpTokenSignature = getSignature(corpTokenTimestamp);
                String urlEncode = urlEncode(corpTokenSignature, "utf-8");

                Map<String, String> corpTokenBody = new HashMap<>();
                corpTokenBody.put("auth_corpid", dingProperties.getCorpId());
                HttpEntity<Map<String, String>> corpTokenRequest = new HttpEntity<>(corpTokenBody);
                String corpTokenUrl = "https://oapi.dingtalk.com/service/get_corp_token?signature="
                    + urlEncode + "&timestamp=" + corpTokenTimestamp + "&suiteTicket="
                    + publicMap.get("suiteTicket") + "&accessKey=" + dingProperties.getSuiteKey();
                log.info("corpTokenUrl: {}", corpTokenUrl);
                ResponseEntity<String> corpTokenEntity =
                    restTemplate.postForEntity(new URI(corpTokenUrl), corpTokenRequest, String.class);
                JsonNode corpTokenResponse = JacksonUtils.jsonToTree(corpTokenEntity.getBody());
                log.info("corpTokenResponse: {}", corpTokenResponse);
                String accessToken = corpTokenResponse.get("access_token").textValue();

                // 注册业务事件回调
                Map<String, String> registerCallBody = new HashMap<>();
                registerCallBody.put("call_back_tag", JacksonUtils.toJson(new String[] {"bpms_instance_change","bpms_task_change"}));
                registerCallBody.put("token", dingProperties.getSuiteToken());
                registerCallBody.put("aes_key", dingProperties.getEncodingAESKey());
                registerCallBody.put("url", "http://www.chuncongcong.com:8030/callback");
                HttpEntity<Map<String, String>> registerCallRequest = new HttpEntity<>(registerCallBody);
                ResponseEntity<String> registerCallEntity = restTemplate.postForEntity(
                    "https://oapi.dingtalk.com/call_back/register_call_back?access_token={accessToken}",
                    registerCallRequest, String.class, accessToken);
                JsonNode registerCallResponse = JacksonUtils.jsonToTree(registerCallEntity.getBody());
                log.info("registerCallResponse: {}", registerCallResponse);
                boolean isRegisterCall = "ok".equals(registerCallResponse.get("errmsg"));
                resultMap = encryptText(isRegisterCall ? "success" : "failed");
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

    private String getSignature(long timestamp) {
        try {
            String stringToSign = timestamp+"\n"+publicMap.get("suiteTicket");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(dingProperties.getSuiteSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.encodeBase64(signData));
        } catch (Exception e) {
            log.error("签名计算异常, e: {}", e);
            throw new RuntimeException(e);
        }
    }

    public static String urlEncode(String value, String encoding) {
        if (value == null) {
            return "";
        }

        try {
            String encoded = URLEncoder.encode(value, encoding);
            return encoded.replace("+", "%20").replace("*", "%2A")
                    .replace("~", "%7E").replace("/", "%2F");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("FailedToEncodeUri", e);
        }
    }
}
