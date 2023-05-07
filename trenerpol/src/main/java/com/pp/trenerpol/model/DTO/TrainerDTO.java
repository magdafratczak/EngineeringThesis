package com.pp.trenerpol.model.DTO;

import lombok.Data;

@Data
public class TrainerDTO {
	private int expYears;
	private int protegesNum;
	private String[] bio;
	private String city;
	private double pricesFrom;
	private double pricesTo;
	private String sex;
	private String name;
	private String surname;
	private String phoneNumber;
}
