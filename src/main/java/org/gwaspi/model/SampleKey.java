package org.gwaspi.model;

/**
 * Uniquely identifies a sample within a set of samples.
 * For example, you can think of it as the type of the primary key
 * in a PLINK Flat "*.ped" file.
 * The (combined) value of this key is unique within a set of samples,
 * so for example within a single "*.ped" file.
 *
 * @author hoijui
 */
public final class SampleKey implements Comparable<SampleKey> {

	public static final KeyFactory<SampleKey> KEY_FACTORY = new SampleKeyFactory();
	/**
	 * This one is used as the default value if no family-ID is available.
	 * This is the case in the Affymetrix and Beagle formats, for example.
	 */
	public static final String FAMILY_ID_NONE = "0";

	private String sampleId;
	private String familyId;

	public SampleKey(String sampleId, String familyId) {

		this.sampleId = sampleId;
		this.familyId = familyId;
	}

//	public SampleKey() {
//		this("0", FAMILY_ID_NONE);
//	}

	@Override
	public int compareTo(SampleKey other) {
		return hashCode() - other.hashCode();
	}

	@Override
	public boolean equals(Object other) {

		boolean equal = false;

		if (other instanceof SampleKey) {
			SampleKey otherSampleKey = (SampleKey) other;
			equal = getFamilyId().equals(otherSampleKey.getFamilyId());
			equal = equal && getSampleId().equals(otherSampleKey.getSampleId());
		}

		return equal;
	}

	@Override
	public int hashCode() {

		int hash = 7;
		hash = 31 * hash + (this.sampleId != null ? this.sampleId.hashCode() : 0);
		hash = 31 * hash + (this.familyId != null ? this.familyId.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return KEY_FACTORY.encode(this);
	}

	/**
	 * Allows to parse a sample key from a single string value.
	 * @param keyStr should be of the form returned by
	 *   {@link SampleKey#toString()}
	 * @return the parsed sample key
	 */
	public static SampleKey valueOf(String keyStr) {
		return KEY_FACTORY.decode(keyStr);
	}

	public String getSampleId() {
		return sampleId;
	}

	public String getFamilyId() {
		return familyId;
	}
}
