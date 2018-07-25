package io.github.yangziwen.zyftp.message;

import java.util.List;
import java.util.Map;

public interface MessageResource {
	
	List<String> getAvailableLanguages();
	
	String getMessage(int code, String subId, String language);

	Map<String, String> getMessages(String language);

}
