/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.model;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A memory efficient implementation of GenotypesList.
 */
public class CompactGenotypesList extends java.util.AbstractList<byte[]> implements GenotypesList {

	public static final GenotypesListFactory FACTORY = new GenotypesListFactory() {
		@Override
		public GenotypesList createGenotypesList(Collection<byte[]> rawGenotypes) {

			// HACK We should/could probably get this list from the QA report, instead of generating it here
			Set<byte[]> possibleGenotypes = org.gwaspi.netCDF.operations.Utils.extractUniqueGenotypesOrdered(rawGenotypes);
			return new CompactGenotypesList(rawGenotypes, possibleGenotypes);
		}
	};

	private final Set<byte[]> possibleGenotypes;
	/**
	 * The (uncompressed) number of genotype entries.
	 */
	private final int size;
	/**
	 * The number of bits required to represent a single genotype
	 * in the compact form.
	 * The number of possible values might be any number from 0
	 * (all genotypes have the same value)
	 * to 5 (aa, aA, Aa, AA, 00) for the genotypes of a single marker;
	 * or from 0 to 4^2+1=17 for an arbitrary list
	 * (e.g. the genotypes of a sample).
	 * Thus this value might range from 0 bits (for 0 poss. vals.)
	 * to 5 bits (for 32 poss. vals., cause 17 is already bigger then 16,
	 * which would be 4 bits).
	 */
	private final int genotypeSize;
	private final byte compactGenotypeMask;
	/**
	 * Translates from a hash of a genotype
	 * ({@link Genotype#hashCode(byte[]})
	 * to the compact representation.
	 * Only the first {@link #genotypeSize} bits of the resulting byte matter.
	 */
	private final Map<Integer, Byte> encodingTable;
//	private final byte[] encodingTable;
	/**
	 * Translates from the compact representation
	 * to the full genotype.
	 */
//	private final Map<Byte, byte[]> decodingTable;
	private final byte[][] decodingTable;
	/**
	 * The main, compact storage.
	 */
//	private final byte[] compactGenotypes;
	private final ByteBuffer compactGenotypes;
//	private final Collection<Byte> compactGenotypes;

	private CompactGenotypesList(
			ByteBuffer compactGenotypes,
			Map<Integer, Byte> encodingTable,
			byte[][] decodingTable,
			int size,
			int genotypeSize,
			Set<byte[]> possibleGenotypes)
	{
		this.size = size;
		this.genotypeSize = genotypeSize;
		int tmpMove = 8 - this.genotypeSize;
		this.compactGenotypeMask = (byte) (0xFF << tmpMove >> tmpMove);
		this.encodingTable = encodingTable;
		this.decodingTable = decodingTable;
		this.compactGenotypes = compactGenotypes;
		this.possibleGenotypes = possibleGenotypes;
	}

	public CompactGenotypesList(Collection<byte[]> originalGenotypes,
			Collection<byte[]> possibleGenotypes)
	{
		this.size = originalGenotypes.size();
		this.possibleGenotypes = Collections.unmodifiableSet(new LinkedHashSet<byte[]>(possibleGenotypes));
		this.genotypeSize = calcGenotypeBits(possibleGenotypes.size());
		int tmpMove = 8 - this.genotypeSize;
		this.compactGenotypeMask = (byte) (0xFF << tmpMove >> tmpMove);
//		this.encodingTable = new byte[this.genotypeSize];
		this.encodingTable = new HashMap<Integer, Byte>(possibleGenotypes.size());
		this.decodingTable = new byte[possibleGenotypes.size()][2];
		int numStorageBytes = calcStorageBytes((long) originalGenotypes.size() * this.genotypeSize);
//		this.compactGenotypes = new byte[numStorageBytes];
		this.compactGenotypes = ByteBuffer.allocateDirect(numStorageBytes);
//		this.compactGenotypes = new ArrayList<Byte>(numStorageBytes);

		// create the encoding- and decoding tables
		byte compactForm = 0x00;
		for (byte[] possibleGenotype : possibleGenotypes) {
			int gtHashCode = Genotype.hashCode(possibleGenotype);
			this.encodingTable.put(gtHashCode, compactForm);
			this.decodingTable[compactForm][0] = possibleGenotype[0];
			this.decodingTable[compactForm][1] = possibleGenotype[1];
			compactForm++;
		}

		// encode all original genotypes
//		int byteIndex = 0;
		int firstBitIndex = 0;
		byte curByte = 0x00;
		for (byte[] originalGenotype : originalGenotypes) {
			int gtHashCode = Genotype.hashCode(originalGenotype);
			compactForm = this.encodingTable.get(gtHashCode);
			curByte += compactForm << firstBitIndex;
			if ((firstBitIndex + this.genotypeSize) > 7) {
				// we need the next byte too
				compactGenotypes.put(curByte);
//				byteIndex++;
				curByte = 0x00;
				curByte += compactForm << (firstBitIndex - 8);
				firstBitIndex -= 8;
			}
			firstBitIndex += this.genotypeSize;
		}
	}

	@Override
	public Set<byte[]> getPossibleGenotypes() {
		return possibleGenotypes;
	}

	/**
	 * Calculates the number of bits required to store one genotype.
	 */
	public static int calcGenotypeBits(int numPossibleGenotypes) {

		// calculate log_2(numPossibleGenotypes)
		// using the rule: log_x(a) / log_x(y) = log_y(a)
		return (int) Math.ceil(Math.log10(numPossibleGenotypes) / Math.log10(2));
	}

	/**
	 * Calculates the number of bytes required
	 * to store the given number of bits.
	 */
	public static int calcStorageBytes(long numBits) {
		return (int) ((numBits + 7) / 8);
	}


	@Override
	public byte[] get(int index) {

		long firstBitIndex = (long) index * genotypeSize;
		int firstByteIndex = (int) (firstBitIndex / 8);
		byte firstBitLocalIndex = (byte) (firstBitIndex % 8);
		byte theByte = compactGenotypes.get(firstByteIndex);
		byte compactValue = (byte) (theByte >> firstBitLocalIndex); // XXX Java fail!
		if (firstBitLocalIndex + genotypeSize > 7) {
			theByte = compactGenotypes.get(firstByteIndex + 1);
			compactValue += theByte >> (firstBitLocalIndex - 8);
		}
		compactValue &= compactGenotypeMask;

		return Arrays.copyOf(decodingTable[compactValue], 2);
	}

	@Override
	public int size() {
		return size;
	}
}
