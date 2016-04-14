package com.codcodes.icebreaker;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;

public class Backend implements Runnable 
{
	private Socket client;
	private InetAddress host;
	private int port;
	private Context appContext;
	
	public Backend(Context appContext, InetAddress host, int port)/*TODO: logger*/
	{
		this.host = host;
		this.port = port;
		this.appContext = appContext;
	}
	
	public void sendMessage(String msg)
	{
		try
		{
			client = new Socket(host, port);
			PrintWriter out = new PrintWriter(client.getOutputStream());
			out.println(msg);
			out.flush();
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
		}
	}
	
	public void startListenerService() throws IOException
	{
		while(true)
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			while(!in.ready()){}
			String line = in.readLine();
			System.out.println(line);
			StringTokenizer tokenizer = new StringTokenizer(line," ");
			String cmd = tokenizer.nextToken();
			switch(cmd)
			{
				case "HTTP/1.1":
					String code = "";
					while(tokenizer.hasMoreTokens())
						code+=tokenizer.nextToken();
					//logger.println(code);
					//logger.flush();
					break;
				default:
					Toast.makeText(appContext, "Unknown Protocol", Toast.LENGTH_LONG).show();
					break;
			}
		}
	}
	
	public BufferedReader getResponse() throws IOException
	{
		return new BufferedReader(new InputStreamReader(client.getInputStream()));
	}

	@Override
	public void run() 
	{
		try 
		{
			startListenerService();
		} 
		catch (IOException e) 
		{
			System.err.println(e.getMessage());
		}
	}
}
