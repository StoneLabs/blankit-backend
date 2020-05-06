/*
    Copyright (c) 2016, Levy Ehrstein. All rights reserved.
    This file is part of BlankIT.

    BlankIT is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BlankIT is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BlankIT.  If not, see <http://www.gnu.org/licenses/>.

    Diese Datei ist Teil von BlankIT.

    BlankIT ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    BlankIT wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
*/

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;



/**
 * Main logic class containing parsing algorithm
 */
public class Main 
{
	public static final String API_prefix =
		"https://de.wikipedia.org/w/api.php" +
		"?format=xml&action=query&prop=extracts&exlimit=max&explaintext&titles=";
	
	/**
	 * Programms main.
	 *
	 * @param  args  Arg0: Article name to search for
	 * @return	Returns the article in plain text format
	 */
	public static void main(String[] args)
	{
		String wikiUrl = args[0];
		
		String xml = getWikiXML(wikiUrl);
		String out = ParseText (xml);
		System.out.println(out);
	}
	
	/**
	 * Uses wiki api implementation to get a XML version of the articles content
	 *
	 * @param  article  Article name searched for
	 * @return	Returns The XML document containing the article
	 */
	public static String getWikiXML(String article)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(API_prefix);
		sb.append(article);
				
		return getText(sb.toString());
	}



	/**
	 * Downloads 'nearly' any file using its URL
	 *
	 * @param  url  The url downloaded by the function
	 * @return	Returns the files content
	 */
    public static String getText(String url)
    {
        try {
	        URL website = new URL(url);
	        URLConnection connection = website.openConnection();
	        BufferedReader in = new BufferedReader(
	                                new InputStreamReader(
	                                    connection.getInputStream()));
	
	        StringBuilder response = new StringBuilder();
	        String inputLine = "";
	        while ((inputLine = in.readLine()) != null) 
	        	response.append(inputLine);

			in.close();
			
	        return response.toString();
		} catch (IOException e) {
			Debug.Error(e.getMessage());
		}
        return "";
    }


	/**
	 * Parses the XML-Formatted input to plain text output!
	 *
	 * @param  xml  A string conatining the XML-document.
	 * @return	Returns the article in plain text format.
	 */
    public static String ParseText(String xml)
    {
    	try {
    		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    		Document doc = dBuilder.parse(
    				new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
    				
    		doc.getDocumentElement().normalize();

//    		System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
    				
    		NodeList nList = doc.getElementsByTagName("page");
    				
    		for (int temp = 0; temp < nList.getLength(); temp++) {
    			Node nNode = nList.item(temp);
    					
//    			System.out.println("\nCurrent Element :" + nNode.getNodeName());
    					
    			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

    				Element eElement = (Element) nNode;
    				
        			String idx = eElement.getAttribute("_idx");
        			if (Integer.parseInt(idx) == -1)
        				continue;
        			
        			return eElement.getTextContent();
    			}
    		}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
    	return "[@31]No article found!";
    }
}
