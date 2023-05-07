package com.pp.trenerpol.model.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Data
public class SignInDTO {
	@NotBlank(message = "Please provide email!")
	private String email;
	@NotBlank(message = "Please provide password!")
	private String password;
}
