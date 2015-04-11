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

import java.net.UnknownHostException;
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

	public static Document parseXmlFile(final String xmlUrl) {
		Document dom = null;
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			// Using factory get an instance of document builder
			final DocumentBuilder documentBuilder = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file
			dom = documentBuilder.parse(xmlUrl);
		} catch (UnknownHostException ex) {
			log.debug("UnknownHostException: {}", ex.getMessage());
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
		final NodeList nodeList = docEle.getElementsByTagName(tagName);
		if (nodeList != null && nodeList.getLength() > 0) {
			for (int nodeIndex = 0; nodeIndex < nodeList.getLength(); nodeIndex++) {
				// get the tagName element
				final Element element = (Element) nodeList.item(nodeIndex);
				// get the tagName object
				//Employee tagObject = getEmployee(el);
				// add it to list
				resultList.add(element);
			}
		}
		return resultList;
	}

	public static String getTextValue(Element parentElement, String tagName) {
		String textVal = null;
		final NodeList nodeList = parentElement.getElementsByTagName(tagName);
		if (nodeList != null && nodeList.getLength() > 0) {
			final Element element = (Element) nodeList.item(0);
			textVal = element.getFirstChild().getNodeValue();
		}
		return textVal;
	}

	public static Date getDateValue(Element parentElement, String tagName) throws ParseException {
		Date date = null;
		final NodeList nodeList = parentElement.getElementsByTagName(tagName);
		if (nodeList != null && nodeList.getLength() > 0) {
			final Element element = (Element) nodeList.item(0);
			String textVal = element.getFirstChild().getNodeValue();
			DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
			date = formatter.parse(textVal);
		}
		return date;
	}
}
