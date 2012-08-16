/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gwaspi.global;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Fernando Muñiz Fernandez
 * IBE, Institute of Evolutionary Biology (UPF-CSIC)
 * CEXS-UPF-PRBB
 */
public class XMLParser {

	public static Document parseXmlFile(String XMLurl) {
		Document dom = null;
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			dom = db.parse(XMLurl);


		} catch (Exception e) {
		}

//        catch(ParserConfigurationException pce) {
//                pce.printStackTrace();
//        }catch(SAXException se) {
//                se.printStackTrace();
//        }catch(IOException ioe) {
//                ioe.printStackTrace();
//        }
		return dom;
	}

	public static ArrayList parseDocument(Document dom, String tagName) {
		ArrayList resultList = new ArrayList();
		Object tagObject = new Object();

		//get the root element
		Element docEle = dom.getDocumentElement();

		//get a nodelist of  elements
		NodeList nl = docEle.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				//get the tagName element
				Element el = (Element) nl.item(i);
				//get the tagName object
				//Employee tagObject = getEmployee(el);
				//add it to list
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
		String textVal = null;
		Date date = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
			DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
			date = (Date) formatter.parse(textVal);
		}
		return date;
	}
}
