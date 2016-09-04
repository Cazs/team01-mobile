package com.codcodes.icebreaker.auxilary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.codcodes.icebreaker.model.Event;
import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.screens.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

import javax.net.ssl.HttpsURLConnection;

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

    public static byte[] getFBImage(String host, String resource) throws IOException
    {
        Log.d(TAG,"Opening connection to " + host);
        URL urlConn = new URL(host+'/'+resource);
        HttpsURLConnection httpConn =  (HttpsURLConnection)urlConn.openConnection();
        httpConn.setRequestMethod("GET");
        //httpConn.setDoOutput(true);
        //httpConn.setDoInput(true);
        //httpConn.setChunkedStreamingMode(1024);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        DataInputStream is = new DataInputStream(httpConn.getInputStream());

        int len;
        while ((len=is.read(buffer,0,buffer.length))>0)
        {
            baos.write(buffer,0,len);
            baos.flush();
        }
        is.close();
        //if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
        return baos.toByteArray();
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

    public static Event getEvent(long event_id) throws IOException
    {
        String eventJson = RemoteComms.sendGetRequest("getEvent/"+event_id);
        Event e = new Event();
        if(eventJson!=null)
            JSON.getJsonable(eventJson, e);
        else return null;
        return e;
    }

    public static int imageUpload(byte[] bitmap, String remote_filename,String ext) throws IOException
    {
        String payload = bytearrayToBase64(bitmap);

        /*ArrayList<AbstractMap.SimpleEntry<String,String>> params = new ArrayList<>();
        params.add(new AbstractMap.SimpleEntry<String,String>
                (new AbstractMap.SimpleEntry<String, String>("filename",remote_filename + ext)));
        params.add(new AbstractMap.SimpleEntry<String,String>
                (new AbstractMap.SimpleEntry<String, String>("payload",payload)));*/

        URL urlConn = new URL("http://icebreak.azurewebsites.net/IBUserRequestService.svc/imgUpload/"+remote_filename + ext);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestMethod("PUT");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        //Write to server
        OutputStream os = httpConn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
        writer.write(bytearrayToBase64(bitmap));
        writer.flush();
        writer.close();
        os.close();

        httpConn.connect();

        /*Scanner scn = new Scanner(new InputStreamReader(httpConn.getErrorStream()));
        String resp = "";
        while(scn.hasNext())
            resp+=scn.nextLine();
        System.err.println(TAG+": "+resp);*/
        int rcode = httpConn.getResponseCode();
        httpConn.disconnect();
        return rcode;
    }

    public static String bytearrayToBase64(byte[] img_data) throws IOException
    {
        String base64 = Base64.encodeToString(img_data,Base64.DEFAULT);
        return base64;
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

    public static String postData(String function, String params) throws IOException
    {
        function = function.charAt(0)=='/'||function.charAt(0)=='\\'?function.substring(1):function;//Remove first slash if it exists
        URL urlConn = new URL("http://icebreak.azurewebsites.net/IBUserRequestService.svc/" + function);
        HttpURLConnection httpConn = (HttpURLConnection)urlConn.openConnection();
        httpConn.setReadTimeout(10000);
        httpConn.setConnectTimeout(15000);
        httpConn.setRequestMethod("POST");
        httpConn.setDoInput(true);
        httpConn.setDoOutput(true);

        //Write to server
        OutputStream os = httpConn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
        writer.write(params);
        writer.flush();
        writer.close();
        os.close();

        httpConn.connect();

        Scanner scn=null;
        if(httpConn.getResponseCode()==HttpURLConnection.HTTP_OK)
             scn = new Scanner(new InputStreamReader(httpConn.getInputStream()));
        else
            scn = new Scanner(new InputStreamReader(httpConn.getErrorStream()));
        String resp = "";
        if(scn!=null)
            while(scn.hasNext())
                resp+=scn.nextLine();
        //System.err.println(resp);

        return httpConn.getResponseCode() + ":" + resp;
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
            //Do some formatting, prep for server side
            if(destPath.charAt(0)=='/'||destPath.charAt(0)=='\\')destPath = destPath.substring(1);
            if(destPath.contains("/"))destPath = destPath.replaceAll("/","|");
            if(destPath.contains("\\"))destPath = destPath.replaceAll("\\\\","|");

            String headers = "GET /IBUserRequestService.svc/imageDownload/"+destPath + '|' + image+ext+" HTTP/1.1\r\n"
                    + "Host: icebreak.azurewebsites.net\r\n"
                    + "Content-Type: text/plain;charset=utf-8;\r\n\r\n";

            out.print(headers);
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            String resp, base64;
            //TODO: set a timeout
            while (!in.ready()) {}//wait indefinitely
            Pattern pattern = Pattern.compile("^[A-F0-9]+$");//"((\\d*[A-Fa-f]\\d*){2,}|\\d{1})");//"([0-9A-Fa-f]{2,}|[0-9]{1})");//"[0-9A-Fa-f]");
            String payload = "";
            while ((resp = in.readLine()) != null)
            {
                //System.out.println(resp);
                if (resp.toLowerCase().contains("400 bad request"))
                    return false;
                if (resp.toLowerCase().contains("404 not found"))
                    return false;

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

                byte[] binFileArr = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);
                WritersAndReaders.saveImage(binFileArr, destPath + "/" + image + ext);
                Log.d(TAG,"Image download complete");
                return true;
            }
            else throw new FileNotFoundException("Server> File '"+image+ext+"' was not found");
        }
        catch (IOException e)
        {
            Log.d(TAG,e.getMessage(),e);
            return  false;
        }
    }

    public static int hexToDecimal(String hex)
    {
        String possibleDigits = "0123456789ABCDEF";
        int dec = 0;
        for (int i = 0; i < hex.length(); i++)
        {
            char currChar = hex.charAt(i);
            int x = possibleDigits.indexOf(currChar);
            dec = 16 * dec + x;
        }
        return dec;
    }

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
        ArrayList<AbstractMap.SimpleEntry<String, String>> msg_details = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_id", m.getId()));
        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message", m.getMessage()));
        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_status", String.valueOf(m.getStatus())));
        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_sender", m.getSender()));
        //msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_time", m.getTime()));
        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_receiver", m.getReceiver()));

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
            Log.wtf(TAG, e.getMessage(),e);
            //TODO: Better logging
        }
        return false;
    }
}
