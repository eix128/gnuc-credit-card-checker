
package com.gnuc.java.ccc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class FileType
{
	Vector<String>		dTypes	= null;
	Vector<CreditCard>	rTypes	= null;
	
	public FileType(String configPath) throws SAXException, IOException, ParserConfigurationException
	{
		dTypes = new Vector<String>();
		rTypes = new Vector<CreditCard>();
		Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configPath);
		NodeList fileTypes = xmlDoc.getElementsByTagName("ccFile");
		for (int i = 0; i < fileTypes.getLength(); i++)
			dTypes.add(((Element) fileTypes.item(i)).getAttribute("mime"));
		NodeList regexTypes = xmlDoc.getElementsByTagName("ccRegEx");
		for (int i = 0; i < regexTypes.getLength(); i++)
		{
			Element elm = (Element) regexTypes.item(i);
			rTypes.add(new CreditCard(elm.getFirstChild().getNodeValue(), elm.getAttribute("ccIssuer"), elm.getAttribute("ccCode")));
		}
	}
	
	public void updateRegex(Vector<CreditCard> rTypes)
	{
		this.rTypes = rTypes;
	}
	
	public synchronized FileWithCard search(File input) throws IOException
	{
		FileInputStream is = null;
		try
		{
			is = new FileInputStream(input);
			ContentHandler contenthandler = new BodyContentHandler();
			Metadata metadata = new Metadata();
			metadata.set(Metadata.RESOURCE_NAME_KEY, input.getName());
			Parser parser = new AutoDetectParser();
			parser.parse(is, contenthandler, metadata,new ParseContext());
			is.close();
			FileWithCard fwc = new FileWithCard(input);
			for (CreditCard card : rTypes)
			{
				Pattern pattern = Pattern.compile(card.cardRegex);
				Matcher m = null;
				m = pattern.matcher(contenthandler.toString());
				while (m.find() && LuhnCheck.c(m.group()))// && SumOfDigits.get(Integer.parseInt(m.group()))==50 )
				{
					// String foundCardNumber=m.group();
					// if(Luhn.Check(foundCardNumber))
					fwc.addCard(card, m.group(), m.start(), m.end());
				}
			}
			return fwc.getCardsOnThisFile().size() > 0 ? fwc : null;
		}
		catch (TikaException e)
		{
			// e.printStackTrace();
			is.close();
			return null;
		}
		catch (SAXException e)
		{
			// e.printStackTrace();
			is.close();
			return null;
		}
		catch (IOException e)
		{
			// e.printStackTrace();
			is.close();
			return null;
		}
	}
	
	public synchronized boolean check(File input) throws IOException
	{
		FileInputStream is = null;
		try
		{
			is = new FileInputStream(input);
			ContentHandler contenthandler = new BodyContentHandler();
			Metadata metadata = new Metadata();
			metadata.set(Metadata.RESOURCE_NAME_KEY, input.getName());
			Parser parser = new AutoDetectParser();
			parser.parse(is, contenthandler, metadata,new ParseContext());
			is.close();
			String mimeTp=metadata.get(Metadata.CONTENT_TYPE);
			if(mimeTp.indexOf(";")>-1)
				mimeTp=mimeTp.substring(0,mimeTp.indexOf(";"));
			//System.out.println(mimeTp+"   "+dTypes.contains(mimeTp));
			return dTypes.contains(mimeTp);
		}
		catch (TikaException e)
		{
			// e.printStackTrace();
			is.close();
			return false;
		}
		catch (SAXException e)
		{
			// e.printStackTrace();
			is.close();
			return false;
		}
		catch (IOException e)
		{
			// e.printStackTrace();
			is.close();
			return false;
		}
	}
	
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException
	{
		File searchFile = new File("D:/Desktop/Search/Credit Card Test Data.htm");
		FileType ft = new FileType("D:/Workspace/Gnu Consultancy/Paladion/documentTypes_ORIGINAL.xml");
		System.out.println("Result : " + ft.check(searchFile));
		ft.search(searchFile);
	}
}
