package com.codcodes.icebreaker.auxilary;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.codcodes.icebreaker.screens.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;


public class WritersAndReaders 
{
	private static String TAG = "IB/WritersAndReaders";

	public static void saveImage(byte[] data,String filename)
	{
        File f=null;
        String folders = "";
		/**Directory creation**/
        if(filename.contains("/") || filename.contains("\\"))
        {
            //Remove first slash if it exists
            folders = (filename.charAt(0)=='/'||filename.charAt(0)=='\\') ? filename.substring(1) : filename;
            String[] dirs = folders.split("/");//get directories
            String directories = "/";
            for(int i=0;i<dirs.length-1;i++)
                directories = directories +"/" + dirs[i];

            f = new File(MainActivity.rootDir + "/Icebreak/" + directories);

            if(!f.isDirectory())
                Log.d(TAG,f.getPath() + " directory creation: " + f.mkdirs());

            f = new File(MainActivity.rootDir + "/Icebreak/" + filename);
        }else
        {
            /*
             * For rare but possible cases where the image is to be saved on the app's root directory
             * In such cases the filename variable will not have a preceding '/'
             */
            f = new File(MainActivity.rootDir + "/Icebreak/" + filename);
        }

		try
		{
            FileOutputStream fos = new FileOutputStream(f.getPath().toString());
            fos.write(data);
			fos.flush();
            fos.close();
			Log.d(TAG,"Saved image to disk: " + f.getPath().toString());
		}
		catch (IOException e)
		{
            //TODO: better logging
			Log.d(TAG,"Could not write file: " + e.getMessage());
            e.printStackTrace();
		}
	}

    public static void writeAttributeToConfig(String key, String value) throws IOException
    {
        File f = new File(MainActivity.rootDir + "/Icebreak/config.cfg");
        StringBuilder result = new StringBuilder();
        if(f.exists())
        {
            String s = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            while ((s = in.readLine())!=null)
            {
                if(s.contains("="))
                {
                    String var = s.split("=")[0];
                    String val = s.split("=")[1];
                    if(var.equals(key))
                        val = value;
                    result.append(var+"="+val);
                }
            }
            in.close();
        }
        else
        {
            result.append(key+"="+value);
        }
        PrintWriter out = new PrintWriter(f);
        out.print(result.toString());
        out.flush();
        out.close();
    }

    public static String readAttributeFromConfig(String key) throws IOException
    {
        File f = new File(MainActivity.rootDir + "/Icebreak/config.cfg");
        if(f.exists())
        {
            String s = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            while ((s = in.readLine())!=null)
            {
                if(s.contains("="))
                {
                    String var = s.split("=")[0];
                    String val = s.split("=")[1];
                    if(var.equals(key))
                        return val;
                }
            }
            in.close();
        }
        return null;
    }

    public static String getRandomIdStr(int length)
    {
        String id = "";
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for(int i=0;i<length;i++)
        {
            Random r = new Random();
            id += chars.charAt(r.nextInt(chars.length()));
        }

        return id;
    }

	//Clients will use this - only have one Item instance to keep track of - theirs
	/*public static void saveItem(Item item,String filename)
	{
        //make directory if it doesn't exist
        File f = new File(path + "/The_Mediator/");
        if(!f.isDirectory())
            f.mkdir();

		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(path + "/The_Mediator/" + filename)));
			oos.writeObject(item);
			oos.flush();
			oos.close();
			Log.d("W&R","Saved item to disk: " + path + "/" + filename);
		}
		catch (IOException e)
		{
			System.err.println("Could not save backup: " + e.getMessage());
		}
	}
	
	//Server will use this - has many Item instances to keep track of
	public static void saveItems(ArrayList<Item> items,String filename)
	{
        //make directory if it doesn't exist
        File f = new File(path + "/The_Mediator/");
        if(!f.isDirectory())
            f.mkdir();

		try 
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(path + "/The_Mediator/"+filename)));
			oos.writeObject(items);
			oos.flush();
			oos.close();
			System.out.println("Saved items to disk: "  + path + "/" + filename);
		} 
		catch (IOException e) 
		{
			System.err.println("Could not save backup: " + e.getMessage());
		}
	}
	
	//Both server & clients
	public static void saveMessages(ArrayList<Message> messages,String filename)
	{
        //make directory if it doesn't exist
        File f = new File(path + "/The_Mediator/");
        if(!f.isDirectory())
            f.mkdir();

		//Write to disk
		try 
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(path + "/The_Mediator/"+filename)));
			oos.writeObject(messages);
			oos.flush();
			oos.close();
			System.out.println("Saved messages to disk: " + path + "/" + filename);
		} 
		catch (IOException e) 
		{
			System.err.println("Could not save backup: " + e.getMessage());
		}
	}
	
	//Clients will use this - only have one Item instance to keep track of - theirs
	@SuppressWarnings("unchecked")
	public static Item loadItem(String filename)
	{
        //make directory if it doesn't exist
        File f = new File(path + "/The_Mediator/");
        if(!f.isDirectory())
            f.mkdir();

		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(path + "/The_Mediator/"+filename)));
			Item item = (Item)ois.readObject();
			ois.close();
			System.out.println("Loaded item from disk: " + item);
			return item;
		} 
		catch (FileNotFoundException e2) 
		{
			System.err.println("No locally saved " + filename + " - creating a new one: " + e2.getMessage());
		}
		catch (IOException e2) 
		{
			System.err.println("IO Error: " + e2.getMessage());
		} 
		catch (ClassNotFoundException e1) 
		{
			System.err.println("This copy of the program is missing some files: " + e1.getMessage());
		}
		return null;
	}
	
	//Server will use this - has many Item instances to keep track of
	public static ArrayList<Item> loadItems(String filename)
	{
        //make directory if it doesn't exist
        File f = new File(path + "/The_Mediator/");
        if(!f.isDirectory())
            f.mkdir();

		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(path + "/The_Mediator/" + filename)));
			ArrayList<Item> items = (ArrayList<Item>)ois.readObject();
			ois.close();
			System.out.println("Loaded items from disk: " + items.size());
			return items;
		} 
		catch (FileNotFoundException e2) 
		{
			System.err.println("No locally saved " + filename + " - creating a new one: " + e2.getMessage());
		}
		catch (IOException e2) 
		{
			System.err.println("IO Error: " + e2.getMessage());
		} 
		catch (ClassNotFoundException e1) 
		{
			System.err.println("This copy of the program is missing some files: " + e1.getMessage());
		}
		return null;
	}
	
	//Both server & clients
	@SuppressWarnings("unchecked")
	public static ArrayList<Message> loadMessages(String filename)
	{
        //make directory if it doesn't exist
        File f = new File(path + "/The_Mediator/");
        if(!f.isDirectory())
            f.mkdir();

		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(path + "/The_Mediator/" + filename)));
			ArrayList<Message> messages = (ArrayList<Message>)ois.readObject();
			ois.close();
			System.out.println("Loaded messages from disk: " + messages.size());
			return messages;
		} 
		catch (FileNotFoundException e2) 
		{
			System.err.println("No locally saved "+filename+" - creating a new one: " + e2.getMessage());
		}
		catch (IOException e2) 
		{
			System.err.println("IO Error: " + e2.getMessage());
		} 
		catch (ClassNotFoundException e1) 
		{
			System.err.println("This copy of the program is missing some files: " + e1.getMessage());
		}
		return null;
	}*/
}
