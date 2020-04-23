package com.chuncongcong.test.utils;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Security;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

public class DingTalkEncryptor {
	private static final Charset CHARSET = Charset.forName("utf-8");

	private static final Base64 base64 = new Base64();

	private byte[] aesKey;

	private String token;

	private String corpId;

	private static final Integer AES_ENCODE_KEY_LENGTH = Integer.valueOf(43);

	private static final Integer RANDOM_LENGTH = Integer.valueOf(16);

	static {
		try {
			Security.setProperty("crypto.policy", "limited");
			RemoveCryptographyRestrictions();
		} catch (Exception exception) {}
	}

	public DingTalkEncryptor(String token, String encodingAesKey, String corpIdOrSuiteKey) throws DingTalkEncryptException {
		if (null == encodingAesKey || encodingAesKey.length() != AES_ENCODE_KEY_LENGTH.intValue())
			throw new DingTalkEncryptException(Integer.valueOf(900004));
		this.token = token;
		this.corpId = corpIdOrSuiteKey;
		this.aesKey = Base64.decodeBase64(encodingAesKey + "=");
	}

	public Map<String, String> getEncryptedMap(String plaintext, Long timeStamp, String nonce) throws DingTalkEncryptException {
		if (null == plaintext) {
			throw new DingTalkEncryptException(Integer.valueOf(900001));
		}
		if (null == timeStamp) {
			throw new DingTalkEncryptException(Integer.valueOf(900002));
		}
		if (null == nonce) {
			throw new DingTalkEncryptException(Integer.valueOf(900003));
		}
		String encrypt = encrypt(Utils.getRandomStr(RANDOM_LENGTH.intValue()), plaintext);
		String signature = getSignature(this.token, String.valueOf(timeStamp), nonce, encrypt);
		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("msg_signature", signature);
		resultMap.put("encrypt", encrypt);
		resultMap.put("timeStamp", String.valueOf(timeStamp));
		resultMap.put("nonce", nonce);
		return resultMap;
	}

	public String getDecryptMsg(String msgSignature, String timeStamp, String nonce, String encryptMsg) throws DingTalkEncryptException {
		String signature = getSignature(this.token, timeStamp, nonce, encryptMsg);
		if (!signature.equals(msgSignature)) {
			throw new DingTalkEncryptException(Integer.valueOf(900006));
		}
		String result = decrypt(encryptMsg);
		return result;
	}

	private String encrypt(String random, String plaintext) throws DingTalkEncryptException {
		try {
			byte[] randomBytes = random.getBytes(CHARSET);
			byte[] plainTextBytes = plaintext.getBytes(CHARSET);
			byte[] lengthByte = Utils.int2Bytes(plainTextBytes.length);
			byte[] corpidBytes = this.corpId.getBytes(CHARSET);
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			byteStream.write(randomBytes);
			byteStream.write(lengthByte);
			byteStream.write(plainTextBytes);
			byteStream.write(corpidBytes);
			byte[] padBytes = PKCS7Padding.getPaddingBytes(byteStream.size());
			byteStream.write(padBytes);
			byte[] unencrypted = byteStream.toByteArray();
			byteStream.close();
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			SecretKeySpec keySpec = new SecretKeySpec(this.aesKey, "AES");
			IvParameterSpec iv = new IvParameterSpec(this.aesKey, 0, 16);
			cipher.init(1, keySpec, iv);
			byte[] encrypted = cipher.doFinal(unencrypted);
			String result = base64.encodeToString(encrypted);
			return result;
		} catch (Exception e) {
			throw new DingTalkEncryptException(Integer.valueOf(900007));
		}
	}

	private String decrypt(String text) throws DingTalkEncryptException {
		byte[] originalArr;
		String plainText;
		String fromCorpid;
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			SecretKeySpec keySpec = new SecretKeySpec(this.aesKey, "AES");
			IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(this.aesKey, 0, 16));
			cipher.init(2, keySpec, iv);
			byte[] encrypted = Base64.decodeBase64(text);
			originalArr = cipher.doFinal(encrypted);
		} catch (Exception e) {
			throw new DingTalkEncryptException(Integer.valueOf(900008));
		}
		try {
			byte[] bytes = PKCS7Padding.removePaddingBytes(originalArr);
			byte[] networkOrder = Arrays.copyOfRange(bytes, 16, 20);
			int plainTextLegth = Utils.bytes2int(networkOrder);
			plainText = new String(Arrays.copyOfRange(bytes, 20, 20 + plainTextLegth), CHARSET);
			fromCorpid = new String(Arrays.copyOfRange(bytes, 20 + plainTextLegth, bytes.length), CHARSET);
		} catch (Exception e) {
			throw new DingTalkEncryptException(Integer.valueOf(900009));
		}
		if (!fromCorpid.equals(this.corpId)) {
			throw new DingTalkEncryptException(Integer.valueOf(900010));
		}
		return plainText;
	}

	public String getSignature(String token, String timestamp, String nonce, String encrypt) throws DingTalkEncryptException {
		try {
			String[] array = { token, timestamp, nonce, encrypt };
			Arrays.sort((Object[])array);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < 4; i++) {
				sb.append(array[i]);
			}
			String str = sb.toString();
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(str.getBytes());
			byte[] digest = md.digest();
			StringBuffer hexstr = new StringBuffer();
			String shaHex = "";
			for (int j = 0; j < digest.length; j++) {
				shaHex = Integer.toHexString(digest[j] & 0xFF);
				if (shaHex.length() < 2) {
					hexstr.append(0);
				}
				hexstr.append(shaHex);
			}
			return hexstr.toString();
		} catch (Exception e) {
			throw new DingTalkEncryptException(Integer.valueOf(900006));
		}
	}

	private static void RemoveCryptographyRestrictions() throws Exception {
		Class<?> jceSecurity = getClazz("javax.crypto.JceSecurity");
		Class<?> cryptoPermissions = getClazz("javax.crypto.CryptoPermissions");
		Class<?> cryptoAllPermission = getClazz("javax.crypto.CryptoAllPermission");
		if (jceSecurity != null) {
			setFinalStaticValue(jceSecurity, "isRestricted", Boolean.valueOf(false));
			PermissionCollection defaultPolicy = getFieldValue(jceSecurity, "defaultPolicy", null, PermissionCollection.class);
			if (cryptoPermissions != null) {
				Map<?, ?> map = (Map<?, ?>) getFieldValue(cryptoPermissions, "perms", defaultPolicy, (Class)Map.class);
				map.clear();
			}
			if (cryptoAllPermission != null) {
				Permission permission = getFieldValue(cryptoAllPermission, "INSTANCE", null, Permission.class);
				defaultPolicy.add(permission);
			}
		}
	}

	private static Class<?> getClazz(String className) {
		Class<?> clazz = null;
		try {
			clazz = Class.forName(className);
		} catch (Exception exception) {}
		return clazz;
	}

	private static void setFinalStaticValue(Class<?> srcClazz, String fieldName, Object newValue) throws Exception {
		Field field = srcClazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & 0xFFFFFFEF);
		field.set((Object)null, newValue);
	}

	private static <T> T getFieldValue(Class<?> srcClazz, String fieldName, Object owner, Class<T> dstClazz) throws Exception {
		Field field = srcClazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		return dstClazz.cast(field.get(owner));
	}
}

