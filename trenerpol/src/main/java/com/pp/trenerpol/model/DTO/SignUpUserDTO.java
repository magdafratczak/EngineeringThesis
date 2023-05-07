package com.pp.trenerpol.model.DTO;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SignUpUserDTO {

	@NotBlank(message = "Please provide user name!")
	private String name;
	@NotBlank(message = "Please provide user surname!")
	private String surname;
	@NotBlank(message = "Please provide user email!")
	private String email;
}
