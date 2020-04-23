package com.chuncongcong.test.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author HU
 * @date 2020/4/23 17:03
 */

@Data
public class DecryptMsg {

	@JsonProperty(value = "Random")
	private String random;

	@JsonProperty(value = "EventType")
	private String eventType;

	@JsonProperty(value = "TestSuiteKey")
	private String testSuiteKey;
}
