/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gwaspi.gui;


// A simple Java Console for your application (Swing version)
// Requires Java 1.1.5 or higher
//
// Disclaimer the use of this source is at your own risk.
//
// Permission to use and distribute into your own applications
//
// RJHM van den Bergh , rvdb@comweb.nl

 import java.io.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;

 public class Console extends WindowAdapter implements WindowListener,  ActionListener, Runnable
 {
	private JFrame frame;
	private JTextArea textArea;
	private Thread readerThread;
	private Thread readerThread2;
	private boolean quit;

	private final PipedInputStream pipInputStream=new PipedInputStream();
	private final PipedInputStream pipInputStream2=new PipedInputStream();


	public Console()
	{
		// create all components and add them
		frame=new JFrame("Java Console");
		Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize=new Dimension((int)(screenSize.width/2),(int)(screenSize.height/2));
		int x=(int)(frameSize.width/2);
		int y=(int)(frameSize.height/2);
		frame.setBounds(x,y,frameSize.width,frameSize.height);

		textArea=new JTextArea();
		textArea.setEditable(false);
		JButton button=new JButton("Close");

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new JScrollPane(textArea),BorderLayout.CENTER);
		frame.getContentPane().add(button,BorderLayout.SOUTH);
		frame.setVisible(true);

		frame.addWindowListener(this);
		//button.addActionListener(this);
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frame.setVisible(false);
            }
        });

		try
		{
			PipedOutputStream pout=new PipedOutputStream(this.pipInputStream);
			System.setOut(new PrintStream(pout,true));
		}
		catch (java.io.IOException io)
		{
			textArea.append("Couldn't redirect STDOUT to this console\n"+io.getMessage());
		}
		catch (SecurityException se)
		{
			textArea.append("Couldn't redirect STDOUT to this console\n"+se.getMessage());
        	}

		try
		{
			PipedOutputStream pout2=new PipedOutputStream(this.pipInputStream2);
			System.setErr(new PrintStream(pout2,true));
		}
		catch (java.io.IOException io)
		{
			textArea.append("Couldn't redirect STDERR to this console\n"+io.getMessage());
		}
		catch (SecurityException se)
		{
			textArea.append("Couldn't redirect STDERR to this console\n"+se.getMessage());
		}

		quit=false; // signals the Threads that they should exit

		// Starting two separate threads to read from the PipedInputStreams
		//
		readerThread=new Thread(this);
		readerThread.setDaemon(true);
		readerThread.start();
		//
		readerThread2=new Thread(this);
		readerThread2.setDaemon(true);
		readerThread2.start();

	}

	public synchronized void windowClosed(WindowEvent evt)
	{
		quit=true;
//		this.notifyAll(); // stop all threads
//		try { reader.join(1000);pin.close();   } catch (Exception e){}
//		try { reader2.join(1000);pin2.close(); } catch (Exception e){}
//		System.exit(0);
	}
	public synchronized void windowClosing(WindowEvent evt)
	{
		frame.setVisible(false); // default behaviour of JFrame
		frame.dispose();
	}

	public synchronized void actionPerformed(ActionEvent evt)
	{
		textArea.setText("");
	}

	public synchronized void run()
	{
		try
		{
			while (Thread.currentThread()==readerThread)
			{
				try { this.wait(100);}catch(InterruptedException ie) {}
				if (pipInputStream.available()!=0)
				{
					String input=this.readLine(pipInputStream);
					textArea.append(input);
				}
				if (quit) return;
			}

			while (Thread.currentThread()==readerThread2)
			{
				try { this.wait(100);}catch(InterruptedException ie) {}
				if (pipInputStream2.available()!=0)
				{
					String input=this.readLine(pipInputStream2);
					textArea.append(input);
				}
				if (quit) return;
			}
		} catch (Exception e)
		{
			textArea.append("\nConsole reports an Internal error.");
			textArea.append("The error is: "+e);
		}

	}

	public synchronized String readLine(PipedInputStream in) throws IOException
	{
		String input="";
		do
		{
			int available=in.available();
			if (available==0) break;
			byte b[]=new byte[available];
			in.read(b);
			input=input+new String(b,0,b.length);
		}while( !input.endsWith("\n") &&  !input.endsWith("\r\n") && !quit);
		return input;
	}

	public static void main(String[] arg)
	{
		new Console(); // create console with not reference
	}
}

