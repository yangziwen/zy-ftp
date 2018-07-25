package io.github.yangziwen.zyftp.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageResource {
	
	private final Map<String, String> responseMessages = loadResponseMessages();
	
	public String getMessage(int code, String subId) {
		String key = String.valueOf(code);
		if (StringUtils.isNotBlank(subId)) {
			key += "." + subId;
		}
		return responseMessages.get(key);
	}
	
	private Map<String, String> loadResponseMessages() {
		Map<String, String> messages = new HashMap<>();
		try (InputStream in = getClass().getClassLoader().getResourceAsStream("response-message.properties")) {
			Properties properties = new Properties();
			properties.load(in);
			for (Entry<Object, Object> entry : properties.entrySet()) {
				messages.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
			}
			return messages;
		} catch (IOException e) {
			log.error("failed to load response messages", e);
			throw new RuntimeException(e);
		}
	}

}
