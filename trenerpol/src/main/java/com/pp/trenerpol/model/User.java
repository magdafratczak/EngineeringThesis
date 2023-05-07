package com.pp.trenerpol.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.Collection;
import java.util.Set;

@Entity
@Data
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User implements UserDetails {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "phone_num")
	private String phoneNum;

	@Column(name = "register_date")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date registerDate;

	@Column(name = "sex")
	private String sex;

	@Column(name = "surname")
	private String surname;

	@Column(name = "type")
	private String type;

	@Email
	@Column(name = "email", unique = true)
	private String email;

	@Column(name = "password")
	@JsonIgnore
	private String password;

	@Column(name = "enabled2fa")
	private boolean enabled2fa;

	@Column(name = "secret")
	@JsonIgnore
	private String secret;

	@Column(name = "enabled")
	private boolean enabled;

	@Column(name = "deletion_date")
	private java.util.Date deletionDate;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JsonIgnore
	@JoinTable(name = "user_role",
			joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
	private Set<Role> roles;

	public User() {
		super();
		this.enabled = false;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
}
