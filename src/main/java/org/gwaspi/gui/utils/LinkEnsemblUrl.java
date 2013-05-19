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

package org.gwaspi.gui.utils;

public class LinkEnsemblUrl {

	private LinkEnsemblUrl() {
	}

	private static final String HOMOSAPIENS_BY_CHRPOS = "http://www.ensembl.org/Homo_sapiens/Location/View?r=";

	public static String getHomoSapiensLink(String chr, int position) {
		String baseUrl = HOMOSAPIENS_BY_CHRPOS;
		Integer startPos = (position - 50000);
		Integer endPos = (position + 50000);
		if (Integer.signum(startPos) == -1) {
			startPos = 0;
		}

		String querry = chr + ":" + startPos + "-" + endPos;

		return (baseUrl + querry);
	}
}
