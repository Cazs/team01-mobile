package com.codcodes.icebreaker.auxilary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.codcodes.icebreaker.model.Event;
import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.screens.MainActivity;
import com.google.android.gms.maps.model.LatLng;

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

        String response = null;
        if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
        {
            response="";
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            String line="";
            int read=0;
            while ((line=in.readLine())!=null)
                response += line;
            //Log.d(TAG,response);
        }else
        {
            response="";
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
            String line="";
            int read=0;
            while ((line=in.readLine())!=null)
                response += line;
        }
        return response;
    }

    public static boolean pingServer(String username) throws IOException
    {
        String response = sendGetRequest("ping/" + username);
        if(response.toLowerCase().contains("success"))
            return true;
        else return false;
    }

    public ArrayList<Event> getNearbyEvents(double lat, double lng, double range) throws InstantiationException, IllegalAccessException, IOException
    {

        String response = sendGetRequest(String.format("getNearbyEvents/%s/%s/%s",
                String.valueOf(lat), String.valueOf(lng), String.valueOf(range)));
        ArrayList<Event> events = new ArrayList<>();
        JSON.<Event>getJsonableObjectsFromJson(response,events,Event.class);
        return events;
    }

    public static void logOutUserFromEvent(Context context) throws IOException
    {
        //Update status on server
        User u = LocalComms.getContact(context,SharedPreference.getUsername(context));
        if(u==null)
            u = RemoteComms.getUser(context,SharedPreference.getUsername(context));
        if(u!=null)
        {
            Event e = new Event();
            e.setId(0);
            u.setEvent(e);
            String res = RemoteComms.postData("userUpdate/"+u.getUsername(),u.toString());
            if(res.contains("200"))
            {
                WritersAndReaders.writeAttributeToConfig(Config.EVENT_ID.getValue(),"0");
                Log.d(TAG,"Successfully updated user Event status locally and remotely.");
            }else Log.wtf(TAG,"Could not update Event status of User on remote DB.");
        }
    }

    public static Drawable getGoogleMapsBitmap(double lat, double lng, int zoom, int w, int h, Event event) throws IOException
    {
        if(event.isValid())
        {
            double lat_offset = 0.00005;
            double lng_offset = 0.00000;

            String event_coords = "";

            for (LatLng pos : event.getBoundary())
                event_coords += pos.latitude + "," + pos.longitude + "|";
            if (event_coords.length() > 0)
                event_coords = event_coords.substring(0, event_coords.length() - 1);//remove last pipe
            String url = "https://maps.google.com/maps/api/staticmap?" +
                    "center=" + lat + "," + lng + "&" +
                    "zoom=" + zoom + "&" +
                    "size=" + w + "x" + h + "&" +
                    "sensor=true&" +
                    "markers=icon:" + URLEncoder.encode("http://icebreak.azurewebsites.net/images/public_res/ic_gps_fixed_black_24dp.png", "UTF-8") + "|" + lat + "," + lng + "&";
            if (event.getOrigin().latitude != 0.0 && event.getOrigin().longitude != 0.0)
            {
                url += "markers=icon:" + URLEncoder.encode("http://icebreak.azurewebsites.net/images/public_res/dot.png", "UTF-8") + "|" + event.getOrigin().latitude + "," + event.getOrigin().longitude + "&";
                url += "path=color:0x0000ff|weight:5|" + (lat + lat_offset) + "," + (lng + lng_offset) + "|" + (event.getOrigin().latitude + lat_offset) + "," + (event.getOrigin().longitude + lng_offset) + "&";
            }
            url += "path=color:0x0000ff|weight:5|" + event_coords;

            Log.d(TAG, "Opening connection to http://maps.google.com...");
            URL urlConn = new URL(url);
            HttpsURLConnection httpConn = (HttpsURLConnection) urlConn.openConnection();
            httpConn.setRequestMethod("GET");
            //httpConn.setDoOutput(true);
            //httpConn.setDoInput(true);
            //httpConn.setChunkedStreamingMode(1024);
        /*ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        DataInputStream is = new DataInputStream(httpConn.getInputStream());

        int len;
        while ((len=is.read(buffer,0,buffer.length))>0)
        {
            baos.write(buffer,0,len);
            baos.flush();
        }
        is.close();*/
        /*Bitmap bmp = null;
        bmp = BitmapFactory.decodeStream(httpConn.getInputStream());
        if(httpConn.getInputStream()!=null)
            httpConn.getInputStream().close();*/
            //if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)

            return Drawable.createFromStream(httpConn.getInputStream(), "google_maps_bitmap");
        }else
        {
            Log.d(TAG,"getGoogleMapsBitmap> Invalid Event.");
            return null;
        }
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
        //String payload = bytearrayToBase64(bitmap);

        /*ArrayList<AbstractMap.SimpleEntry<String,String>> params = new ArrayList<>();
        params.add(new AbstractMap.SimpleEntry<String,String>
                (new AbstractMap.SimpleEntry<String, String>("filename",remote_filename + ext)));
        params.add(new AbstractMap.SimpleEntry<String,String>
                (new AbstractMap.SimpleEntry<String, String>("payload",payload)));*/

        System.err.println(remote_filename + ext);
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
            if(entry!=null)
            {
                if(entry.getKey()!=null && entry.getValue()!=null)
                {
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8") + "=");
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8") + (i != params.size() - 1 ? "&" : ""));
                }else return -1;
            }else return -1;
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
        httpConn.setReadTimeout(20000);
        httpConn.setConnectTimeout(20000);
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

        if (scn != null)
            while (scn.hasNextLine())
                resp += scn.nextLine();
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

                try
                {
                    byte[] binFileArr = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);
                    WritersAndReaders.saveImage(context, binFileArr, destPath + "/" + image + ext);
                    Log.d(TAG, "Image download complete");
                    return true;
                }catch (IllegalArgumentException e)
                {
                    Log.d(TAG,e.getMessage(),e);
                    return false;
                }
            }
            else throw new FileNotFoundException("Server> File '"+image+ext+"' was not found");
        }
        catch (IOException e)
        {
            Log.d(TAG,"IOE: " + e.getMessage(),e);
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

    public static Bitmap getImage(Context context, String filename,String ext, String path, BitmapFactory.Options options) throws IOException
    {
        path = path.charAt(0) != '/' && path.charAt(0) != '\\' ? '/' + path : path;
        Bitmap bitmap = null;
        if(!ext.contains("."))//add dot to image extension if it's not there
            ext = '.' + ext;

        Log.d(TAG,"***Downloading image [~"+path+'/'+filename+ext+"]***");

        if (RemoteComms.imageDownloader(filename, ext, path, context))
            bitmap = LocalComms.getImage(context,filename,ext,path,options);
        else Log.d(TAG,"Image could not be downloaded.");//TODO: better logging
        return bitmap;
    }

    public static boolean sendMessage(Context context, Message m) throws IOException
    {
        ArrayList<AbstractMap.SimpleEntry<String, String>> msg_details = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_id", m.getId()));
        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message", m.getMessage()));
        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_status", String.valueOf(m.getStatus())));
        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_sender", m.getSender()));
        //msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_time", m.getTime()));
        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_receiver", m.getReceiver()));

        //Send to server
        final int response_code = RemoteComms.postData("addMessage", msg_details);
        if(response_code != HttpURLConnection.HTTP_OK)
        {
            Log.d(TAG,"Could not send request: " + response_code);
            return false;
        }
        else
        {
            Log.d(TAG,"Message Sent");
            return true;
        }
    }
}
