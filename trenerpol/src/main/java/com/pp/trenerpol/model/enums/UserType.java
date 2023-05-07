package com.pp.trenerpol.model.enums;

public enum UserType {
	PROTEGEE("P"), TRAINER("T");

	private final String dbValue;

	UserType(String dbValue) {
		this.dbValue = dbValue;
	}

	public String getDbValue() {
		return this.dbValue;
	}

	@Override
	public String toString() {
		return this.dbValue;
	}
}
