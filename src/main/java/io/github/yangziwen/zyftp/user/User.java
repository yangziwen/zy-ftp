package io.github.yangziwen.zyftp.user;

import lombok.Data;

@Data
public class User {
	
	private String username;
	
	private String password;
	
	private boolean isAuthenticated;
	
	private int maxIdleTime;
	
	private String homeDirectory;

}
