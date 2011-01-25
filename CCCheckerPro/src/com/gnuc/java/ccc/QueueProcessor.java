
package com.gnuc.java.ccc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.swt.widgets.Label;

/**
 * Abstract class models a set of threads consuming inputs of type <tt>I</tt> from an input queue, and writing outputs of type <tt>O</tt> to an output queue.
 * 
 * @author sdasgupta
 * @param <I>
 * @param <O>
 */
public class QueueProcessor<I, O>
{
	/**
	 * Private class polls for and extracts messages from {@link QueueProcessor#inputQueue}, processes each message using {@link QueueProcessor#process(Object)}
	 * and places the resulting object (if it is not <tt>null</tt>) into {@link QueueProcessor#outputQueue}.
	 * 
	 * @author sdasgupta
	 */
	private final class ConsumerRunnable implements Runnable
	{
		public void run()
		{
			while (true)
			{
				try
				{
					final I input = QueueProcessor.this.inputQueue.poll(100, TimeUnit.MILLISECONDS);
					if (input != null)
					{
						pushService.submit(new Runnable()
						{
							public void run()
							{
								try
								{
									O result = processor.process(input);
									if (result != null)
									{
										QueueProcessor.this.outputQueue.put(result);
										producedMessageCount.incrementAndGet();
									}
								}
								catch (InterruptedException e)
								{
									Thread.currentThread().interrupt();
									for (Label lb : CCCheckerPro.get().getLabelList())
										lb.setText("     STOPPED");
									return;
								}
								catch (InvocationTargetException exc)
								{
									return;
								}
							}
						});
						consumedMessageCount.incrementAndGet();
					}
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
					return;
				}
			}
		}
	}
	/**
	 * The number of threads consuming messages from {@link QueueProcessor#inputQueue}.
	 */
	private final int threadCount;
	/**
	 * The threads consuming messages from {@link QueueProcessor#inputQueue}.
	 */
	private final List<Thread> consumerThreads;
	/**
	 * The queue from which input messages are read.
	 */
	private final BlockingQueue<I> inputQueue;
	/**
	 * The queue to which output messages are written.
	 */
	private final BlockingQueue<O> outputQueue;
	/**
	 * Processes inputs (pulled from the <tt>inputQueue</tt>) and pushes the results (obtained by calling {@link #process(Object)}) to the <tt>outputQueue</tt>.
	 */
	private final ExecutorService pushService;
	/**
	 * Counts the total number of consumed messages.
	 */
	private final AtomicInteger consumedMessageCount = new AtomicInteger(0);
	/**
	 * Counts the total number of produced messages.
	 */
	private final AtomicInteger producedMessageCount = new AtomicInteger(0);
	private final IProcessor<I, O> processor;
	
	public void hardKill() throws InterruptedException
	{
		pushService.shutdownNow();
		while (true)
		{
			if (pushService.isTerminated())
			{
				break;
			}
			Thread.sleep(100);
		}
		for (Thread thread : consumerThreads)
		{
			thread.interrupt();
		}
		for (Thread thread : consumerThreads)
		{
			thread.join();
		}
	}
	
	/**
	 * Public constructor.
	 * 
	 * @param inputQueue
	 * @param outputQueue
	 * @param threadCount
	 */
	public QueueProcessor(BlockingQueue<I> inputQueue, BlockingQueue<O> outputQueue, int threadCount, IProcessor<I, O> processor)
	{
		super();
		this.inputQueue = inputQueue;
		this.outputQueue = outputQueue;
		this.threadCount = threadCount;
		this.processor = processor;
		pushService = Executors.newFixedThreadPool(10);
		consumerThreads = new ArrayList<Thread>();
	}
	
	// protected abstract O process(I input) throws InterruptedException,
	// InvocationTargetException;
	public void startup()
	{
		// Create the consumer threads.
		for (int i = 0; i < this.threadCount; i++)
		{
			consumerThreads.add(new Thread(new ConsumerRunnable()));
		}
		for (Thread thread : consumerThreads)
		{
			thread.start();
		}
	}
	
	/**
	 * Waits for <tt>inputMessageCount</tt> messages to be processed, invokes {@link QueueProcessor#shutdown()}, and returns
	 * {@link QueueProcessor#producedMessageCount}.
	 * 
	 * @param inputMessageCount
	 * @return
	 * @throws InterruptedException
	 */
	public int waitFor(int inputMessageCount) throws InterruptedException
	{
		while (true)
		{
			if (consumedMessageCount.get() >= inputMessageCount)
			{
				break;
			}
			Thread.sleep(100);
		}
		shutdown();
		return producedMessageCount.get();
	}
	
	/**
	 * Issues a shutdown request to the {@link QueueProcessor#pushService}, waits for that service to shut down, interrupts the
	 * {@link QueueProcessor#consumerThreads}, waits for those threads to shut down, then returns.
	 * <p>
	 * All messages which have already been consumed are processed.
	 * </p>
	 * 
	 * @throws InterruptedException
	 */
	public void shutdown() throws InterruptedException
	{
		pushService.shutdown();
		while (true)
		{
			if (pushService.isTerminated())
			{
				break;
			}
			Thread.sleep(100);
		}
		for (Thread thread : consumerThreads)
		{
			thread.interrupt();
		}
		for (Thread thread : consumerThreads)
		{
			thread.join();
		}
	}
}
