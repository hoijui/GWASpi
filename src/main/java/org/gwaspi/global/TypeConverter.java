package org.gwaspi.global;

public interface TypeConverter<F, T> {

	T convert(F from);
}
