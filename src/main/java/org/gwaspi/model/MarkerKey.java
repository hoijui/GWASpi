package org.gwaspi.model;

/**
 * Uniquely identifies a marker within a set of markers.
 * For example, you can think of it as the type of the primary key
 * in a PLINK Flat "*.map" file.
 * The value of this key is unique within a set of markers,
 * so for example withing a single "*.map" file.
 */
public final class MarkerKey implements Comparable<MarkerKey> {

	public static final KeyFactory<MarkerKey> KEY_FACTORY = new MarkerKeyFactory();

	private String markerId;

	public MarkerKey(String markerId) {

		this.markerId = markerId;
	}

//	public MarkerKey() {
//		this("0");
//	}

	@Override
	public int compareTo(MarkerKey other) {
		return hashCode() - other.hashCode();
	}

	@Override
	public boolean equals(Object other) {

		boolean equal = false;

		if (other instanceof MarkerKey) {
			MarkerKey otherMarkerKey = (MarkerKey) other;
			equal = getMarkerId().equals(otherMarkerKey.getMarkerId());
		}

		return equal;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + (this.markerId != null ? this.markerId.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return KEY_FACTORY.encode(this);
	}

	/**
	 * Allows to parse a marker key from a single string value.
	 * @param keyStr should be of the form returned by
	 *   {@link MarkerKey#toString()}
	 * @return the parsed marker key
	 */
	public static MarkerKey valueOf(String keyStr) {
		return KEY_FACTORY.decode(keyStr);
	}

	public String getMarkerId() {
		return markerId;
	}
}
