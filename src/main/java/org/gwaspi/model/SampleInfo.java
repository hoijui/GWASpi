package org.gwaspi.model;

import org.gwaspi.constants.cDBSamples;

public class SampleInfo implements Comparable<SampleInfo> {

	public static enum Sex {
		UNKNOWN,
		MALE,
		FEMALE;
	}

	public static enum Affection {
		/** Undefined */ UNKNOWN,
		/** Control */ UNAFFECTED,
		/** Case */ AFFECTED;
	}

	private int orderId;
	private String sampleId;
	private String familyId;
	private String fatherId;
	private String motherId;
	private Sex sex;
	private Affection affection;
	private String category;
	private String disease;
	private String population;
	private int age;
	private String filter;
	private String poolId;
	private int approved;
	private int status;

	public SampleInfo() {
		this.orderId = Integer.MIN_VALUE;
		this.sampleId = "0";
		this.familyId = "0";
		this.fatherId = "0";
		this.motherId = "0";
		this.sex = Sex.UNKNOWN;
		this.affection = Affection.UNKNOWN;
		this.category = "0";
		this.disease = "0";
		this.population = "0";
		this.age = 0;
		this.filter = "";
		this.poolId = "";
		this.approved = 0;
		this.status = 0;
	}

	public SampleInfo(String sampleId) {
		this();

		this.sampleId = sampleId;
	}

	public SampleInfo(
			String sampleId,
			String familyId,
			String fatherId,
			String motherId,
			Sex sex,
			Affection affection,
			String category,
			String disease,
			String population,
			int age)
	{
		this.orderId = Integer.MIN_VALUE;
		this.sampleId = sampleId;
		this.familyId = familyId;
		this.fatherId = fatherId;
		this.motherId = motherId;
		this.sex = sex;
		this.affection = affection;
		this.category = category;
		this.disease = disease;
		this.population = population;
		this.age = age;
		this.filter = "";
		this.poolId = "";
		this.approved = 0;
		this.status = 0;
	}


	public SampleInfo(
			int orderId,
			String sampleId,
			String familyId,
			String fatherId,
			String motherId,
			Sex sex,
			Affection affection,
			String category,
			String disease,
			String population,
			int age,
			String filter,
			String poolId,
			int approved,
			int status)
	{
		this.orderId = orderId;
		this.sampleId = sampleId;
		this.familyId = familyId;
		this.fatherId = fatherId;
		this.motherId = motherId;
		this.sex = sex;
		this.affection = affection;
		this.category = category;
		this.disease = disease;
		this.population = population;
		this.age = age;
		this.filter = filter;
		this.poolId = poolId;
		this.approved = approved;
		this.status = status;
	}

	@Override
	public int compareTo(SampleInfo other) {
		return getSampleId().compareTo(other.getSampleId());
	}

	public int getOrderId() {
		return orderId;
	}

	public String getSampleId() {
		return sampleId;
	}

	public String getFamilyId() {
		return familyId;
	}

	public String getFatherId() {
		return fatherId;
	}

	public String getMotherId() {
		return motherId;
	}

	public Sex getSex() {
		return sex;
	}

	/** @deprecated */
	public String getSexStr() {

		if (getSex() != null) {
			return String.valueOf(getSex().ordinal());
		} else {
			return "0";
		}
	}

	public Affection getAffection() {
		return affection;
	}

	/** @deprecated */
	public String getAffectionStr() {

		if (getAffection() != null) {
			return String.valueOf(getAffection().ordinal());
		} else {
			return "0";
		}
	}

	public String getCategory() {
		return category;
	}

	public String getDisease() {
		return disease;
	}

	public String getPopulation() {
		return population;
	}

	public int getAge() {
		return age;
	}

	public String getFilter() {
		return filter;
	}

	public String getPoolId() {
		return poolId;
	}

	public int getApproved() {
		return approved;
	}

	public int getStatus() {
		return status;
	}

	/**
	 * @deprecated
	 * @param fieldName see org.gwaspi.constants.cDBSamples#f_*
	 * @return String, Integer, Boolean or null
	 */
	public Object getField(String fieldName) {

		if (fieldName.equals(cDBSamples.f_ID)) {
			return getOrderId();
		} else if (fieldName.equals(cDBSamples.f_SAMPLE_ID)) {
			return getSampleId();
		} else if (fieldName.equals(cDBSamples.f_FAMILY_ID)) {
			return getFamilyId();
		} else if (fieldName.equals(cDBSamples.f_FATHER_ID)) {
			return getFatherId();
		} else if (fieldName.equals(cDBSamples.f_MOTHER_ID)) {
			return getMotherId();
		} else if (fieldName.equals(cDBSamples.f_SEX)) {
			return getSexStr();
		} else if (fieldName.equals(cDBSamples.f_AFFECTION)) {
			return getAffectionStr();
		} else if (fieldName.equals(cDBSamples.f_CATEGORY)) {
			return getCategory();
		} else if (fieldName.equals(cDBSamples.f_DISEASE)) {
			return getDisease();
		} else if (fieldName.equals(cDBSamples.f_POPULATION)) {
			return getPopulation();
		} else if (fieldName.equals(cDBSamples.f_AGE)) {
			return getAge();
		} else if (fieldName.equals(cDBSamples.f_FILTER)) {
			return getFilter();
		} else if (fieldName.equals(cDBSamples.f_POOL_ID)) {
			return getPoolId();
		} else if (fieldName.equals(cDBSamples.f_APPROVED)) {
			return getApproved();
		} else if (fieldName.equals(cDBSamples.f_STATUS_ID_FK)) {
			return getStatus();
		} else {
			return null;
		}
	}
}
