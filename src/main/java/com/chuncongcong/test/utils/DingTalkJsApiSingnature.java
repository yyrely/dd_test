package com.chuncongcong.test.utils;

import java.security.MessageDigest;
import java.util.Formatter;

public class DingTalkJsApiSingnature {
	public static String getJsApiSingnature(String url, String nonce, Long timeStamp, String jsTicket) throws DingTalkEncryptException {
		String plainTex = "jsapi_ticket=" + jsTicket + "&noncestr=" + nonce + "&timestamp=" + timeStamp + "&url=" + url;
		System.out.println(plainTex);
		String signature = "";
		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(plainTex.getBytes("UTF-8"));
			signature = byteToHex(crypt.digest());
			return signature;
		} catch (Exception e) {
			throw new DingTalkEncryptException(Integer.valueOf(900006));
		}
	}

	private static String byteToHex(byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", new Object[] { Byte.valueOf(b) });
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("=" +
				getJsApiSingnature("http://test4.weixin.wtoip.com/wtoip/dingding/dzmp", "abcde12345",
						Long.valueOf(1463132072L), "M95mXz9E4Wy9LjWBIYZdkXOhM7KRvXr5Cq2Yz521gi7d3rnqEY4JO2FFsjLgJC8b5G7ajnJARnidJVYl4hjaXD"));
		String url = "http://test4.weixin.wtoip.com:80/wtoip/dingding/dzmp";
		String nonce = "abcde12345";
		Long timeStamp = Long.valueOf(1463125744L);
		String tikcet = "gUsHOoPPzLVZKVkClnESg88m7qMV4c0Ys9VGsMigqzZU7gA8PeoNzHODmYPZ85TYuoZryXuqEUFlXLN1OPEixm";
		String jsApiSingnature = getJsApiSingnature(url, nonce, timeStamp, tikcet);
		System.err.println(jsApiSingnature + ", equals = " + jsApiSingnature
				.equals("d14dfc1d0d98cad2438e664723e8a9d8633b443f"));
	}
}
