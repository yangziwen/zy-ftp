package io.github.yangziwen.zyftp.common;

public enum DataType {

	BINARY,

	ASCII;

	public static DataType from(String value) {
		if ("a".equalsIgnoreCase(value)) {
			return DataType.ASCII;
		}
		if ("i".equalsIgnoreCase(value)) {
			return DataType.BINARY;
		}
		throw new IllegalArgumentException("unknown data type: " + value);
	}

}
