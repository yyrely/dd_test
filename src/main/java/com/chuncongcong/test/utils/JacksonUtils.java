package com.chuncongcong.test.utils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public abstract class JacksonUtils {

	private static final ObjectMapper DEFAULT_OBJECT_MAPPER = newDefaultObjectMapper();

	private static final ObjectMapper DEFAULT_OBJECT_MAPPER_INCLUDE_NULL;

	static {
		DEFAULT_OBJECT_MAPPER_INCLUDE_NULL = newDefaultObjectMapper();
		DEFAULT_OBJECT_MAPPER_INCLUDE_NULL.setSerializationInclusion(JsonInclude.Include.ALWAYS);
	}

	private static final TypeReference<HashMap<String, String>> STRING_MAP =
			new TypeReference<HashMap<String, String>>() {};

	public static final ObjectMapper newDefaultObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		disableFeatures(objectMapper);
		enableFeatures(objectMapper);
		return objectMapper;
	}

	private static void disableFeatures(ObjectMapper objectMapper) {
		objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}

	private static void enableFeatures(ObjectMapper objectMapper) {}

	public static final String toJson(Object object) {
		return toJson(DEFAULT_OBJECT_MAPPER, object);
	}

	public static final String toJson(ObjectMapper objectMapper, Object object) {
		assertObjectMapper(objectMapper);
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new JsonException(e);
		}
	}

	public static final <T> T jsonToObject(String json, Class<T> clazz) {
		return jsonToObject(DEFAULT_OBJECT_MAPPER, json, clazz);
	}

	public static final <T> T jsonToObject(String json, TypeReference<T> typeReference) {
		return jsonToObject(DEFAULT_OBJECT_MAPPER, json, typeReference);
	}

	public static final <T> T jsonToObject(ObjectMapper objectMapper, String json, Class<T> clazz) {
		assertObjectMapper(objectMapper);
		try {
			return objectMapper.readValue(json, clazz);
		} catch (Exception e) {
			throw new JsonException(e);
		}
	}

	public static final <T> T jsonToObject(ObjectMapper objectMapper, String json, TypeReference<T> typeReference) {
		assertObjectMapper(objectMapper);
		try {
			return objectMapper.readValue(json, typeReference);
		} catch (Exception e) {
			throw new JsonException(e);
		}
	}

	public static final <T> T jsonToObject(InputStream inputStream, Class<T> clazz) {
		return jsonToObject(DEFAULT_OBJECT_MAPPER, inputStream, clazz);
	}

	public static final <T> T jsonToObject(ObjectMapper objectMapper, InputStream inputStream, Class<T> clazz) {
		assertObjectMapper(objectMapper);
		try {
			return objectMapper.readValue(inputStream, clazz);
		} catch (Exception e) {
			throw new JsonException(e);
		}
	}

	public static final <T> List<T> jsonToList(String json, Class<T> clazz) {
		return jsonToList(DEFAULT_OBJECT_MAPPER, json, clazz);
	}

	public static final <T> List<T> jsonToList(ObjectMapper objectMapper, String json, Class<T> clazz) {
		assertObjectMapper(objectMapper);
		try {
			return objectMapper.readValue(json,
					objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
			// return objectMapper.readValue(json, new TypeReference<List<T>>() {
			// });
		} catch (Exception e) {
			throw new JsonException(e);
		}
	}

	public static final JsonNode jsonToTree(String json) {
		return jsonToTree(DEFAULT_OBJECT_MAPPER, json);
	}

	public static final JsonNode jsonToTree(ObjectMapper objectMapper, String json) {
		assertObjectMapper(objectMapper);
		try {
			return objectMapper.readTree(json);
		} catch (Exception e) {
			throw new JsonException(e);
		}
	}

	public static final <T> T treeToValue(JsonNode jsonNode, Class<T> clazz) {
		return treeToValue(DEFAULT_OBJECT_MAPPER, jsonNode, clazz);
	}

	public static final <T> T treeToValue(ObjectMapper objectMapper, JsonNode jsonNode, Class<T> clazz) {
		assertObjectMapper(objectMapper);
		try {
			return objectMapper.treeToValue(jsonNode, clazz);
		} catch (Exception e) {
			throw new JsonException(e);
		}
	}

	public static final Map<String, Object> convertToMap(Object object) {
		return convertToMap(DEFAULT_OBJECT_MAPPER, object);
	}

	public static final Map<String, Object> convertToMap(ObjectMapper objectMapper, Object object) {
		assertObjectMapper(objectMapper);
		return objectMapper.convertValue(object, Map.class);
	}

	public static final Map<String, String> convertToStringMap(Object object) {
		return convertToStringMap(DEFAULT_OBJECT_MAPPER, object);
	}

	public static final Map<String, String> convertToStringMapIncludeNull(Object object) {
		return convertToStringMap(DEFAULT_OBJECT_MAPPER_INCLUDE_NULL, object);
	}

	public static final Map<String, String> convertToStringMap(ObjectMapper objectMapper, Object object) {
		assertObjectMapper(objectMapper);
		return objectMapper.convertValue(object, STRING_MAP);
	}

	public static final <E> E convert(Object from, Class<E> to) {
		return convert(DEFAULT_OBJECT_MAPPER, from, to);
	}

	public static final <E> E convert(ObjectMapper objectMapper, Object from, Class<E> to) {
		assertObjectMapper(objectMapper);
		return objectMapper.convertValue(from, to);
	}

	public static final <E> E stringMapToObject(Map<String, String> map, Class<E> clazz) {
		return stringMapToObject(DEFAULT_OBJECT_MAPPER, map, clazz);
	}

	public static final <E> E stringMapToObject(ObjectMapper objectMapper, Map<String, String> map, Class<E> clazz) {
		return objectMapper.convertValue(map, clazz);
	}

	private static void assertObjectMapper(ObjectMapper objectMapper) {
		Objects.requireNonNull(objectMapper, "ObjectMapper must not be null.");
	}

	public static class JsonException extends RuntimeException {

		private static final long serialVersionUID = -8318031819390714507L;

		public JsonException() {}

		public JsonException(String message) {
			super(message);
		}

		public JsonException(String message, Throwable cause) {
			super(message, cause);
		}

		public JsonException(Throwable cause) {
			super(cause);
		}

		public JsonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}
	}
}

