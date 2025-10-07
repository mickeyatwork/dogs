package com.test.dogs.api.rest.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.sql.Date;

@Entity
public class DogEntity {

	@Id
	private Long id;
	private String name;
	private String breed;
	private String supplier;
	private Integer badgeID;
	private String gender;
	private Date birthDate;
	private Date dateAcquired;
	private String status;
	private Date leavingDate;
	private String leavingReason;
	private String kennelingCharacteristics;
	private Date dateDeleted;

	public DogEntity() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBreed() {
		return breed;
	}

	public void setBreed(String breed) {
		this.breed = breed;
	}

	public String getSupplier() {
		return supplier;
	}

	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}

	public Integer getBadgeID() {
		return badgeID;
	}

	public void setBadgeID(Integer badgeID) {
		this.badgeID = badgeID;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public java.sql.Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public java.sql.Date getDateAcquired() {
		return dateAcquired;
	}

	public void setDateAcquired(Date dateAcquired) {
		this.dateAcquired = dateAcquired;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public java.sql.Date getLeavingDate() {
		return leavingDate;
	}

	public void setLeavingDate(Date leavingDate) {
		this.leavingDate = leavingDate;
	}

	public String getLeavingReason() {
		return leavingReason;
	}

	public void setLeavingReason(String leavingReason) {
		this.leavingReason = leavingReason;
	}

	public String getKennelingCharacteristics() {
		return kennelingCharacteristics;
	}

	public void setKennelingCharacteristics(String kennelingCharacteristics) {
		this.kennelingCharacteristics = kennelingCharacteristics;
	}

	public Date getDateDeleted() {
		return dateDeleted;
	}

	public void setDateDeleted(Date dateDeleted) {
		this.dateDeleted = dateDeleted;
	}
}
