package com.codcodes.icebreaker.tabs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.auxilary.ImageUtils;
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.auxilary.UserContactsRecyclerViewAdapter;
import com.codcodes.icebreaker.model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.android.gms.internal.zzir.runOnUiThread;
/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link IOnListFragmentInteractionListener}
 * interface.
 */
public class UserContactsFragment extends Fragment
{

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String TAG = "IB/UserContactsFragment";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private static boolean CHUNKED = false;
    private IOnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UserContactsFragment()
    {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static UserContactsFragment newInstance(int columnCount)
    {
        UserContactsFragment fragment = new UserContactsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    public static UserContactsFragment newInstance(Context context, Bundle b)
    {
        UserContactsFragment instance = new UserContactsFragment();
        instance.setArguments(b);
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_usercontacts_list, container, false);
        View rview = null;
        if(view != null)
            rview = view.findViewById(R.id.userContactList);
        // Set the adapter
        if (rview instanceof RecyclerView)
        {
            Context context = view.getContext();
            final RecyclerView recyclerView = (RecyclerView) rview;
            if (mColumnCount <= 1)
            {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            }
            else
            {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            Thread tContactsLoader = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Looper.prepare();

                    try
                    {
                        //String query = "/IBUserRequestService.svc/getUserContacts";//imageDownload/profile_ghost.png
                        URLConnection httpConn = null;
                        httpConn = new URL("http://icebreak.azurewebsites.net/IBUserRequestService.svc/getUserContacts").openConnection();
                        //httpConn.setRequestProperty("Accept-Charset", java.nio.charset.StandardCharsets.UTF_8.name());
                        Scanner s = new Scanner(httpConn.getInputStream());
                        String response = "";
                        while (s.hasNextLine())
                            response += s.nextLine();

                        final ArrayList<User> contacts = new ArrayList<>();
                        JSON.<User>getJsonableObjectsFromJson(response, contacts, User.class);
                        System.err.println(contacts.size());
                        //Attempt to load images into memory and set the list adapter
                        /*try
                        {*/
                            final ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
                            Bitmap circularbitmap = null;
                            Bitmap bitmap = null;
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ALPHA_8;

                            for (User u : contacts)
                            {
                                //Look for user profile image
                        if (!new File(Environment.getExternalStorageDirectory().getPath()
                                + "/Icebreak/profile/" + u.getUsername() + ".png").exists())
                        {
                            //if (imageDownload(u.getUsername() + ".png", "/profile")) {
                            if (imageDownloader(u.getUsername() + ".png", "/profile"))
                            {
                                bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                                        + "/Icebreak/profile/" + u.getUsername() + ".png", options);
                                //Bitmap bitmap = ImageUtils.getInstant().compressBitmapImage(holder.getView().getResources(),R.drawable.blue);
                                circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                            } else //user has no profile yet - attempt to load default profile image
                            {
                                if (!new File(Environment.getExternalStorageDirectory().getPath().toString()
                                        + "/Icebreak/profile/profile_default.png").exists())
                                {
                                    //Attempt to download default profile image
                                    if (imageDownloader("profile_default.png", "/profile"))
                                    {
                                        bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                                                + "/Icebreak/profile/profile_default.png", options);
                                        //Bitmap bitmap = ImageUtils.getInstant().compressBitmapImage(holder.getView().getResources(),R.drawable.blue);
                                        circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                                    } else //Couldn't download default profile image
                                    {
                                        Toast.makeText(getActivity(), "Could not download default profile images, please check your internet connection.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                } else//default profile image exists
                                {
                                    bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                                            + "/Icebreak/profile/profile_default.png", options);
                                    //Bitmap bitmap = ImageUtils.getInstant().compressBitmapImage(holder.getView().getResources(),R.drawable.blue);
                                    circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                                }
                            }
                        } else//user profile image exists
                        {
                            bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                                    + "/Icebreak/profile/" + u.getUsername() + ".png", options);
                            circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                        }
                        if(bitmap == null || circularbitmap == null)
                        {
                            System.err.println("Bitmap is null");
                        }
                        else
                        {
                            bitmaps.add(circularbitmap);
                            bitmap.recycle();
                        }
                    }
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    recyclerView.setAdapter(new UserContactsRecyclerViewAdapter(contacts, bitmaps, mListener));
                                }
                            });
                        /*}
                        catch (IOException e)
                        {
                            //TODO: Loggging
                            e.printStackTrace();
                        }*/
                    }
                    catch (IOException e)
                    {
                        //TODO: Error Logging
                        e.printStackTrace();
                    }
                    catch (java.lang.InstantiationException e)
                    {
                        //TODO: Error Logging
                        e.printStackTrace();
                    }
                    catch (IllegalAccessException e)
                    {
                        //TODO: Error Logging
                        e.printStackTrace();
                    }
                }
            });
            tContactsLoader.start();
        }
        return view;
    }

    public static boolean imageDownloader(String image, String destPath)
    {
      /*  try
        {
            URL urlConn = urlConn = new URL("http://icebreak.azurewebsites.net/IBUserRequestService.svc/imageDownload/profile_default.png");//+image);
            //httpConn.setRequestProperty("Accept-Charset", java.nio.charset.StandardCharsets.UTF_8.name());

            HttpURLConnection httpConn =  (HttpURLConnection)urlConn.openConnection();

                    //(HttpURLConnection)urlConn;
            //httpConn.setChunkedStreamingMode(8192);
            httpConn.setRequestProperty("Transfer-Encoding","chunked");
            Scanner s = new Scanner(httpConn.getInputStream());
            String response = "";

            while (s.hasNextLine())
                response += s.nextLine();

            if(httpConn.getResponseCode() == 200)
            {
                if (response.length() > 0 && !response.equals("FNE"))
                {
                    response = response.replaceAll("\"", "");
                    System.out.println(response);
                    if(response.contains(":"))
                    {
                        String base64 = response.split(":")[1];
                        System.err.println(base64);
                        byte[] binFileArr = android.util.Base64.decode(response, android.util.Base64.DEFAULT);
                        WritersAndReaders.saveImage(binFileArr, destPath + "/" + image);
                        return true;
                    }
                    else
                    {
                        System.err.println(httpConn.getResponseMessage() + "> Response doesn't have ':', can't split");
                        return false;
                    }
                } else
                {
                    System.err.println("FNE/Not Found >> " + httpConn.getResponseMessage());
                    return false;
                }
            }
            else
            {
                System.err.println("404 " + httpConn.getResponseMessage());
                return false;
            }
        }
        catch (FileNotFoundException e)
        {
            Log.d(TAG, "FNE:    " + e.getMessage() + "\n");
            //TODO: Logging
            return false;
        }
        catch (IOException e)
        {
            Log.d(TAG, "IOE:    " + e.getMessage());
            //TODO: Logging
            return false;
        }
*/

        try
        {
            System.out.println("Attempting to download image: " + image);
            Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
            System.out.println("Connection established, Sending request..");
            PrintWriter out = new PrintWriter(soc.getOutputStream());
            //Android: final String base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
            /*String headers = "GET /IBUserRequestService.svc/imageDownload/" + image + " HTTP/1.1\r\n"
                    + "Host: icebreak.azurewebsites.net\r\n"
                    //+ "Content-Type: application/x-www-form-urlencoded\r\n"
                    + "Content-Type: text/plain;\r\n"// charset=utf-8
                    + "Content-Length: 0\r\n\r\n";*/
            String headers = "GET /IBUserRequestService.svc/imageDownload/"+image+" HTTP/1.1\r\n"
                    + "Host: icebreak.azurewebsites.net\r\n"
                    //+ "Content-Type: application/x-www-form-urlencoded\r\n"
                    + "Content-Type: text/plain;charset=utf-8;\r\n\r\n";
                    //+ "Content-Length: 0\r\n\r\n";

            out.print(headers);
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            String resp, base64;
            while (!in.ready()) {
            }
            Pattern pattern = Pattern.compile("^[A-F0-9]+$");//"((\\d*[A-Fa-f]\\d*){2,}|\\d{1})");//"([0-9A-Fa-f]{2,}|[0-9]{1})");//"[0-9A-Fa-f]");
            //System.out.println(pattern.matcher("4FA3").find());
            //System.out.println(hexToDecimal("7D0"));
            String payload = "";
            while ((resp = in.readLine()) != null)
            {
                //System.out.println(resp);
                if (resp.toLowerCase().contains("400 bad request"))
                {
                    System.out.println("<<<400 bad request>>>");
                    return false;
                }
                if (resp.toLowerCase().contains("404 not found"))
                {
                    System.out.println("<<<404 not found>>>");
                    return false;
                }
                if (resp.toLowerCase().contains("transfer-encoding"))
                {
                    String encoding = resp.split(":")[1];
                    if (encoding.toLowerCase().contains("chunked"))
                    {
                        CHUNKED = true;
                        System.out.println("Preparing for chunked data.");
                    }
                }

                if (CHUNKED)
                {
                    Matcher m = pattern.matcher(resp.toUpperCase());
                    if (m.find())
                    {
                        int dec = hexToDecimal(m.group(0));
                        String chunk = in.readLine();
                        //char[] chunk = new char[dec];
                        //int readCount = in.read(chunk,0,chunk.length);//sjv3
                        //System.out.println(chunk);
                        //System.out.println("Chunk size: "+ readCount);
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
                WritersAndReaders.saveImage(binFileArr, destPath + "/" + image);
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

    public static boolean imageDownload(String imageName, String desDir) throws IOException
    {
        try
        {
            URLConnection urlConn = null;
            urlConn = new URL("http://icebreak.azurewebsites.net/IBUserRequestService.svc/imageDownload/"+imageName).openConnection();
            //httpConn.setRequestProperty("Accept-Charset", java.nio.charset.StandardCharsets.UTF_8.name());
            HttpURLConnection httpConn = (HttpURLConnection)urlConn;
            httpConn.setChunkedStreamingMode(2048);
            //Scanner s = new Scanner(urlConn.getInputStream());
            String response = httpConn.getResponseMessage();
            /*while (s.hasNextLine())
                response += s.nextLine();*/

            if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                if (response.length() > 0 && !response.equals("FNE"))
                {
                    response = response.replaceAll("\"", "");
                    if(response.contains(":")) {
                        String base64 = response.split(":")[1];
                        System.err.println(base64);
                        byte[] binFileArr = android.util.Base64.decode(response, android.util.Base64.DEFAULT);
                        WritersAndReaders.saveImage(binFileArr, desDir + "/" + imageName);
                        return true;
                    }
                    else
                    {
                        System.err.println(httpConn.getResponseMessage());
                        return false;
                    }
                } else
                {
                    System.err.println("FNE/Not Found >> " + httpConn.getResponseMessage());
                    return false;
                }
            }
            else
            {
                System.err.println("404 " + httpConn.getResponseMessage());
                return false;
            }
        }
        catch (FileNotFoundException e)
        {
            Log.d(TAG, "FNE:    " + e.getMessage());
            //TODO: Logging
            return false;
        }

        /*Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
        System.out.println("Sending image download request");
        PrintWriter out = new PrintWriter(soc.getOutputStream());
        //Android: final String base64 = ;
        String headers = "GET /IBUserRequestService.svc/imageDownload/" + imageName + " HTTP/1.1\r\n"
                + "Host: icebreak.azurewebsites.net\r\n"
                //+ "Content-Type: application/x-www-form-urlencoded\r\n"
                + "Content-Type: text/plain;\r\n"// charset=utf-8
                + "Content-Length: 0\r\n\r\n";
        out.print(headers);
        out.flush();

        BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
        String resp,base64;
        while(!in.ready()){}
        Pattern pattern = Pattern.compile("^[A-F0-9]+$");//"((\\d*[A-Fa-f]\\d*){2,}|\\d{1})");//"([0-9A-Fa-f]{2,}|[0-9]{1})");//"[0-9A-Fa-f]");
        String payload = "";
        while((resp = in.readLine())!=null)
        {
            //System.out.println(resp);
            if(resp.toLowerCase().contains("transfer-encoding"))
            {
                String encoding = resp.split(":")[1];
                if(encoding.toLowerCase().contains("chunked"))
                {
                    CHUNKED = true;
                    System.out.println("Preparing for chunked data.");
                }
            }

            if(CHUNKED)
            {
                Matcher m = pattern.matcher(resp.toUpperCase());
                if(m.find())
                {
                    int dec = hexToDecimal(m.group(0));
                    String chunk = in.readLine();
                    //char[] chunk = new char[dec];
                    //int readCount = in.read(chunk,0,chunk.length);//sjv3
                    //System.out.println(chunk);
                    //System.out.println("Chunk size: "+ readCount);
                    if(dec==0)
                        break;//End of chunks
                    if(chunk.length()>0)
                        payload += chunk;//String.copyValueOf(chunk);
                }
            }
        }
        out.close();
        //in.close();
        soc.close();
        if(payload.length()>0)
        {
            //payload = payload.split(":")[1];
            payload = payload.replaceAll("\"", "");
            //System.out.println(payload)
            byte[] binFileArr = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);
            WritersAndReaders.saveImage(binFileArr,desDir + "/" + imageName);
            return true;
        }
        else
        {
            return false;
        }*/
    }

    public static int hexToDecimal(String hex)
    {
        String possibleDigits = "0123456789ABCDEF";
        int dec = 0;
        for(int i=0;i<hex.length();i++)
        {
            char currChar = hex.charAt(i);
            int x = possibleDigits.indexOf(currChar);
            dec = 16*dec + x;
        }
        return dec;
    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof IOnListFragmentInteractionListener)
        {
            mListener = (IOnListFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString()
                    + " must implement IOnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }
}
