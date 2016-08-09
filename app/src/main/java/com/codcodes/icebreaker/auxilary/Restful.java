package com.codcodes.icebreaker.auxilary;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Casper on 2016/08/08.
 */
public class Restful
{
    private static final String TAG = "IB/Restful";
    private static boolean CHUNKED = false;

    public static String getJsonFromURL(String url) throws IOException
    {
        Log.d(TAG,"Opening connection to IceBreak_domain/Service/" + url);
        URL urlConn = urlConn = new URL("http://icebreak.azurewebsites.net/IBUserRequestService.svc/" + url);
        HttpURLConnection httpConn =  (HttpURLConnection)urlConn.openConnection();
        //httpConn.setRequestProperty("Transfer-Encoding","chunked");
        Scanner s = new Scanner(httpConn.getInputStream());
        String response = "";

        while (s.hasNextLine())

            response += s.nextLine();
        System.out.println(response);
        if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
        {
            return response;
        }else
            return  "";
    }

    public static boolean imageDownloader(String image, String ext, String destPath, Activity context)
    {

        //Check for storage permissions
        validateStoragePermissions(context);
        //Check for invalid filenames
<<<<<<< HEAD
        if( image==null || ext == null)
=======
        if(image==null || ext == null)
>>>>>>> Temp
        {
            Log.d(TAG, "The image filename or extension is null");
            return false;
        }
<<<<<<< HEAD
        if (image.equals("null"))
        {
            return  false;
        }
=======
        if(image.equals("null"))
        {
            Log.d(TAG, "The image filename is null");
            return false;
        }

>>>>>>> Temp
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

            //System.out.println(payload);
            payload = payload.split(":")[1];
            payload = payload.replaceAll("\"", "");

            payload = payload.substring(0,payload.length()-1);
            if(!payload.equals("FNE"))
            {
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

    public static void validateStoragePermissions(Activity activity)
    {
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE =
                {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
        //Check for write permissions
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            //No permission - prompt the user for permission
            ActivityCompat.requestPermissions
                    (
                            activity,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE
                    );
        }
    }
}
