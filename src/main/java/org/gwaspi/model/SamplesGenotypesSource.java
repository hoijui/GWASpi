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

import java.util.List;
//import java.util.Map;

/**
 * Allows to read the genotypes matrix, sample by sample.
 * Each list of genotypes is #markers long.
 * The map has an ordered iterator.
 * Not all Map operations are supported by all implementations,
 * and some might be unbearably slow.
 */
//public interface SamplesGenotypesSource extends Map<SampleKey, GenotypesList> {
public interface SamplesGenotypesSource extends List<GenotypesList> {
}
