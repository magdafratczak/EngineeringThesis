package com.pp.trenerpol.model;

import com.sun.istack.NotNull;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;

@Entity
@Data
@Table(name = "trainer_schedule")
@TypeDefs({@TypeDef(name = "string-array", typeClass = StringArrayType.class)})
public class TrainerSchedule {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@NotNull
	@Column(name = "trainer_id")
	private Long trainerId;

	@Column(name = "modified_date")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date modifiedDate;

	@Column(name = "monday",
			columnDefinition = "text[]")
	@Type(type = "string-array")
	private String[] monday;

	@Column(name = "tuesday",
			columnDefinition = "text[]")
	@Type(type = "string-array")
	private String[] tuesday;

	@Column(name = "wednesday",
			columnDefinition = "text[]")
	@Type(type = "string-array")
	private String[] wednesday;

	@Column(name = "thursday",
			columnDefinition = "text[]")
	@Type(type = "string-array")
	private String[] thursday;

	@Column(name = "friday",
			columnDefinition = "text[]")
	@Type(type = "string-array")
	private String[] friday;

	@Column(name = "saturday",
			columnDefinition = "text[]")
	@Type(type = "string-array")
	private String[] saturday;

	@Column(name = "sunday",
			columnDefinition = "text[]")
	@Type(type = "string-array")
	private String[] sunday;

}
