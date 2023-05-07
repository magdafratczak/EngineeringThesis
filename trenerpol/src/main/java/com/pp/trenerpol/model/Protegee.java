package com.pp.trenerpol.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "protegee")
public class Protegee extends User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "birth_date")
	@Temporal(TemporalType.DATE)
	private java.util.Date birthDate;

	@Column(name = "height")
	private int height;

}
