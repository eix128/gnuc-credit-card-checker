
package com.gnuc.java.ccc;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import com.swtdesigner.SWTResourceManager;

public class Waiter extends Shell
{
	static Display			display				= null;
	private final Cursor	waitingIndicator	= new Cursor(display, SWT.CURSOR_WAIT);
	private Text			txtPleaseWaitUntil;
	
	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void showWait()
	{
		try
		{
			display = Display.getDefault();
			final Waiter shell = new Waiter(display);
			int screenWidth = Display.getDefault().getClientArea().width;
			int screenHeight = Display.getDefault().getClientArea().height;
			shell.setLocation((screenWidth / 2) - 250, (screenHeight / 2) - 100);
			shell.addFocusListener(new FocusListener()
			{
				public void focusLost(FocusEvent e)
				{
					shell.forceActive();
				}
				
				public void focusGained(FocusEvent e)
				{}
			});
			shell.open();
			shell.layout();
			while (!shell.isDisposed())
			{
				if (!display.readAndDispatch())
				{
					display.sleep();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Create the shell.
	 * 
	 * @param display
	 */
	public Waiter(Display display)
	{
		super(display, SWT.BORDER | SWT.TITLE | SWT.PRIMARY_MODAL);
		setImage(SWTResourceManager.getImage(Waiter.class, "/org/eclipse/jface/action/images/stop.gif"));
		createContents();
	}
	
	/**
	 * Create contents of the shell.
	 */
	protected void createContents()
	{
		setText("Working");
		setSize(300, 150);
		setCursor(waitingIndicator);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		ProgressBar progressBar = new ProgressBar(this, SWT.SMOOTH | SWT.INDETERMINATE);
		progressBar.setBounds(10, 71, 274, 25);
		progressBar.setEnabled(true);
		progressBar.setVisible(true);
		txtPleaseWaitUntil = new Text(this, SWT.WRAP | SWT.MULTI);
		txtPleaseWaitUntil.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		txtPleaseWaitUntil.setText("Please wait until the application closes all open files.");
		txtPleaseWaitUntil.setBounds(12, 24, 272, 41);
	}
	
	@Override
	protected void checkSubclass()
	{
		// Disable the check that prevents subclassing of SWT components
	}
}
