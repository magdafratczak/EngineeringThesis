package com.pp.trenerpol.model.DTO;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UserDTO {
	@NotNull
	@Size(min = 1, message = "{Size.userDto.firstName}")
	private String firstName;

	@NotNull
	@Size(min = 1, message = "{Size.userDto.lastName}")
	private String lastName;

	private String password;

	@NotNull
	@Size(min = 1)
	private String matchingPassword;

	@NotNull
	@Size(min = 1, message = "{Size.userDto.email}")
	private String email;

	private boolean isUsing2FA;

	private String phoneNumber;

}
