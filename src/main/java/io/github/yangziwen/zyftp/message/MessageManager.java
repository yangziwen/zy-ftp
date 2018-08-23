package io.github.yangziwen.zyftp.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageManager {

	private Configuration config = createTemplateConfiguration();

	private Configuration createTemplateConfiguration() {
		Map<String, String> responseMessages = loadResponseMessages();
		StringTemplateLoader templateLoader = new StringTemplateLoader();
		for (Entry<String, String> entry : responseMessages.entrySet()) {
			templateLoader.putTemplate(entry.getKey(), entry.getValue());
		}
		Configuration config = new Configuration(Configuration.VERSION_2_3_28);
		config.setTemplateLoader(templateLoader);
		config.setDefaultEncoding("UTF-8");
		config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		return config;
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

	public String render(String key, Object model) {
		StringWriter writer = new StringWriter();
		try {
			config.getTemplate(key).process(model, writer);
		} catch (TemplateException | IOException e) {
			log.error("failed to render template[{}] with model[{}]", key, model);
		}
		return writer.toString();
	}

}
