
package com.gnuc.java.ccc;

/**
 * Luhn Class is an implementation of the Luhn algorithm that checks validity of a credit card number.
 * 
 * @author <a href="http://www.chriswareham.demon.co.uk/software/Luhn.java">Chris Wareham</a>
 * @version Checks whether a string of digits is a valid credit card number according to the Luhn algorithm. 1. Starting with the second to last digit and
 *           moving left, double the value of all the alternating digits. For any digits that thus become 10 or more, add their digits together. For example,
 *           1111 becomes 2121, while 8763 becomes 7733 (from (1+6)7(1+2)3). 2. Add all these digits together. For example, 1111 becomes 2121, then 2+1+2+1 is
 *           6; while 8763 becomes 7733, then 7+7+3+3 is 20. 3. If the total ends in 0 (put another way, if the total modulus 10 is 0), then the number is valid
 *           according to the Luhn formula, else it is not valid. So, 1111 is not valid (as shown above, it comes out to 6), while 8763 is valid (as shown
 *           above, it comes out to 20).
 * @param ccNumber
 *            the credit card number to validate.
 * @return <b>true</b> if the number is valid, <b>false</b> otherwise.
 */
public class Luhn
{
	public static boolean Check(String ccNumber)
	{
		int sum = 0;
		boolean alternate = false;
		for (int i = ccNumber.length() - 1; i >= 0; i--)
		{
			int n = Integer.parseInt(ccNumber.substring(i, i + 1));
			if (alternate)
			{
				n *= 2;
				if (n > 9)
				{
					n = (n % 10) + 1;
				}
			}
			sum += n;
			alternate = !alternate;
		}
		return (sum % 10 == 0);
	}
}
