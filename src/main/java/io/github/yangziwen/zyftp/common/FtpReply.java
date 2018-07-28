package io.github.yangziwen.zyftp.common;

import lombok.Getter;

@Getter
public enum FtpReply {

	REPLY_110 (110, "restart marker reply"),

	REPLY_120 (120, "service ready in nnn minutes"),

	REPLY_125 (125, "data connection already open"),

	REPLY_150 (150, "file status okay"),

	REPLY_200 (200, "command okay"),

	REPLY_202 (202, "command not implemented"),

	REPLY_211 (211, "system status reply"),

	REPLY_212 (212, "directory status"),

	REPLY_213 (213, "file status"),

	REPLY_214 (214, "help message"),

	REPLY_215 (215, "name system type"),

	REPLY_220 (220, "service ready"),

	REPLY_221 (221, "closing control connection"),

	REPLY_225 (225, "data connection open no transfer in progress"),

	REPLY_226 (226, "closing data connection"),

	REPLY_227 (227, "entering passive mode"),

	REPLY_230 (230, "user logged in"),

	REPLY_250 (250, "requested file action okay"),

	REPLY_257 (257, "pathname created"),

	REPLY_331 (331, "user name okay need password"),

	REPLY_332 (332, "need account for login"),

	REPLY_350 (350, "requested file action pending further information"),

	REPLY_421 (421, "service not available closing control connection"),

	REPLY_425 (425, "cant open data connection"),

	REPLY_426 (426, "connection closed transfer aborted"),

	REPLY_450 (450, "requested file action not taken"),

	REPLY_451 (451, "requested action aborted"),

	REPLY_452 (452, "requested action not taken"),

	REPLY_500 (500, "syntax error command unrecognized"),

	REPLY_501 (501, "syntax error in parameters or arguments"),

	REPLY_502 (502, "command not implemented"),

	REPLY_503 (503, "bad sequence of commands"),

	REPLY_504 (504, "command not implemented for that parameter"),

	REPLY_530 (530, "invalid user"),

	REPLY_532 (532, "need account for storing files"),

	REPLY_550 (550, "requested action not taken"),

	REPLY_551 (551, "requested action aborted page type unknown"),

	REPLY_552 (552, "requested file action aborted exceeded storage"),

	REPLY_553 (553, "requested action not taken file name not allowed");

	private int code;

	private String description;

	private FtpReply(int code, String description) {
		this.code = code;
		this.description = description;
	}

}
