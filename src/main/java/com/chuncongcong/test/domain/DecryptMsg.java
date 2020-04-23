package com.chuncongcong.test.domain;

import lombok.Data;

/**
 * @author HU
 * @date 2020/4/23 17:03
 */

@Data
public class DecryptMsg {

	private String Random;

	private String EventType;

	private String TestSuiteKey;
}
