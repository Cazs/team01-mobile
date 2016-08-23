package com.codcodes.icebreaker.auxilary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.screens.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Casper on 2016/08/08.
 */
public class RemoteComms
{
    private static final String TAG = "IB/RemoteComms";
    private static boolean CHUNKED = false;

    public static String sendGetRequest(String url) throws IOException
    {
        url = url.charAt(0)=='/'||url.charAt(0)=='\\'?url.substring(1):url;//Remove first slash if it exists
        Log.d(TAG,"Opening connection to IceBreak_domain/Service/" + url);
        URL urlConn = new URL("http://icebreak.azurewebsites.net/IBUserRequestService.svc/" + url);
        HttpURLConnection httpConn =  (HttpURLConnection)urlConn.openConnection();
        Scanner s = new Scanner(httpConn.getInputStream());
        String response = "";

        while (s.hasNextLine())
            response += s.nextLine();

        if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
        {
            return response;
        }else
            return  "";
    }

    public static User getUser(Context context, String username) throws IOException
    {
        String userJson = RemoteComms.sendGetRequest("getUser/"+username);
        User u = new User();
        if(userJson!=null)
            JSON.getJsonable(userJson, u);
        else return null;
        u.setUsername(username);
        LocalComms.addContact(context,u);
        return u;
    }

    public static int postData(String function, ArrayList<AbstractMap.SimpleEntry<String,String>> params) throws IOException
    {
        function = function.charAt(0)=='/'||function.charAt(0)=='\\'?function.substring(1):function;//Remove first slash if it exists
        URL urlConn = new URL("http://icebreak.azurewebsites.net/IBUserRequestService.svc/" + function);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestMethod("POST");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        //Encode URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        for(int i=0;i<params.size();i++)
        {
            AbstractMap.SimpleEntry<String,String> entry = params.get(i);
            result.append(URLEncoder.encode(entry.getKey(),"UTF-8") + "=");
            result.append(URLEncoder.encode(entry.getValue(),"UTF-8") + (i!=params.size()-1?"&":""));
        }
        //System.out.println(result);
        //Write to server
        OutputStream os = httpConn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
        writer.write(result.toString());
        writer.flush();
        writer.close();
        os.close();

        httpConn.connect();

        /*Scanner scn = new Scanner(new InputStreamReader(httpConn.getErrorStream()));
        String resp = "";
        while(scn.hasNext())
            resp+=scn.nextLine();
        System.err.println(resp);*/

        return httpConn.getResponseCode();
    }

    private static boolean imageDownloader(String image, String ext, String destPath, Context context)
    {
        //Check for invalid filenames
        if(image==null || ext == null)
        {
            Log.d(TAG, "The image filename or extension is null");
            return false;
        }
        if(image.equals("null"))
        {
            Log.d(TAG, "The image filename is null");
            return false;
        }

        if(image.isEmpty() || ext.isEmpty())
        {
            Log.d(TAG, "The image filename or extension is empty");
            return false;
        }
        if(!ext.contains("."))//add dot to image extension if it's not there
            ext = '.' + ext;
        try
        {
            Log.d(TAG,"Attempting to download image: " + image + ext);
            Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
            System.out.println("Connection established, Sending request..");
            PrintWriter out = new PrintWriter(soc.getOutputStream());
            String headers = "GET /IBUserRequestService.svc/imageDownload/"+image+ext+" HTTP/1.1\r\n"
                    + "Host: icebreak.azurewebsites.net\r\n"
                    + "Content-Type: text/plain;charset=utf-8;\r\n\r\n";

            out.print(headers);
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            String resp, base64;
            while (!in.ready()) {
            }
            Pattern pattern = Pattern.compile("^[A-F0-9]+$");//"((\\d*[A-Fa-f]\\d*){2,}|\\d{1})");//"([0-9A-Fa-f]{2,}|[0-9]{1})");//"[0-9A-Fa-f]");
            String payload = "";
            while ((resp = in.readLine()) != null)
            {
                //System.out.println(resp);
                if (resp.toLowerCase().contains("400 bad request"))
                {
                    //System.out.println("<<<400 bad request>>>");
                    return false;
                }
                if (resp.toLowerCase().contains("404 not found"))
                {
                    //System.out.println("<<<404 not found>>>");
                    return false;
                }
                if (resp.toLowerCase().contains("transfer-encoding"))
                {
                    String encoding = resp.split(":")[1];
                    if (encoding.toLowerCase().contains("chunked"))
                    {
                        CHUNKED = true;
                        Log.d(TAG,"Preparing for chunked data.");
                    }
                }

                if (CHUNKED)
                {
                    Matcher m = pattern.matcher(resp.toUpperCase());
                    if (m.find())
                    {
                        int dec = hexToDecimal(m.group(0));
                        String chunk = in.readLine();
                        if (dec == 0)
                            break;//End of chunks
                        if (chunk.length() > 0)
                            payload += chunk;//String.copyValueOf(chunk);
                    }
                }
            }
            out.close();
            //in.close();
            soc.close();

            payload = payload.substring(0,payload.length()-1);
            if(!payload.equals("FNE") && payload.length()>0)
            {
                //System.out.println(payload);
                payload = payload.split(":")[1];
                payload = payload.replaceAll("\"", "");

                byte[] binFileArr = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);//Base64.getDecoder().decode(payload.getBytes());
                WritersAndReaders.saveImage(binFileArr, destPath + "/" + image + ext);
                System.out.println("Succesfully wrote to disk");//"\n>>>>>"+base64bytes);
                return true;
            }
            else
            {
                //TODO: Throw FileNotFoundException
                System.err.println("Server> File not found");
                return false;
            }
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
            return  false;
        }
    }

    public static int hexToDecimal(String hex)
    {
        String possibleDigits = "0123456789ABCDEF";
        int dec = 0;
        for (int i = 0; i < hex.length(); i++) {
            char currChar = hex.charAt(i);
            int x = possibleDigits.indexOf(currChar);
            dec = 16 * dec + x;
        }
        return dec;
    }

    /*public static Bitmap getImage(Context context, String filename,String ext, String path, BitmapFactory.Options options)
    {
        //path = MainActivity.rootDir + "/Icebreak" + path;
        path = path.charAt(0) != '/' && path.charAt(0) != '\\' ? '/' + path : path;
        Bitmap bitmap = null;
        if(!ext.contains("."))//add dot to image extension if it's not there
            ext = '.' + ext;
        //Look for image locally
        if (!new File(path + '/' + filename + ext).exists())
        {
            if (RemoteComms.imageDownloader(filename, ext, path, context))
            {
                bitmap = BitmapFactory.decodeFile(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, options);
                //Bitmap bitmap = ImageUtils.getInstant().compressBitmapImage(holder.getView().getResources(),R.drawable.blue);
            } else //user has no profile yet - attempt to load default profile image
            {
                if (!new File(path + "/default.png").exists())
                {
                    //Attempt to download default profile image
                    if (RemoteComms.imageDownloader("default", ".png", "/profile", context))
                    {
                        bitmap = BitmapFactory.decodeFile(MainActivity.rootDir + "/Icebreak" + path + "/default.png", options);
                    } else //Couldn't download default profile image
                    {
                        Toast.makeText(context, "Could not download default image, please check your internet connection.",
                                Toast.LENGTH_LONG).show();
                    }
                } else//default profile image exists
                {
                    System.err.println("default.png image exists");
                    bitmap = BitmapFactory.decodeFile(MainActivity.rootDir + "/Icebreak" + path + "/default.png", options);
                }
            }
        }
        else//User profile exists
        {
            System.err.println(filename+".png image exists");
            bitmap = BitmapFactory.decodeFile(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, options);
        }
        return bitmap;
    }*/

    public static Bitmap getImage(Context context, String filename,String ext, String path, BitmapFactory.Options options)
    {
        path = path.charAt(0) != '/' && path.charAt(0) != '\\' ? '/' + path : path;
        Bitmap bitmap = null;
        if(!ext.contains("."))//add dot to image extension if it's not there
            ext = '.' + ext;

        if (RemoteComms.imageDownloader(filename, ext, path, context))
            bitmap = LocalComms.getImage(context,filename,ext,path,options);
        else Log.d(TAG,"Image could not be downloaded.");//TODO: better logging
        return bitmap;
    }

    public static boolean sendMessage(Context context, Message m)
    {
        /*System.err.println(String.format("id=%s, msg=%s, stat=%s, send=%s, recv=%s",m.getId(),m.getMessage(),
                m.getStatus(),m.getSender(),m.getReceiver()));*/

        ArrayList<AbstractMap.SimpleEntry<String, String>> msg_details = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_id", m.getId()));
        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message", m.getMessage()));
        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_status", String.valueOf(m.getStatus())));
        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_sender", m.getSender()));//TODO
        //msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_time", m.getTime()));//TODO
        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_receiver", m.getReceiver()));//TODO

        //Send to server
        try
        {
            final int response_code = RemoteComms.postData("addMessage", msg_details);
            if(response_code != HttpURLConnection.HTTP_OK)
            {
                Log.d(TAG,"Could not send request: " + response_code);
                //TODO: Better logging
                return false;
            }
            else
            {
                Log.d(TAG,"Message Sent");
                return true;
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
            //TODO: Better logging
        }
        return false;
    }
}
