package org.gwaspi.global;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public interface TypeConverter<F, T> {

	T convert(F from);
}
