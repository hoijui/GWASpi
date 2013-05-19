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

package org.gwaspi.global;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLParser {

	private static final Logger log
			= LoggerFactory.getLogger(XMLParser.class);

	private XMLParser() {
	}

	public static Document parseXmlFile(String XMLurl) {
		Document dom = null;
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file
			dom = db.parse(XMLurl);
		} catch (Exception ex) {
			log.error(null, ex);
		}

		return dom;
	}

	public static List<Element> parseDocument(Document dom, String tagName) {
		List<Element> resultList = new ArrayList<Element>();
		Object tagObject = new Object();

		// get the root element
		Element docEle = dom.getDocumentElement();

		// get a nodelist of  elements
		NodeList nl = docEle.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				// get the tagName element
				Element el = (Element) nl.item(i);
				// get the tagName object
				//Employee tagObject = getEmployee(el);
				// add it to list
				resultList.add(el);
			}
		}
		return resultList;
	}

	public static String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}
		return textVal;
	}

	public static Date getDateValue(Element ele, String tagName) throws ParseException {
		Date date = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			String textVal = el.getFirstChild().getNodeValue();
			DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
			date = formatter.parse(textVal);
		}
		return date;
	}
}
