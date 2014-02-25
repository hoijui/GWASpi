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
import java.util.List;

/**
 * TODO
 */
public interface MarkersMetadataSource extends List<MarkerMetadata> {

	MarkersKeysSource getKeysSource() throws IOException;

	List<String> getMarkerIds() throws IOException;
	List<String> getRsIds() throws IOException;
	List<String> getChromosomes() throws IOException;
	List<Integer> getPositions() throws IOException;
	List<String> getAlleles() throws IOException;
	List<String> getStrands() throws IOException;

////	List<String> getMarkerIds(int from, int to) throws IOException;
//	List<String> getRsIds(int from, int to) throws IOException;
//	List<String> getChromosomes(int from, int to) throws IOException;
//	List<Integer> getPositions(int from, int to) throws IOException;
//	List<String> getAlleles(int from, int to) throws IOException;
//	List<String> getStrands(int from, int to) throws IOException;
}
