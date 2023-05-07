package com.pp.trenerpol.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "trainer")
public class Trainer extends User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "exp_years")
	private int expYears;

	@Column(name = "proteges_num")
	private int protegesNum;

	@Column(name = "bio",
			columnDefinition = "text[]")
	@Type(type = "string-array")
	private String[] bio;

	@Column(name = "city")
	private String city;

	@Column(name = "prices_from")
	private double pricesFrom;

	@Column(name = "prices_to")
	private double pricesTo;
}
