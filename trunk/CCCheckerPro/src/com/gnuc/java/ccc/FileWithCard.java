
package com.gnuc.java.ccc;

import java.io.File;
import java.util.Vector;

public class FileWithCard
{
	private File				fileWithCardNumber;
	private Vector<CardFound>	cardsOnThisFile;
	
	public File getFileWithCardNumber()
	{
		return fileWithCardNumber;
	}
	
	public Vector<CardFound> getCardsOnThisFile()
	{
		return cardsOnThisFile;
	}
	
	public FileWithCard(File coFile)
	{
		fileWithCardNumber = coFile;
		cardsOnThisFile = new Vector<CardFound>();
	}
	
	public void addCard(CreditCard card, String cardNumber, int foundStart, int foundEnd)
	{
		cardsOnThisFile.add(new CardFound(card, cardNumber, foundStart, foundEnd));
	}
	
	public void printCards()
	{
		System.out.println("-------------------");
		System.out.println("File Name : " + fileWithCardNumber.getAbsolutePath());
		for (CardFound card : cardsOnThisFile)
		{
			System.out.println("\tCard Number : " + card.cardNumber);
			System.out.println("\tCard Start  : " + card.foundStart);
			System.out.println("\tCard End    : " + card.foundEnd + "\n");
		}
		System.out.println("-------------------");
	}
}

class CreditCard
{
	String	cardRegex	= "";
	String	cardIssuer	= "";
	String	cardCode	= "";
	
	public CreditCard(String cardRegex, String cardIssuer, String cardCode)
	{
		super();
		this.cardRegex = cardRegex;
		this.cardIssuer = cardIssuer;
		this.cardCode = cardCode;
	}
}

class CardFound
{
	CreditCard	card		= null;
	String		cardNumber	= "";
	int			foundStart;
	int			foundEnd;
	
	public CardFound(CreditCard card, String cardNumber, int foundStart, int foundEnd)
	{
		this.card = card;
		this.cardNumber = cardNumber;
		this.foundStart = foundStart;
		this.foundEnd = foundEnd;
	}
}
