package com.pp.trenerpol.model.DTO;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class ProtegeeDTO {
	private String sex;
	private String name;
	private String surname;
	private String phoneNumber;
	private int height;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date birthDay;
}
