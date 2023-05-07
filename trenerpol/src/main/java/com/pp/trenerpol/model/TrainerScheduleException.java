package com.pp.trenerpol.model;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;

@Data
@Entity
@Table(name = "trainer_schedule_exception")
@TypeDefs({@TypeDef(name = "string-array", typeClass = StringArrayType.class)})
public class TrainerScheduleException {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "trainer_id")
	private Long trainerId;

	@Column(name = "modified_date")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date modifiedDate;

	@Column(name = "date")
	@Temporal(TemporalType.DATE)
	private java.util.Date date;

	@Column(name = "hours",
			columnDefinition = "text[]")
	@Type(type = "string-array")
	private String[] hour;

}
