
package com.gnuc.java.ccc;

import java.io.File;
import java.io.StringWriter;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CCExporter
{
	public static boolean writeXML(String fileName,Vector<FileWithCard> fwcList)
	{
		try
		{
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document ffwc = docBuilder.newDocument();
		
			Element ccSearcher = ffwc.createElement("ccSearcher");
			ffwc.appendChild(ccSearcher);
			
			for(FileWithCard fwc:fwcList)
			{
				Element ccFileWithCard = ffwc.createElement("ccFileWithCard");
				ccFileWithCard.setAttribute("fileName",fwc.getFileWithCardNumber().getAbsolutePath());
				for(CardFound cf:fwc.getCardsOnThisFile())
				{
					Element ccCard = ffwc.createElement("ccCard");
					ccCard.setAttribute("number",cf.cardNumber);
					ccCard.setAttribute("issuer",cf.card.cardIssuer);
					ccFileWithCard.appendChild(ccCard);
				}
				ccSearcher.appendChild(ccFileWithCard);
			}

			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(ffwc);
			trans.transform(source, result);
			FileUtils.writeStringToFile(new File(fileName),sw.toString());
			return true;
		}
		catch (Exception e)
		{
			//System.out.println(e);
			return false;
		}
	}
}
