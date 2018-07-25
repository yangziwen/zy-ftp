package io.github.yangziwen.zyftp.user;

public interface UserManager {

	User getUserByName(String username);
	
	boolean isAdmin(String username);
	
	boolean doesExist(String username);
	
	boolean save(User user);
	
	boolean delete(String username);
	
}
