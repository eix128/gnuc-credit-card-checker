
package com.gnuc.java.ccc;

public class LuhnCheck
{
	public static String getDigits(String s)
	{
		StringBuffer digitsOnly = new StringBuffer();
		char c;
		for (int i = 0; i < s.length(); i++)
		{
			c = s.charAt(i);
			if (Character.isDigit(c))
				digitsOnly.append(c);
		}
		return digitsOnly.toString();
	}
	
	// -------------------
	// Perform Luhn check
	// -------------------
	public static boolean c(String cardNumber)
	{
		String digitsOnly = getDigits(cardNumber);
		int sum = 0;
		int digit = 0;
		int addend = 0;
		boolean timesTwo = false;
		for (int i = digitsOnly.length() - 1; i >= 0; i--)
		{
			digit = Integer.parseInt(digitsOnly.substring(i, i + 1));
			if (timesTwo)
			{
				addend = digit * 2;
				if (addend > 9)
					addend -= 9;
			}
			else
				addend = digit;
			sum += addend;
			timesTwo = !timesTwo;
		}
		int modulus = sum % 10;
		return modulus == 0;
	}
}
