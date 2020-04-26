package com.chuncongcong.test.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @author HU
 * @date 2020/4/24 17:05
 */

@Data
@Configuration
public class DingProperties {

	public static final String url_suite_token = "https://oapi.dingtalk.com/service/get_suite_token";

	@Value("${ding.suite.key}")
	private String suiteKey;

	/**
	 * 套件secret
	 */
	@Value("${ding.suite.secret}")
	private String suiteSecret;

	/**
	 * 套件token
	 * 在微应用的基础信息中我们自定义的那个token
	 */
	@Value("${ding.suite.token}")
	private String suiteToken;

	/**
	 * 套件.数据加密密钥.用于回调的解密
	 * 在微应用的基础信息中"数据加密密钥"
	 */
	@Value("${ding.suite.aes-key}")
	private String encodingAESKey;

}
