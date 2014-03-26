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

package org.gwaspi.operations;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.gwaspi.model.ChromosomesInfosSource;
import org.gwaspi.model.ChromosomesKeysSource;
import org.gwaspi.model.DataSetKey;
import org.gwaspi.model.DataSetSource;
import org.gwaspi.model.MarkersGenotypesSource;
import org.gwaspi.model.MarkersKeysSource;
import org.gwaspi.model.MarkersMetadataSource;
import org.gwaspi.model.MatrixKey;
import org.gwaspi.model.MatrixMetadata;
import org.gwaspi.model.OperationKey;
import org.gwaspi.model.OperationMetadata;
import org.gwaspi.model.OperationsList;
import org.gwaspi.model.SamplesGenotypesSource;
import org.gwaspi.model.SamplesInfosSource;
import org.gwaspi.model.SamplesKeysSource;
import org.gwaspi.netCDF.matrices.MatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOperationDataSet<ET> implements OperationDataSet<ET> {

	protected static final int DEFAULT_ENTRIES_WRITE_BUFFER_SIZE_MARKERS = 10;
	protected static final int DEFAULT_ENTRIES_WRITE_BUFFER_SIZE_SAMPLES = 1;

	private final Logger log = LoggerFactory.getLogger(AbstractOperationDataSet.class);

	private final boolean markersOperationSet;
	private final MatrixKey origin;
	private final DataSetKey parent;
	private Integer numMarkers;
	private Integer numSamples;
	private Integer numChromosomes;
//	private Boolean useAllMarkersFromParent;
//	private Boolean useAllSamplesFromParent;
//	private Boolean useAllChromosomesFromParent;
	private OperationMetadata operationMetadata;
	private OperationKey operationKey;
	private final Queue<ET> writeBuffer;
	private int alreadyWritten;
	private int entriesWriteBufferSize;
	private final int numEntriesToLog;

	public AbstractOperationDataSet(
			boolean markersOperationSet,
			MatrixKey origin,
			DataSetKey parent,
			OperationKey operationKey,
			int entriesWriteBufferSize)
	{
		this.origin = origin;
//		this.origin = (operationKey == null) ? null : operationKey.getParentMatrixKey();
		this.parent = parent;
		this.operationKey = operationKey;
		this.markersOperationSet = markersOperationSet;
//		this.rdMatrixKey = (operationKey == null) ? null : operationKey.getParentMatrixKey();
//		this.rdOperationKey = null;
		this.numMarkers = null;
		this.numSamples = null;
		this.numChromosomes = null;
//		this.useAllMarkersFromParent = null;
//		this.useAllSamplesFromParent = null;
//		this.useAllChromosomesFromParent = null;
		this.operationMetadata = null;
		this.writeBuffer = new LinkedList<ET>();
		this.alreadyWritten = 0;
		this.entriesWriteBufferSize = entriesWriteBufferSize;
		this.numEntriesToLog = markersOperationSet ? 2000 : 100;
	}

	public AbstractOperationDataSet(
			boolean markersOperationSet,
			MatrixKey origin,
			DataSetKey parent,
			OperationKey operationKey)
	{
		this(markersOperationSet, origin, parent, operationKey, getDefaultEntriesWriteBufferSize(markersOperationSet));
	}

	public AbstractOperationDataSet(
			boolean markersOperationSet,
			MatrixKey origin,
			DataSetKey parent)
	{
		this(markersOperationSet, origin, parent, null);
	}

	/**
	 * TODO revise this, for performance reasons.
	 * @param markersOperationSet
	 * @return
	 */
	protected static int getDefaultEntriesWriteBufferSize(boolean markersOperationSet) {

		return markersOperationSet
				? DEFAULT_ENTRIES_WRITE_BUFFER_SIZE_MARKERS
				: DEFAULT_ENTRIES_WRITE_BUFFER_SIZE_SAMPLES;
	}

	protected MatrixKey getOrigin() {
		return origin;
	}

	protected DataSetKey getParent() {
		return parent;
	}

	@Override
	public boolean isMarkersOperationSet() {
		return markersOperationSet;
	}

	protected abstract OperationMetadata createOperationMetadata() throws IOException;

	protected final OperationMetadata getOperationMetadata() throws IOException {

		if (operationMetadata == null) {
			if (operationKey == null) {
				operationMetadata = createOperationMetadata();
				operationKey = OperationsList.insertOperation(operationMetadata);
			} else {
				operationMetadata = OperationsList.getOperationMetadata(operationKey);
			}
		}

		return operationMetadata;
	}

	@Override
	public DataSetSource getParentDataSetSource() throws IOException {

		final DataSetKey parent = getParent();
		final DataSetSource dataSetSource;
		if (parent.isMatrix()) {
			dataSetSource = MatrixFactory.generateMatrixDataSetSource(parent.getMatrixParent());
		} else {
			dataSetSource = OperationManager.generateOperationDataSet(parent.getOperationParent());
		}

		return dataSetSource;
	}

	@Override
	public DataSetSource getOriginDataSetSource() throws IOException {
		return MatrixFactory.generateMatrixDataSetSource(getOrigin());
	}

	@Override
	public final MatrixMetadata getMatrixMetadata() throws IOException {

		throw new UnsupportedOperationException("Not supported.");
		// maybe just leave like this, cause this method should not be used by OperationDataSet "users"
	}

	protected int getEntriesWriteBufferSize() {
		return entriesWriteBufferSize;
	}

//	public void setReadMatrixKey(MatrixKey rdMatrixKey) {
//		this.rdMatrixKey = rdMatrixKey;
//	}
//
//	protected MatrixKey getReadMatrixKey() {
//		return rdMatrixKey;
//	}
//
//	public void setReadOperationKey(OperationKey rdOperationKey) {
//
//		this.rdMatrixKey = rdOperationKey.getParentMatrixKey();
//		this.rdOperationKey = rdOperationKey;
//	}
//
//	protected OperationKey getReadOperationKey() {
//		return rdOperationKey;
//	}

	public int getNumEntries() throws IOException {
		return isMarkersOperationSet() ? getNumMarkers() : getNumSamples();
	}

	@Override
	public void setNumMarkers(int numMarkers) throws IOException {

		this.numMarkers = numMarkers;

		setUseAllMarkersFromParent(numMarkers == getParentDataSetSource().getNumMarkers());
	}

	protected abstract int getNumMarkersRaw() throws IOException;

	@Override
	public final int getNumMarkers() throws IOException {

		if (numMarkers == null) {
			if (getUseAllMarkersFromParent()) {
				setNumMarkers(getParentDataSetSource().getNumMarkers());
			} else {
				setNumMarkers(getNumMarkersRaw());
			}
		}

		return numMarkers;
	}

	/**
	 * @param useAll
	 *   if true, we will use all the markers from the parent operation,
	 *   if this operation has one, or from the parent matrix otherwise.
	 *   If false, we will store/retrieve/manage a separate list of markers
	 *   for this operation.
	 * @throws IOException
	 */
	protected abstract void setUseAllMarkersFromParent(boolean useAll) throws IOException;

	protected abstract boolean getUseAllMarkersFromParent() throws IOException;

	@Override
	public void setNumSamples(int numSamples) throws IOException {

		this.numSamples = numSamples;

		setUseAllSamplesFromParent(numSamples == getParentDataSetSource().getNumSamples());
	}

	protected abstract int getNumSamplesRaw() throws IOException;

	@Override
	public final int getNumSamples() throws IOException {

		if (numSamples == null) {
			if (getUseAllSamplesFromParent()) {
				numSamples = getParentDataSetSource().getNumSamples();
			} else {
				numSamples = getNumSamplesRaw();
			}
		}

		return numSamples;
	}

	/**
	 * @param useAll
	 *   if true, we will use all the samples from the parent operation,
	 *   if this operation has one, or from the parent matrix otherwise.
	 *   If false, we will store/retrieve/manage a separate list of samples
	 *   for this operation.
	 * @throws IOException
	 */
	protected abstract void setUseAllSamplesFromParent(boolean useAll) throws IOException;

	protected abstract boolean getUseAllSamplesFromParent() throws IOException;

	@Override
	public void setNumChromosomes(int numChromosomes) throws IOException {

		this.numChromosomes = numChromosomes;

		setUseAllChromosomesFromParent(numChromosomes == getParentDataSetSource().getNumChromosomes());
	}

	protected abstract int getNumChromosomesRaw() throws IOException;

	@Override
	public final int getNumChromosomes() throws IOException {

		if (numChromosomes == null) {
			if (getUseAllChromosomesFromParent()) {
				numChromosomes = getParentDataSetSource().getNumChromosomes();
			} else {
				numChromosomes = getNumChromosomesRaw();
			}
		}

		return numChromosomes;
	}

	/**
	 * @param useAll
	 *   if true, we will use all the chromosomes from the parent operation,
	 *   if this operation has one, or from the parent matrix otherwise.
	 *   If false, we will store/retrieve/manage a separate list of chromosomes
	 *   for this operation.
	 * @throws IOException
	 */
	protected abstract void setUseAllChromosomesFromParent(boolean useAll) throws IOException;

	protected abstract boolean getUseAllChromosomesFromParent() throws IOException;

	public OperationKey getOperationKey() {
		return operationKey;
	}

	protected abstract SamplesKeysSource getSamplesKeysSourceRaw() throws IOException;

	@Override
	public final SamplesKeysSource getSamplesKeysSource() throws IOException {

		if (getUseAllSamplesFromParent()) {
			return getParentDataSetSource().getSamplesKeysSource();
		} else {
			return getSamplesKeysSourceRaw();
		}
	}

	protected abstract SamplesInfosSource getSamplesInfosSourceRaw() throws IOException;

	@Override
	public final SamplesInfosSource getSamplesInfosSource() throws IOException {

		if (getUseAllSamplesFromParent()) {
			return getParentDataSetSource().getSamplesInfosSource();
		} else {
			return getSamplesInfosSourceRaw();
		}
	}

	protected abstract MarkersKeysSource getMarkersKeysSourceRaw() throws IOException;

	@Override
	public final MarkersKeysSource getMarkersKeysSource() throws IOException {

		if (getUseAllMarkersFromParent()) {
			return getParentDataSetSource().getMarkersKeysSource();
		} else {
			return getMarkersKeysSourceRaw();
		}
	}

	protected abstract MarkersMetadataSource getMarkersMetadatasSourceRaw() throws IOException;

	@Override
	public final MarkersMetadataSource getMarkersMetadatasSource() throws IOException {

		if (getUseAllMarkersFromParent()) {
			return getParentDataSetSource().getMarkersMetadatasSource();
		} else {
			return getMarkersMetadatasSourceRaw();
		}
	}

	protected abstract ChromosomesKeysSource getChromosomesKeysSourceRaw() throws IOException;

	@Override
	public final ChromosomesKeysSource getChromosomesKeysSource() throws IOException {

		if (getUseAllChromosomesFromParent()) {
			return getParentDataSetSource().getChromosomesKeysSource();
		} else {
			return getChromosomesKeysSourceRaw();
		}
	}

	protected abstract ChromosomesInfosSource getChromosomesInfosSourceRaw() throws IOException;

	@Override
	public final ChromosomesInfosSource getChromosomesInfosSource() throws IOException {

		if (getUseAllChromosomesFromParent()) {
			return getParentDataSetSource().getChromosomesInfosSource();
		} else {
			return getChromosomesInfosSourceRaw();
		}
	}

	protected abstract MarkersGenotypesSource getMarkersGenotypesSourceRaw() throws IOException;

	@Override
	public final MarkersGenotypesSource getMarkersGenotypesSource() throws IOException {

		if (getUseAllSamplesFromParent() && getUseAllMarkersFromParent()) {
			return getParentDataSetSource().getMarkersGenotypesSource();
		} else {
			return getMarkersGenotypesSourceRaw();
		}
	}

	protected abstract SamplesGenotypesSource getSamplesGenotypesSourceRaw() throws IOException;

	@Override
	public final SamplesGenotypesSource getSamplesGenotypesSource() throws IOException {

		if (getUseAllSamplesFromParent() && getUseAllMarkersFromParent()) {
			return getParentDataSetSource().getSamplesGenotypesSource();
		} else {
			return getSamplesGenotypesSourceRaw();
		}
	}

//	@Override
	public void addEntry(ET entry) throws IOException {

		writeBuffer.add(entry);

		if (writeBuffer.size() >= entriesWriteBufferSize) {
			writeCurrentEntriesBuffer();
		}
	}

	protected void writeCurrentEntriesBuffer() throws IOException {
		alreadyWritten = writeEntriesBuffer(alreadyWritten, writeBuffer, "");
	}

	protected int writeEntriesBuffer(final int alreadyWritten, final Queue<ET> writeBuffer, String additionalWriteLogSpecifier) throws IOException {

		if (writeBuffer.isEmpty()) { // this should not be required
//			throw new IllegalStateException("We have already written all entries (already written: " + alreadyWritten + ", num entries: " + getNumEntries() + ", write buffer size: " + writeBuffer.size() + ")");
			return alreadyWritten;
		}
		if (alreadyWritten >= getNumEntries()) { // this should not be required
			throw new IllegalStateException("We have already written all entries (already written: " + alreadyWritten + ", num entries: " + getNumEntries() + ", write buffer size: " + writeBuffer.size() + ")");
		}

		final int numWriting = writeBuffer.size();
		writeEntries(alreadyWritten, writeBuffer);
		final int nowWritten = alreadyWritten + numWriting;
		writeBuffer.clear();

		if ((alreadyWritten == 0) // the first entries
				|| ((nowWritten / numEntriesToLog) != ((nowWritten - numWriting) / numEntriesToLog)) // approximately numEntriesToLog written since last log
				|| (nowWritten == getNumEntries())) // the final entries
		{
			log.info("Processed {}{}: {} / {}",
					additionalWriteLogSpecifier,
					isMarkersOperationSet() ? "markers" : "samples",
					nowWritten,
					getNumEntries());
		}

		return nowWritten;
	}

	@Override
	public void finnishWriting() throws IOException {

		writeCurrentEntriesBuffer();

		// this way, we will write the metadata into the DB
		getOperationMetadata();
	}

	protected abstract void writeEntries(int alreadyWritten, Queue<ET> writeBuffer) throws IOException;

	@Override
	public List<ET> getEntries() throws IOException {
		return getEntries(-1, -1);
	}
}
