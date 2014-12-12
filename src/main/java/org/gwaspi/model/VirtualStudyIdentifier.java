/*
 * Copyright (C) 2014 Universitat Pompeu Fabra
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

/**
 * Uniquely identifies a study that may not exist.
 */
public class VirtualStudyIdentifier extends AbstractVirtualIdentifier<StudyKey> {

	public VirtualStudyIdentifier(final StudyKey key, final String name) {
		super(key, name);
	}

	public VirtualStudyIdentifier(final StudyKey key) {
		super(key);
	}

	protected VirtualStudyIdentifier() {
		this(null);
	}

	public static VirtualStudyIdentifier valueOf(final Study study) {
		return new VirtualStudyIdentifier(StudyKey.valueOf(study));
	}
}
