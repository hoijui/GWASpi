///*
// * Copyright (C) 2014 Universitat Pompeu Fabra
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//package org.gwaspi.operations.combi;
//
//import java.util.AbstractList;
//import java.util.List;
//
//class MarkerGenotypesEncoderWrapper extends AbstractList<List<Float>> {
//
//	private final MarkerGenotypesEncoder markerGenotypesEncoder;
//	private int curChunkIndex;
//	private Float[][] curChunk;
//
//	public MarkerGenotypesEncoderWrapper(MarkerGenotypesEncoder markerGenotypesEncoder) {
//
//		this.markerGenotypesEncoder = markerGenotypesEncoder;
////		if (markerGenotypesEncoder.getMaxChunkSize() != 1){}
//		this.curChunkIndex = -1;
//		this.curChunk = null;
//	}
//
//	@Override
//	public List<Float> get(int index) {
//
//		final int requiredChunkIndex = index / markerGenotypesEncoder.getMaxChunkSize();
//		final int innerIndex = index % markerGenotypesEncoder.getMaxChunkSize();
//
//		if (requiredChunkIndex != curChunkIndex) {
//			curChunk = markerGenotypesEncoder.get(requiredChunkIndex);
//			curChunkIndex = requiredChunkIndex;
//		}
//
//		List<Float>
//		return curChunk[][innerIndex] // NOooooo!!
//	}
//
//	@Override
//	public int size() {
//		return markerGenotypesEncoder.getNumFeatures();
//	}
//}
