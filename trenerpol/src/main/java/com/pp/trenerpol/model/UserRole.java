package com.pp.trenerpol.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "user_role")
public class UserRole {
	@Column(name = "user_id")
	private Long userId;

	@Id
	@Column(name = "role_id")
	private Long roleId;

}
