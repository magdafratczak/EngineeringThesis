package com.pp.trenerpol.model.DTO;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SignUpDTO {
	@NotBlank(message = "Please provide name!")
	private String name;
	@NotBlank(message = "Please provide surname!")
	private String surname;
	@NotBlank(message = "Please provide email!")
	private String email;
	@NotBlank(message = "Please provide password!")
	private String password;
	@NotBlank(message = "Please repeat password!")
	private String repeatPassword;
	private String type;
}
