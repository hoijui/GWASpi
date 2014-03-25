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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gwaspi.operations.NetCdfUtils;

/**
 * A memory efficient implementation of GenotypesList.
 */
public class CompactGenotypesList extends AbstractList<byte[]> implements GenotypesList, Serializable {

	public static final GenotypesListFactory FACTORY = new GenotypesListFactory() {
		@Override
		public GenotypesList extract(List<byte[]> rawGenotypes) {

			// HACK We should/could probably get this list from the QA report, instead of generating it here
			Set<byte[]> possibleGenotypes = NetCdfUtils.extractUniqueGenotypesOrdered(rawGenotypes);
			return new CompactGenotypesList(rawGenotypes, possibleGenotypes);
		}
	};

	public static class SelectiveIndicesGenotypesListFactory implements GenotypesListFactory {

		private final List<Integer> gtIndicesToSelect;

		public SelectiveIndicesGenotypesListFactory(List<Integer> gtIndicesToSelect) {

			this.gtIndicesToSelect = gtIndicesToSelect;
		}

		@Override
		public GenotypesList extract(List<byte[]> rawGenotypes) {

			final List<byte[]> filteredGenotypes = NetCdfUtils.includeOnlyIndices(rawGenotypes, gtIndicesToSelect);

			// HACK We should/could probably get this list from the QA report, instead of generating it here
			Set<byte[]> possibleGenotypes = NetCdfUtils.extractUniqueGenotypesOrdered(filteredGenotypes);
			return new CompactGenotypesList(filteredGenotypes, possibleGenotypes);
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
	private transient ByteBuffer compactGenotypes;
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
		final byte tmpMove = (byte) (8 - this.genotypeSize);
		this.compactGenotypeMask = calcByteBitMask(tmpMove);
		this.encodingTable = encodingTable;
		this.decodingTable = decodingTable;
		this.compactGenotypes = compactGenotypes;
		this.possibleGenotypes = possibleGenotypes;
	}

	public CompactGenotypesList(
			Collection<byte[]> originalGenotypes,
			Collection<byte[]> possibleGenotypes)
	{
		this.size = originalGenotypes.size();
		this.possibleGenotypes = Collections.unmodifiableSet(new LinkedHashSet<byte[]>(possibleGenotypes));
		this.genotypeSize = calcGenotypeBits(possibleGenotypes.size());
//		final byte tmpMove = (byte) (8 - this.genotypeSize);
		this.compactGenotypeMask = calcByteBitMask((byte) genotypeSize);
//		this.encodingTable = new byte[this.genotypeSize];
		this.encodingTable = new HashMap<Integer, Byte>(possibleGenotypes.size());
		this.decodingTable = new byte[possibleGenotypes.size()][2];
		int numStorageBytes = calcStorageBytes((long) originalGenotypes.size() * this.genotypeSize);
		if (numStorageBytes == 0) {
			// We always have to store at least one value.
			// This can be seen as a constant,
			// if all genotypes have the same value.
			numStorageBytes = 1;
		}
//		this.compactGenotypes = new byte[numStorageBytes];
		this.compactGenotypes = ByteBuffer.allocateDirect(numStorageBytes);
//		this.compactGenotypes = new ArrayList<Byte>(numStorageBytes);

		// create the encoding and decoding tables
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
		// Indicates whether there is data in curByte
		// that is not yet stored in compactGenotypes.
		boolean unstoredData = false;
		for (byte[] originalGenotype : originalGenotypes) {
			int gtHashCode = Genotype.hashCode(originalGenotype);
			compactForm = this.encodingTable.get(gtHashCode);
			curByte += compactForm << firstBitIndex;
			unstoredData = true;

			if ((firstBitIndex + this.genotypeSize) > 7) {
				// we need the next byte too
				compactGenotypes.put(curByte);
//				byteIndex++;
				curByte = 0x00;
				curByte += compactForm << (firstBitIndex - 8);
				firstBitIndex -= 8;
				unstoredData = false;
			}
			firstBitIndex += this.genotypeSize;
		}
		if (unstoredData) {
			compactGenotypes.put(curByte);
			unstoredData = false;
		}
	}

	/**
	 * We need to override this for Java serialization to work,
	 * because ByteBuffer (the type used for {@link #compactGenotypes})
	 * is not serializable.
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {

		out.defaultWriteObject();
		final int numStorageBytes = compactGenotypes.capacity();
		out.write(numStorageBytes);
		if (compactGenotypes.hasArray()) {
			out.write(compactGenotypes.array());
		} else {
			final byte[] compactGenotypesRaw = new byte[numStorageBytes];
			final int currentPosition = compactGenotypes.position();
			compactGenotypes.rewind();
			compactGenotypes.get(compactGenotypesRaw);
			compactGenotypes.position(currentPosition);
			out.write(compactGenotypesRaw);
		}
	}

	/**
	 * We need to override this for Java serialization to work,
	 * because ByteBuffer (the type used for {@link #compactGenotypes})
	 * is not serializable.
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

		in.defaultReadObject();
		// now we are a "live" object again, so let's run rebuild and start
		final int numStorageBytes = in.readInt();
		final byte[] compactGenotypesRaw = (byte[]) in.readObject();
		compactGenotypes = ByteBuffer.allocateDirect(numStorageBytes);
		compactGenotypes.put(compactGenotypesRaw);
	}

	public static byte calcByteBitMask(byte numBits) {
		return (byte) ~((0xFF >>> numBits) << numBits);
	}

	public static String byteToBitString(byte value) {

		// make sure we get all the leading zeros
		final int bigValue = 0x100 | value;

		// convert to binary string
		String valueBitsStr = Integer.toBinaryString(bigValue);

		// remove all but the last 8 bits representations
		// (integer is 32bit, byte is 8bit)
		valueBitsStr = valueBitsStr.substring(valueBitsStr.length() - Byte.SIZE);

		return valueBitsStr;
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
		return (int) ((numBits + 7L) / 8L);
	}


	@Override
	public byte[] get(int index) {

long firstBitIndex = -1;
int firstByteIndex = -1;
byte firstBitLocalIndex = -1;
byte storedByte = -1;
byte compactValue = -1;
try {
		firstBitIndex = (long) index * genotypeSize;
		firstByteIndex = (int) (firstBitIndex / 8);
		firstBitLocalIndex = (byte) (firstBitIndex % 8);
		storedByte = compactGenotypes.get(firstByteIndex);
		compactValue = (byte) ((storedByte & 0xFF) >>> firstBitLocalIndex); // XXX Java fail!
		if ((firstBitLocalIndex + genotypeSize - 1) > 7) {
//System.err.println("UUU test_1: " + byteToBitString((byte) -47) + " (" + ((byte) -47) + ")");
//System.err.println("UUU test_2: " + byteToBitString((byte) (-47 >> 5)) + " (" + ((byte) (-47 >> 5)) + ")");
//System.err.println("UUU test_3: " + byteToBitString((byte) (-47 >>> 5)) + " (" + ((byte) (-47 >>> 5)) + ")");

//System.err.println("VVV storedByte " + byteToBitString(storedByte) + " (" + storedByte + ")");
			storedByte = compactGenotypes.get(firstByteIndex + 1);
//System.err.println("VVV compactValue " + byteToBitString(compactValue) + " (" + compactValue + ")");
			compactValue += (storedByte & 0xFF) >>> (firstBitLocalIndex - 8);
		}
//System.err.println("WWW compactValue " + byteToBitString(compactValue) + " (" + compactValue + ")");
		compactValue &= compactGenotypeMask;

		return Arrays.copyOf(decodingTable[compactValue], 2);
} catch (Exception ex) {
	System.err.println("XXX failed to get element " + index + " from list of size " + size);
	System.err.println("XXX firstBitIndex " + firstBitIndex);
	System.err.println("XXX firstByteIndex " + firstByteIndex);
	System.err.println("XXX firstBitLocalIndex " + firstBitLocalIndex);
	System.err.println("XXX storedByte " + byteToBitString(storedByte) + " (" + storedByte + ")");
	System.err.println("XXX compactGenotypes " + compactGenotypes.capacity());
	System.err.println("XXX decodingTable " + decodingTable.length);
	System.err.println("XXX compactValue " + byteToBitString(compactValue) + " (" + compactValue + ")");
	System.err.println("XXX genotypeSize " + genotypeSize);
	System.err.println("XXX compactGenotypeMask " + byteToBitString(compactGenotypeMask) + " (" + compactGenotypeMask + ")");
	ex.printStackTrace();
	throw new RuntimeException(ex);
}
	}

	@Override
	public int size() {
		return size;
	}
}
