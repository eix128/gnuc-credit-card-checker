
package com.gnuc.java.ccc;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.xml.sax.SAXException;
import com.swtdesigner.SWTResourceManager;

public class CCCheckerPro
{
	protected Shell					shlCreditCardChecker;
	private static int				foundCount				= 0;
	private static FileType			fileType				= null;
	private static CCCheckerPro		ccc						= null;
	private static Display			display					= null;
	private String					configFile				= "";
	private CCDetect				ccd						= null;
	private Label					opIndicator;
	private Table					filesWithCards,cardsInTheFile;
	private Button					btnStart,btnStop,btnExportAll = null;
	private ProgressBar				progressBar				= null;
	private ExpandBar				expandBar				= null;
	private final Cursor			waitingIndicator		= new Cursor(display, SWT.CURSOR_WAIT);
	private final Cursor			appStartingIndicator	= new Cursor(display, SWT.CURSOR_APPSTARTING);
	private final Cursor			defaultIndicator		= new Cursor(display, SWT.CURSOR_ARROW);
	private static int				searchCount				= 0;
	private Vector<File>			searchList				= null;
	private Vector<File>			fileScanList			= null;
	private Vector<Label>			labelList				= null;
	private Vector<CreditCard>		regExList				= null;
	private Vector<FileWithCard>	fwcList					= null;
	private ExpandItem				xpndtmRegexConfiguration,xpndtmSearchResult,xpndtmSearchConfiguration = null;
	private final Comparator<File>	sizeCompare				= new Comparator<File>()
															{
																public int compare(File file1, File file2)
																{
																	return file1.getTotalSpace() < file2.getTotalSpace() ? 1 : 0;
																}
															};
	private Text					cFNumber;
	private Text					fFNumber;
	
	public Vector<Label> getLabelList()
	{
		return labelList;
	}
	
	public static synchronized CCCheckerPro get()
	{
		return ccc == null ? new CCCheckerPro() : ccc;
	}
	
	public static void main(String[] args)
	{
		try
		{
			CCCheckerPro window = new CCCheckerPro();
			window.open();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void open()
	{
		display = Display.getDefault();
		createContents();
		int screenWidth = Display.getDefault().getClientArea().width;
		int screenHeight = Display.getDefault().getClientArea().height;
		shlCreditCardChecker.setLocation((screenWidth / 2) - 300, (screenHeight / 2) - 350);
		shlCreditCardChecker.open();
		shlCreditCardChecker.layout();
		shlCreditCardChecker.setCursor(defaultIndicator);
		opIndicator = new Label(shlCreditCardChecker, SWT.NONE);
		opIndicator.setBounds(559, 587, 20, 20);
		opIndicator.setImage(SWTResourceManager.getImage(CCCheckerPro.class, "/gnuc/stop.png"));
		opIndicator.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		Label lblCreditCardChecker = new Label(shlCreditCardChecker, SWT.HORIZONTAL | SWT.SHADOW_IN | SWT.RIGHT);
		lblCreditCardChecker.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		lblCreditCardChecker.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		lblCreditCardChecker.setAlignment(SWT.CENTER);
		lblCreditCardChecker.setBounds(0, 597, 584, 15);
		lblCreditCardChecker.setText("FULL - Credit Card Checker Pro - Gnu Consultancy Pvt. Ltd. [http://gnuc.in]");
		Button button = new Button(shlCreditCardChecker, SWT.NONE);
		button.setBounds(23, 476, 75, 25);
		button.setText("New Button");
		Label lblCardsFound = new Label(shlCreditCardChecker, SWT.NONE);
		lblCardsFound.setBounds(20, 570, 75, 15);
		lblCardsFound.setText("Cards Found");
		Label lblFilesFound = new Label(shlCreditCardChecker, SWT.NONE);
		lblFilesFound.setBounds(20, 549, 75, 15);
		lblFilesFound.setText("Files Found");
		cFNumber = new Text(shlCreditCardChecker, SWT.READ_ONLY | SWT.RIGHT);
		cFNumber.setText("0");
		cFNumber.setEditable(false);
		cFNumber.setBounds(10, 568, 139, 23);
		fFNumber = new Text(shlCreditCardChecker, SWT.READ_ONLY | SWT.RIGHT);
		fFNumber.setText("0");
		fFNumber.setEditable(false);
		fFNumber.setBounds(10, 549, 139, 21);
		while (!shlCreditCardChecker.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
	}
	
	protected void createContents()
	{
		shlCreditCardChecker = new Shell();
		shlCreditCardChecker.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		shlCreditCardChecker.addControlListener(new ControlAdapter()
		{
			@Override
			public void controlResized(ControlEvent e)
			{
				shlCreditCardChecker.setSize(600, 650);
			}
		});
		shlCreditCardChecker.addDisposeListener(new DisposeListener()
		{
			public void widgetDisposed(DisposeEvent e)
			{
				// shutdownRibbon.setVisible(false);
				// lblPleaseLoadConfiguration.setBounds(10, 10, 0, 0);
				// shutdownRibbon.setBounds(10, 10, 564, 572);
				if (ccd != null)
					ccd.stop();
				//System.out.println(e.widget + " disposed");
				System.exit(1);
			}
		});
		// shlCreditCardChecker.setMinimumSize(new Point(600, 650));
		shlCreditCardChecker.setImage(SWTResourceManager.getImage(CCCheckerPro.class, "/gnuc/gnuCLogo.png"));
		shlCreditCardChecker.setSize(600, 650);
		shlCreditCardChecker.setText("Credit Card Checker Pro - Gnu Consultancy Pvt. Ltd. [http://gnuc.in]");
		// lblPleaseLoadConfiguration = new CLabel(shlCreditCardChecker, SWT.CENTER);
		// lblPleaseLoadConfiguration.setText("Please load Configuration File");
		// lblPleaseLoadConfiguration.setAlignment(SWT.CENTER);
		// lblPleaseLoadConfiguration.setForeground(SWTResourceManager.getColor(220, 20, 60));
		// lblPleaseLoadConfiguration.setBounds(10, 0, 48, 10);
		// formToolkit.adapt(lblPleaseLoadConfiguration, true, true);
		// shutdownRibbon = new CLabel(shlCreditCardChecker, SWT.CENTER);
		// shutdownRibbon.setText("Please wait while all threads are shutdown.");
		// shutdownRibbon.setAlignment(SWT.CENTER);
		// shutdownRibbon.setForeground(SWTResourceManager.getColor(220, 20, 60));
		// shutdownRibbon.setBounds(337, 0, 97, 10);
		// formToolkit.adapt(shutdownRibbon, true, true);
		/*
		 * Menu menu = new Menu(shlCreditCardChecker, SWT.BAR); shlCreditCardChecker.setMenuBar(menu); MenuItem mntmLoadConfig = new MenuItem(menu,
		 * SWT.CASCADE); mntmLoadConfig.setText("Config"); Menu menu_1 = new Menu(mntmLoadConfig); mntmLoadConfig.setMenu(menu_1); mntmLoad = new
		 * MenuItem(menu_1, SWT.RADIO); mntmLoad.addArmListener(new ArmListener() { public void widgetArmed(ArmEvent e) {} }); mntmLoad.addSelectionListener(new
		 * SelectionAdapter() {
		 * @Override public void widgetSelected(SelectionEvent event) { FileDialog fd = new FileDialog(shlCreditCardChecker, SWT.OPEN);
		 * fd.setText("Open configuration file"); fd.setFilterPath(null); String[] filterExt = { "*.xml" }; fd.setFilterExtensions(filterExt); String selected =
		 * fd.open(); try { fileType = new FileType(selected); lblPleaseLoadConfiguration.setVisible(false); shutdownRibbon.setVisible(false); createTabs(); }
		 * catch (SAXException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); } catch (ParserConfigurationException e) {
		 * e.printStackTrace(); } } }); mntmLoad.setText("Load\tCTRL+L"); mntmLoad.setAccelerator(SWT.CTRL + 'L');
		 */
		init();
		xpndtmSearchResult.setHeight(xpndtmSearchResult.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
	}
	
	protected void init()
	{
		try
		{
			configFile = CCCheckerPro.class.getResource("/gnuc/documentTypes.xml").toString();
			fileType = new FileType(configFile);
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		createTabs();
	}
	
	protected void createTabs()
	{
		searchList = new Vector<File>();
		labelList = new Vector<Label>();
		regExList = new Vector<CreditCard>();
		btnStart = new Button(shlCreditCardChecker, SWT.NONE);
		btnStart.setSelection(true);
		btnStart.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_DARK_SHADOW));
		btnStart.setText("Start");
		btnStop = new Button(shlCreditCardChecker, SWT.NONE);
		btnStop.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BORDER));
		btnStop.setText("Stop");
		btnStart.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDown(MouseEvent e)
			{
				Collections.sort(searchList, sizeCompare);
				btnStart.setEnabled(false);
				btnStop.setEnabled(true);
				filesWithCards.clearAll();
				filesWithCards.setItemCount(0);
				cardsInTheFile.clearAll();
				cardsInTheFile.setItemCount(0);
				fFNumber.setText("0");
				cFNumber.setText("0");
				fwcList= new Vector<FileWithCard>();
				btnExportAll.setEnabled(false);
				if (regExList.size() > 0)
				{
					if (searchList.size() > 0)
					{
						fileType.updateRegex(regExList);
						new Thread()
						{
							public void run()
							{
								try
								{
									display.asyncExec(new Runnable()
									{
										public void run()
										{
											opIndicator.setImage(SWTResourceManager.getImage(CCCheckerPro.class, "/gnuc/working.png"));
											shlCreditCardChecker.setCursor(appStartingIndicator);
											progressBar.setVisible(true);
											xpndtmRegexConfiguration.setExpanded(false);
											xpndtmSearchConfiguration.setExpanded(false);
											xpndtmSearchResult.setExpanded(true);
										}
									});
									ccd = new CCDetect();
									ccd.detect(searchList);
								}
								catch (InterruptedException e)
								{
									e.printStackTrace();
								}
								catch (ExecutionException e)
								{
									e.printStackTrace();
								}
							}
						}.start();
					}
					else
					{
						xpndtmRegexConfiguration.setExpanded(false);
						xpndtmSearchConfiguration.setExpanded(true);
						xpndtmSearchResult.setExpanded(false);
						btnStart.setEnabled(true);
						btnStop.setEnabled(false);
					}
				}
				else
				{
					xpndtmRegexConfiguration.setExpanded(true);
					xpndtmSearchConfiguration.setExpanded(false);
					xpndtmSearchResult.setExpanded(false);
					btnStart.setEnabled(true);
					btnStop.setEnabled(false);
				}
			}
		});
		btnStart.setBounds(200, 544, 75, 25);
		btnStop.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDown(MouseEvent e)
			{
				final Waiter waitProgress = new Waiter(display);
				waitProgress.addFocusListener(new FocusListener()
				{
					public void focusLost(FocusEvent e)
					{
						waitProgress.forceActive();
					}
					
					public void focusGained(FocusEvent e)
					{}
				});
				waitProgress.setLocation((Display.getDefault().getClientArea().width / 2) - 150, (Display.getDefault().getClientArea().height / 2) - 75);
				waitProgress.open();
				opIndicator.setImage(SWTResourceManager.getImage(CCCheckerPro.class, "/gnuc/pending.png"));
				shlCreditCardChecker.setCursor(waitingIndicator);
				progressBar.setVisible(false);
				ccd.stop();
				shlCreditCardChecker.setCursor(defaultIndicator);
				opIndicator.setImage(SWTResourceManager.getImage(CCCheckerPro.class, "/gnuc/stop.png"));
				waitProgress.close();
			}
		});
		btnStop.setEnabled(false);
		btnStop.setBounds(325, 544, 75, 25);
		progressBar = new ProgressBar(shlCreditCardChecker, SWT.SMOOTH | SWT.INDETERMINATE);
		progressBar.setBounds(200, 575, 200, 10);
		progressBar.setVisible(false);
		expandBar = new ExpandBar(shlCreditCardChecker, SWT.V_SCROLL);
		expandBar.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		expandBar.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		expandBar.setBounds(10, 10, 564, 411);
		xpndtmRegexConfiguration = new ExpandItem(expandBar, SWT.NONE);
		xpndtmRegexConfiguration.setText("Regex Configuration");
		Composite composite_1 = new Composite(expandBar, SWT.NONE);
		composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		xpndtmRegexConfiguration.setControl(composite_1);
		xpndtmRegexConfiguration.setHeight(160);
		Label lblPleaseSelectCredit = new Label(composite_1, SWT.NONE);
		lblPleaseSelectCredit.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		lblPleaseSelectCredit.setBounds(10, 20, 434, 16);
		lblPleaseSelectCredit.setText("Please select credit card patterns to check during search");
		int b_Y = 45;
		for (final CreditCard card : fileType.rTypes)
		{
			final Button button = new Button(composite_1, SWT.CHECK);
			button.setBounds(10, b_Y, 412, 16);
			button.setText(card.cardIssuer + " : " + card.cardRegex);
			button.setSelection(true);
			button.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
			regExList.add(card);
			button.addSelectionListener(new SelectionListener()
			{
				public void widgetSelected(SelectionEvent e)
				{
					if (button.getSelection() == true && regExList != null && !regExList.contains(card))
						regExList.add(card);
					else if (button.getSelection() == false && regExList != null && regExList.contains(card))
						regExList.remove(card);
				}
				
				public void widgetDefaultSelected(SelectionEvent e)
				{}
			});
			b_Y += 22;
		}
		xpndtmRegexConfiguration.setHeight(b_Y + 16);
		xpndtmSearchConfiguration = new ExpandItem(expandBar, SWT.NONE);
		xpndtmSearchConfiguration.setText("Search Configuration");
		final Composite composite = new Composite(expandBar, SWT.NONE);
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		xpndtmSearchConfiguration.setControl(composite);
		xpndtmSearchConfiguration.setHeight(560);
		Label lblPleaseChooseDirectories = new Label(composite, SWT.NONE);
		lblPleaseChooseDirectories.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		lblPleaseChooseDirectories.setBounds(10, 20, 434, 15);
		lblPleaseChooseDirectories.setText("Please choose directories to include in search");
		File[] roots = File.listRoots();
		b_Y = 45;
		for (final File root : roots)
		{
			if (root.getFreeSpace() > 0)
			{
				searchCount++;
				final Button button = new Button(composite, SWT.CHECK);
				button.setBounds(20, b_Y, 200, 16);
				button.setText(root.getAbsolutePath());
				button.setSelection(false);
				button.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
				button.addSelectionListener(new SelectionListener()
				{
					public void widgetSelected(SelectionEvent e)
					{
						if (button.getSelection() == true && searchList != null && !searchList.contains(root))
							searchList.add(root);
						else if (button.getSelection() == false && searchList != null && searchList.contains(root))
							searchList.remove(root);
					}
					
					public void widgetDefaultSelected(SelectionEvent e)
					{}
				});
				b_Y += 22;
			}
		}
		Button btnAdd = new Button(composite, SWT.NONE);
		btnAdd.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDown(MouseEvent e)
			{
				DirectoryDialog dd = new DirectoryDialog(shlCreditCardChecker, SWT.OPEN);
				dd.setText("Select Directory");
				final String selected = dd.open();
				if (selected != null && !searchList.contains(new File(selected)))
				{
					int b_Y = 45;
					b_Y += searchCount * 22;
					searchCount++;
					searchList.add(new File(selected));
					final Button button = new Button(composite, SWT.CHECK);
					button.setBounds(20, b_Y, 200, 16);
					button.setText(selected);
					button.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
					button.addSelectionListener(new SelectionListener()
					{
						public void widgetSelected(SelectionEvent e)
						{
							if (button.getSelection() == true && searchList != null && !searchList.contains(new File(selected)))
								searchList.add(new File(selected));
							else if (button.getSelection() == false && searchList != null && searchList.contains(new File(selected)))
								searchList.remove(new File(selected));
						}
						
						public void widgetDefaultSelected(SelectionEvent e)
						{}
					});
					button.setSelection(true);
					xpndtmSearchConfiguration.setHeight(b_Y + 32);
				}
			}
		});
		btnAdd.setBounds(493, 15, 34, 25);
		btnAdd.setText("Add");
		xpndtmSearchConfiguration.setHeight(b_Y + 16);
		xpndtmSearchResult = new ExpandItem(expandBar, SWT.NONE);
		xpndtmSearchResult.setExpanded(true);
		xpndtmSearchResult.setText("Search Result");
		Composite composite_3 = new Composite(expandBar, SWT.NONE);
		xpndtmSearchResult.setControl(composite_3);
		Composite composite_5 = new Composite(composite_3, SWT.NONE);
		composite_5.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		composite_5.setBounds(0, 0, 554, 320);
		btnExportAll = new Button(composite_5, SWT.NONE);
		btnExportAll.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDown(MouseEvent event)
			{
				btnExportAll.setEnabled(false);
				FileDialog sfd = new FileDialog(shlCreditCardChecker, SWT.SAVE);
				sfd.setFilterPath(null);
				String[] filterExt = { "*.xml" };
				sfd.setFilterExtensions(filterExt);
				boolean done = false;
				String fileName = null;
				while (!done)
				{
					fileName = sfd.open();
					if (fileName == null)
						done = true;
					else
					{
						File file = new File(fileName);
						if (file.exists())
						{
							MessageBox mb = new MessageBox(sfd.getParent(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
							mb.setMessage(fileName + " already exists. Do you want to replace it?");
							done = mb.open() == SWT.YES;
						}
						else
							done = true;
					}
				}
				if (fileName != null)
				{
					if (!CCExporter.writeXML(fileName, fwcList))
					{
						MessageBox mb = new MessageBox(sfd.getParent(), SWT.ICON_ERROR | SWT.OK);
						mb.setMessage("Could not save file.\nPlease try again.");
						mb.open();
					}
					else
					{
						MessageBox mb = new MessageBox(sfd.getParent(), SWT.ICON_INFORMATION | SWT.OK);
						mb.setMessage("Document succesfully saved.");
						mb.open();
					}
				}
				else
				{
					MessageBox mb = new MessageBox(sfd.getParent(), SWT.ICON_ERROR | SWT.OK);
					mb.setMessage("You did not select any file.\nPlease try again to continue.");
					mb.open();
				}
				btnExportAll.setEnabled(true);
			}
		});
		btnExportAll.setEnabled(false);
		btnExportAll.setBounds(469, 285, 75, 25);
		btnExportAll.setText("Export XML");
		cardsInTheFile = new Table(composite_5, SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION);
		cardsInTheFile.setBounds(344, 10, 200, 269);
		cardsInTheFile.setHeaderVisible(true);
		cardsInTheFile.setLinesVisible(true);
		TableColumn tblclmnCardNumber = new TableColumn(cardsInTheFile, SWT.NONE);
		tblclmnCardNumber.setWidth(132);
		tblclmnCardNumber.setText("Card Number");
		TableColumn tblclmnType = new TableColumn(cardsInTheFile, SWT.CENTER);
		tblclmnType.setResizable(false);
		tblclmnType.setWidth(42);
		tblclmnType.setText("Type");
		filesWithCards = new Table(composite_5, SWT.BORDER | SWT.FULL_SELECTION);
		filesWithCards.setBounds(10, 10, 328, 269);
		filesWithCards.setHeaderVisible(true);
		filesWithCards.setLinesVisible(true);
		filesWithCards.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				if (fwcList.size() > 0)
				{
					TableItem item = (TableItem) e.item;
					if (item == null)
						return;
					for (FileWithCard fwc : fwcList)
					{
						if (fwc.getFileWithCardNumber().getAbsolutePath() == item.getText())
						{
							cardsInTheFile.clearAll();
							cardsInTheFile.setItemCount(0);
							for (CardFound cf : fwc.getCardsOnThisFile())
							{
								TableItem newCardRow = new TableItem(cardsInTheFile, SWT.NONE);
								newCardRow.setText(0, cf.cardNumber);
								newCardRow.setText(1, cf.card.cardCode);
							}
							return;
						}
					}
				}
			}
		});
		TableColumn fileWithCards = new TableColumn(filesWithCards, SWT.NONE);
		fileWithCards.setWidth(253);
		fileWithCards.setText("Files with Cards");
		TableColumn tblclmnCards = new TableColumn(filesWithCards, SWT.CENTER);
		tblclmnCards.setResizable(false);
		tblclmnCards.setWidth(48);
		tblclmnCards.setText("Cards");
		Composite composite_4 = new Composite(shlCreditCardChecker, SWT.NONE);
		composite_4.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		composite_4.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		composite_4.setBounds(10, 427, 564, 111);
		b_Y = 5;
		for (int i = 0; i < 5; i++)
		{
			Label thdLabel = new Label(composite_4, SWT.NONE);
			thdLabel.setBounds(5, b_Y, 544, 15);
			thdLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
			thdLabel.setText("");
			labelList.add(thdLabel);
			b_Y += 22;
		}
	}
	class CCDetect
	{
		Thread								dumpingThread;
		AtomicInteger						dumpedCount;
		ExecutorService						dumpService;
		DirectoryScanner					rootScanner;
		BlockingQueue<File>					fileQueue,fileTypeQueue;
		BlockingQueue<FileWithCard>			outputQueue;
		QueueProcessor<File, File>			fileReaders;
		QueueProcessor<File, FileWithCard>	fileChecker;
		int									workItemCount0,workItemCount1,workItemCount2;
		
		public int getRandom(Random r, int lower, int higher)
		{
			double x = (double) r.nextInt() / Integer.MAX_VALUE * higher;
			return (int) x + lower;
		}
		
		void shutdownAndAwaitTermination(ExecutorService pool)
		{
			pool.shutdown();
			try
			{
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
				{
					pool.shutdownNow();
					if (!pool.awaitTermination(60, TimeUnit.SECONDS))
						System.err.println("Pool did not terminate");
				}
			}
			catch (InterruptedException ie)
			{
				pool.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
		
		public void stop()
		{
			try
			{
				shutdownAndAwaitTermination(dumpService);
				fileReaders.hardKill();
				fileChecker.hardKill();
				for (Label lb : labelList)
					lb.setText("     STOPPED");
				btnStop.setEnabled(false);
				btnStart.setEnabled(true);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		public void detect(Vector<File> fileRoots) throws InterruptedException, ExecutionException
		{
			for (File single : fileRoots)
			{
				fileScanList = new Vector<File>();
				fileQueue = new ArrayBlockingQueue<File>(5000);
				rootScanner = new DirectoryScanner(fileQueue, 5, 20);
				fileTypeQueue = new LinkedBlockingQueue<File>();
				fileReaders = new QueueProcessor<File, File>(fileQueue, fileTypeQueue, 5, new IProcessor<File, File>()
				{
					// Identify the file type and all found loaded onto fileReaders
					public File process(final File input)
					{
						try
						{
							if (input.isDirectory())
								return null;
							if (input.isFile())
							{
								display.asyncExec(new Runnable()
								{
									public void run()
									{
										Label refLabel = labelList.get(Math.abs(getRandom(new Random(), 0, 5)));
										refLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
										refLabel.setText(" Reading          : " + input.getName());
									}
								});
								if (fileType.check(input) && !fileScanList.contains(input))
								{
									fileScanList.add(input);
									return input;
								}
								else
									return null;
							}
						}
						catch (Exception e)
						{
							// e.printStackTrace();
							return null;
						}
						return null;
					}
				});
				outputQueue = new LinkedBlockingQueue<FileWithCard>();
				fileChecker = new QueueProcessor<File, FileWithCard>(fileTypeQueue, outputQueue, 5, new IProcessor<File, FileWithCard>()
				{
					public FileWithCard process(final File input)
					{
						try
						{
							display.asyncExec(new Runnable()
							{
								public void run()
								{
									Label refLabel = labelList.get(Math.abs(getRandom(new Random(), 0, 5)));
									refLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN));
									refLabel.setText(" Searching        : " + input.getName());
									// refLabel.setText(" Searching        : " + input.getAbsolutePath());
								}
							});
							return fileType.search(input);
						}
						catch (Exception e)
						{
							// e.printStackTrace();
							return null;
						}
					}
				});
				// Start the file readers.
				fileReaders.startup();
				// Start the file finders.
				fileChecker.startup();
				// Start the thread that dumps the matching output to the console.
				dumpedCount = new AtomicInteger(0);
				dumpService = Executors.newFixedThreadPool(10);
				new Vector<FileWithCard>();
				Thread dumpingThread = new Thread(new Runnable()
				{
					public void run()
					{
						try
						{
							while (true)
							{
								final FileWithCard foundFileWithCards = outputQueue.take();
								dumpService.submit(new Runnable()
								{
									public void run()
									{
										display.asyncExec(new Runnable()
										{
											public void run()
											{
												if (!fwcList.contains(foundFileWithCards) && foundFileWithCards.getCardsOnThisFile().size() > 0)
												{
													fwcList.add(foundFileWithCards);
													fFNumber.setText(String.valueOf(fwcList.size()));
													foundCount += foundFileWithCards.getCardsOnThisFile().size();
													cFNumber.setText(String.valueOf(foundCount));
													TableItem newRow = new TableItem(filesWithCards, SWT.NONE);
													newRow.setText(0, foundFileWithCards.getFileWithCardNumber().getAbsolutePath());
													newRow.setText(1, String.valueOf(foundFileWithCards.getCardsOnThisFile().size()));
													if (fwcList.size() == 1)
													{
														filesWithCards.setSelection(0);
														for (CardFound cf : foundFileWithCards.getCardsOnThisFile())
														{
															TableItem newCardRow = new TableItem(cardsInTheFile, SWT.NONE);
															newCardRow.setText(0, cf.cardNumber);
															newCardRow.setText(1, cf.card.cardCode);
														}
													}
												}
											}
										});
									}
								});
								dumpedCount.incrementAndGet();
							}
						}
						catch (InterruptedException exc)
						{
							display.asyncExec(new Runnable()
							{
								public void run()
								{
									for (Label lb : labelList)
									{
										lb.setText(" DONE");
										lb.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
									}
									if (fwcList.size() > 0)
										btnExportAll.setEnabled(true);
									btnStop.setEnabled(false);
									btnStart.setEnabled(true);
									progressBar.setVisible(false);
									shlCreditCardChecker.setCursor(defaultIndicator);
									opIndicator.setImage(SWTResourceManager.getImage(CCCheckerPro.class, "/gnuc/stop.png"));
								}
							});
							return;
						}
					}
				});
				dumpingThread.start();
				final int workItemCount0 = rootScanner.scan(single);
				int workItemCount1 = fileReaders.waitFor(workItemCount0);
				int workItemCount2 = fileChecker.waitFor(workItemCount1);
				while (true)
				{
					if (dumpedCount.get() == workItemCount2)
					{
						dumpingThread.interrupt();
						break;
					}
					Thread.sleep(100);
				}
				dumpingThread.join();
				dumpService.shutdownNow();
			}
		}
	}
}
