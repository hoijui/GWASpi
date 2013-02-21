package org.gwaspi.model;

public final class MarkerKeyFactory implements KeyFactory<MarkerKey> {

	@Override
	public String encode(MarkerKey key) {
		return key.getMarkerId();
	}

	@Override
	public MarkerKey decode(String keyStr) {
		// TODO throw some type of runtime exception if keyStr does not conform strictly to a valid key
		return new MarkerKey(keyStr);
	}
}
