
package com.gnuc.java.ccc;

import java.lang.reflect.InvocationTargetException;

public interface IProcessor<I, O>
{
	O process(I input) throws InterruptedException, InvocationTargetException;
}
