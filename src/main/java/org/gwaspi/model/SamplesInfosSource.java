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
import org.gwaspi.model.SampleInfo.Affection;
import org.gwaspi.model.SampleInfo.Sex;

/**
 * TODO
 */
public interface SamplesInfosSource extends List<SampleInfo> {

	List<Integer> getSampleOrigIndices() throws IOException;
	List<SampleKey> getSampleKeys() throws IOException;
	List<Integer> getOrderIds() throws IOException;
	List<String> getFathers() throws IOException;
	List<String> getMothers() throws IOException;
	List<Sex> getSexes() throws IOException;
	List<Affection> getAffections() throws IOException;
	List<String> getCategories() throws IOException;
	List<String> getDiseases() throws IOException;
	List<String> getPopulations() throws IOException;
	List<Integer> getAges() throws IOException;
	List<String> getFilters() throws IOException;
	List<Integer> getApproveds() throws IOException;
	List<Integer> getStatuses() throws IOException;

	List<Integer> getSampleOrigIndices(int from, int to) throws IOException;
	List<SampleKey> getSampleKeys(int from, int to) throws IOException;
	List<Integer> getOrderIds(int from, int to) throws IOException;
	List<String> getFathers(int from, int to) throws IOException;
	List<String> getMothers(int from, int to) throws IOException;
	List<Sex> getSexes(int from, int to) throws IOException;
	List<Affection> getAffections(int from, int to) throws IOException;
	List<String> getCategories(int from, int to) throws IOException;
	List<String> getDiseases(int from, int to) throws IOException;
	List<String> getPopulations(int from, int to) throws IOException;
	List<Integer> getAges(int from, int to) throws IOException;
	List<String> getFilters(int from, int to) throws IOException;
	List<Integer> getApproveds(int from, int to) throws IOException;
	List<Integer> getStatuses(int from, int to) throws IOException;
}
