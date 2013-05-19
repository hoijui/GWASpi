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

import org.gwaspi.global.Text;

public class HelpURLs {

	public static final String baseURL = "http://www.gwaspi.org/";

	private HelpURLs() {
	}

	public static class Intro {

		public static final Object[] intro = new Object[]{"Introducing " + Text.App.appName, "?page_id=213"};
		public static final Object[] quickstart = new Object[]{"Quickstart", "?page_id=175"};
		public static final Object[] tutorial = new Object[]{"Tutorial", "?page_id=226"};
		public static final Object[] loadGts = new Object[]{"Load Genotyes", "?page_id=245"};
		public static final Object[] fileFormats = new Object[]{"Import File Formats", "?page_id=121"};
		public static final Object[] GWASinOneGo = new Object[]{"GWAS in one go", "?page_id=289"};

		private Intro() {
		}
	}

	public static class QryURL {

		public static final String intro = "?page_id=213";
		public static final String quickstart = "?page_id=175";
		public static final String tutorial = "?page_id=226";
		public static final String loadGts = "?page_id=245";
		public static final String fileFormats = "?page_id=121";
		public static final String GWASinOneGo = "?page_id=289";
		public static final String createStudy = "?page_id=231";
		public static final String helpIndex = "";
		public static final String matrixExtract = "?page_id=297";
		public static final String currentMatrix = "?page_id=294";
		public static final String currentStudy = "?page_id=240";
		public static final String matrixTranslate = "?page_id=300";
		public static final String matrixMerge = "?page_id=302";
		public static final String matrixAnalyse = "?page_id=305";
		public static final String sampleQAreport = "?page_id=345";
		public static final String hwReport = "?page_id=347";
		public static final String assocReport = "?page_id=349";
		public static final String markerQAreport = "?page_id=345";
		public static final String sampleInforeport = "?page_id=343";

		private QryURL() {
		}
	}
}
