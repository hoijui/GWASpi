package org.gwaspi.model;

/**
 * Allows to convert <code>Key</code>s of type <code>K</code>
 * from and to <code>String</code>s.
 */
public interface KeyFactory<K> {

	/**
	 * Allows to parse a key from a single string value.
	 * @param keyStr should be of the form returned by
	 *   {@link #encode(K)}
	 * @return the parsed key
	 */
	K decode(String keyStr);

	/**
	 * Allows to convert a key into a single string representation.
	 * @param key a valid key
	 * @return the string encoded key
	 */
	String encode(K key);
}
