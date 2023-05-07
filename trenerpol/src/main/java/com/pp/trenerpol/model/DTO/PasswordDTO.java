package com.pp.trenerpol.model.DTO;

import lombok.Data;

@Data
public class PasswordDTO {
	private String oldPassword;
	private String newPassword;
	private String repetition;
	private String token;
}
