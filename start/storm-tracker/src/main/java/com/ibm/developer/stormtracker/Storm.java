package com.ibm.developer.stormtracker;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "Storms")
public class Storm {

	@Id
	@GeneratedValue(generator = "storms_id_generator")
	@SequenceGenerator(name = "storms_id_generator", allocationSize = 1, initialValue = 10)
	private long id;
	private String startDate;
	private String endDate;
	private String startLocation;
	private String endLocation;
	private String type;
	private int intensity;

	Storm() {
	}

	public Storm(String startDate, String endDate, String startLocation, String endLocation, String type,
			int intensity) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.startLocation = startLocation;
		this.endLocation = endLocation;
		this.type = type;
		this.intensity = intensity;
	}

	public long getId() {
		return id;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public String getStartLocation() {
		return startLocation;
	}

	public String getEndLocation() {
		return endLocation;
	}

	public String getType() {
		return type;
	}

	public int getIntensity() {
		return intensity;
	}

}
