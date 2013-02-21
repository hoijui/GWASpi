package org.gwaspi.model;

public final class SampleKeyFactory implements KeyFactory<SampleKey> {

	@Override
	public String encode(SampleKey key) {
		return key.getSampleId() + " " + key.getFamilyId();
	}

	@Override
	public SampleKey decode(String keyStr) {
		SampleKey key = null;

		String[] parts = keyStr.split(" ", 3);
		if (parts.length == 2) {
			key = new SampleKey(parts[0], parts[1]);
		}

		// TODO throw some type of runtime exception if keyStr does not conform strictly to a valid key
		return key;
	}
}
