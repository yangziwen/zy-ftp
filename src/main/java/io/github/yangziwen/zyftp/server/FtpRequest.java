package io.github.yangziwen.zyftp.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FtpRequest {
	
	private String command;
	
	private String argument;
	
}
