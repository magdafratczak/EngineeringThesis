package com.pp.trenerpol.model.DTO;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SignInWithTokenDTO {
	@NotBlank(message = "Please provide email!")
	private String email;
	@NotBlank(message = "Please provide GA code!")
	private String code;
}
