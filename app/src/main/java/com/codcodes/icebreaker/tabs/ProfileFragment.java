package com.codcodes.icebreaker.tabs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.screens.Edit_ProfileActivity;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.screens.InitialActivity;
import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.screens.RewardsActivity;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.android.gms.internal.zzir.runOnUiThread;


/**
 * Created by tevin on 2016/07/13.
 */
public class ProfileFragment extends android.support.v4.app.Fragment
{
    private static AssetManager mgr;
    private TextView name;
    private TextView age;
    private TextView occupation;
    private String profilePicture;
    private String Name;
    private String Age;
    private String Occupation;
    private String Bio;
    private String Catchphrase;
    private String Gender;
    private ImageView circularImageView;
    private User user;
    private View v;
    private Bitmap circularbitmap = null;
    private static final boolean DEBUG = false;
    private final String TAG = "IB/ProfileFragment";
    private static boolean CHUNKED = false;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        validateStoragePermissions(getActivity());

        v = inflater.inflate(R.layout.fragment_profile, container, false);
        //TODO: Use this information to send to database to see whch user it is.
        final String username = SharedPreference.getUsername(v.getContext()).toLowerCase();

        Typeface h = Typeface.createFromAsset(mgr, "Infinity.ttf");
        name = (TextView) v.findViewById(R.id.profile_name);
        name.setTypeface(h);


        age = (TextView) v.findViewById(R.id.profile_age);
        age.setTypeface(h);

        occupation = (TextView) v.findViewById(R.id.profile_occupation);
        occupation.setTypeface(h);


        final TextView rewards = (TextView) v.findViewById(R.id.profile_Rewards);
        rewards.setTypeface(h);
        rewards.setText("Rewards");



        TextView settings = (TextView) v.findViewById(R.id.profile_settings);
        settings.setTypeface(h);
        settings.setText("Settings");


        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                Bitmap bitmap = null;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                ArrayList<User> userList = null;
                String usrJson = null;
                try
                {
                    System.err.println("Preparing to read user from db");
                    usrJson = getUserJson(username);
                    //System.out.println(usrJson);
                    userList = new ArrayList<>();
                    JSON.<User>getJsonableObjectsFromJson(usrJson,userList,User.class);
                } catch (IOException e)
                {
                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    Log.d(TAG,e.getMessage());
                    //TODO: Error Logging
                    try
                    {
                        Thread.sleep(3000);
                    } catch (InterruptedException e1)
                    {
                        e1.printStackTrace();
                    }
                    System.exit(-1);
                } catch (java.lang.InstantiationException e)
                {
                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    Log.d(TAG,e.getMessage());
                    //TODO: Error Logging
                } catch (IllegalAccessException e)
                {
                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    Log.d(TAG,e.getMessage());
                    //TODO: Error Logging
                }
                //user = readUser(username);
                if(userList==null)
                {
                    //TODO: Notify user
                    Log.d(TAG,"Something went wrong while we were trying to read your profile.");
                }
                else if(userList.isEmpty())
                {
                    Toast.makeText(getActivity(),"Your username was not found",Toast.LENGTH_LONG).show();
                    Log.d(TAG,"Username '" + username + "' not found on DB");
                    //TODO: Error Logging
                }
                else if(userList.size()>1)//More than one user returned - unlikely but it doesn't hurt to be sure
                {
                    Toast.makeText(getActivity(),"Your username was not found",Toast.LENGTH_LONG).show();
                    Log.d(TAG,"Found multiple users for user '" + username + "' on DB");
                    //TODO: Error Logging
                }
                else//All is well
                {
                    user= userList.get(0);
                    Name = user.getFirstname() + " " + user.getLastname();
                    Age = String.valueOf(user.getAge());
                    Occupation = user.getOccupation();

                    Bio = user.getBio();
                    Catchphrase = user.getCatchphrase();
                    Gender = user.getGender();
                    profilePicture = "/Icebreak/profile/" + username + ".png";
                    //Look for user profile image
                    if (!new File(Environment.getExternalStorageDirectory().getPath()
                            + profilePicture).exists())
                    {
                        //if (imageDownload(u.getUsername() + ".png", "/profile")) {
                        if (imageDownloader(username + ".png", "/profile"))
                        {
                            bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                                    + profilePicture, options);
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
                                + profilePicture, options);
                        circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                    }
                }

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        age.setText(Age);
                        name.setText(Name);
                        occupation.setText(Occupation);
                        circularImageView = (ImageView) v.findViewById(R.id.circleview);
                        circularImageView.setImageBitmap(circularbitmap);
                    }
                });
            }
        });
        thread.start();


        rewards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int link_color = Color.parseColor("#4665f0");
                Intent intent = new Intent(view.getContext(), RewardsActivity.class);
                // rewards.startAnimation();
                startActivity(intent);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), InitialActivity.class);
                startActivity(intent);
            }
        });

        ImageView reward_icon = (ImageView) v.findViewById(R.id.rewards_icon);

        int color = Color.parseColor("#46bdf0");
        reward_icon.setColorFilter(color);

        ImageView setting_icon = (ImageView) v.findViewById(R.id.setting_icon);
        setting_icon.setColorFilter(color);

        FloatingActionButton editButton = (FloatingActionButton) v.findViewById(R.id.Edit);
        editButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Intent intent = new Intent(view.getContext(), Edit_ProfileActivity.class);
                intent.putExtra("First Name",user.getFirstname());
                intent.putExtra("Last Name", user.getLastname());
                intent.putExtra("Age",Age);
                intent.putExtra("Occupation",Occupation);
                intent.putExtra("Bio",Bio);
                intent.putExtra("Catchphrase",Catchphrase);
                intent.putExtra("Gender",Gender);
                startActivity(intent);
            }
        });

        Button logOut = (Button) v.findViewById(R.id.logOut);
        logOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SharedPreference.logOut(view.getContext());
                Intent intent = new Intent(view.getContext(), InitialActivity.class);
                startActivity(intent);

            }
        });
        return v;
    }

    public static boolean imageDownloader(String image, String destPath)
    {
        try
        {
            System.out.println("Attempting to download image: " + image);
            Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
            System.out.println("Connection established, Sending request..");
            PrintWriter out = new PrintWriter(soc.getOutputStream());
            //Android: final String base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
            String headers = "GET /IBUserRequestService.svc/imageDownload/"+image+" HTTP/1.1\r\n"
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

    public static String getUserJson(String username) throws IOException
    {
        URL urlConn = urlConn = new URL("http://icebreak.azurewebsites.net/IBUserRequestService.svc/getUser/" + username);
        HttpURLConnection httpConn =  (HttpURLConnection)urlConn.openConnection();
        //httpConn.setRequestProperty("Transfer-Encoding","chunked");
        Scanner s = new Scanner(httpConn.getInputStream());
        String response = "";

        while (s.hasNextLine())
            response += s.nextLine();

        if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
        {
            System.out.println(response);
            return response;
        }else
            return  "";
    }

    public User readUser(String username)
    {
        try {
            Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
            Log.d(TAG, "Connection established");

            PrintWriter out = new PrintWriter(soc.getOutputStream());
            Log.d(TAG, "Sending request");
            //String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");

            out.print("GET /IBUserRequestService.svc/getUser/" + username + " HTTP/1.1\r\n"
                    + "Host: icebreak.azurewebsites.net\r\n"
                    + "Content-Type: text/plain;\r\n"
                    + "Content-Length: 0\r\n\r\n");
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            String resp;
            //Wait for response indefinitely TODO: Time-Out
            while (!in.ready()) {}

            String userJson = "";
            boolean openUserRead = false;
            while ((resp = in.readLine()) != null)
            {
                if (DEBUG) System.out.println(resp);

                if (resp.equals("0")) {
                    out.close();
                    //in.close();
                    soc.close();
                    if (DEBUG) System.out.println(">>Done<<");
                    break;//EOF
                }

                if (resp.isEmpty())
                    if (DEBUG) System.out.println("\n\nEmpty Line\n\n");

                if (resp.contains("{")) {
                    if (DEBUG) System.out.println("Opening at>>" + resp.indexOf("{"));
                    openUserRead = true;
                }

                if (openUserRead)
                    userJson += resp;//.substring(resp.indexOf('['));

                if (resp.contains("}")) {
                    if (DEBUG) System.out.println("Closing at>>" + resp.indexOf("}"));
                    openUserRead = false;
                }
            }
            return getUser(userJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void validateStoragePermissions(Activity activity) {
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE =
                {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
        //Check for write permissions
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //No permission - prompt the user for permission
            ActivityCompat.requestPermissions
                    (
                            activity,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE
                    );
        }
    }

    public static ProfileFragment newInstance(Context context) {
        ProfileFragment e = new ProfileFragment();
        mgr = context.getAssets();
        Bundle b = new Bundle();
        e.setArguments(b);
        return e;
    }

    public static boolean imageDownload(String filename) throws IOException {
        Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
        System.out.println("Sending image download request");
        PrintWriter out = new PrintWriter(soc.getOutputStream());

        String headers = "GET /IBUserRequestService.svc/imageDownload/" + filename + " HTTP/1.1\r\n"
                + "Host: icebreak.azurewebsites.net\r\n"
                + "Content-Type: text/plain;charset=utf-8\r\n"
                + "Content-Length: 0\r\n\r\n";
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
        while ((resp = in.readLine()) != null) {
            System.out.println(resp);
            if (resp.toLowerCase().contains("transfer-encoding")) {
                String encoding = resp.split(":")[1];
                if (encoding.toLowerCase().contains("chunked")) {
                    CHUNKED = true;
                    System.out.println("Preparing for chunked data.");
                }
            }

            if (CHUNKED) {
                Matcher m = pattern.matcher(resp.toUpperCase());
                if (m.find()) {
                    int dec = hexToDecimal(m.group(0));
                    String chunk = in.readLine();
                    //char[] chunk = new char[dec];
                    //int readCount = in.read(chunk,0,chunk.length);//sjv3
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
        if (payload.length() > 0) {
            //payload = payload.split(":")[1];
            payload = payload.replaceAll("\"", "");
            //System.out.println(payload)
            byte[] binFileArr = android.util.Base64.decode(payload, Base64.NO_PADDING);
            WritersAndReaders.saveImage(binFileArr, filename);
            return true;
        } else {
            return false;
        }
    }

    public static int hexToDecimal(String hex) {
        String possibleDigits = "0123456789ABCDEF";
        int dec = 0;
        for (int i = 0; i < hex.length(); i++) {
            char currChar = hex.charAt(i);
            int x = possibleDigits.indexOf(currChar);
            dec = 16 * dec + x;
        }
        return dec;
    }

    private static User getUser(String json)
    {
        System.out.println("Reading User: " + json);
        //TODO: Regex fo user string

        int endPos = json.indexOf("}");
        int startPos = json.indexOf("{");
        System.out.println(startPos + " to " + endPos);
        String userJson = json.substring(startPos, endPos + 1);

        String p2 = "\"([a-zA-Z0-9\\s~`!@#$%^&*)(_+-={}\\[\\];',./\\|<>?]*)\"\\:(\"[a-zA-Z0-9\\s~`!@#$%^&*()_+-={}\\[\\];',./\\|<>?]*\"|\"[0-9,]\"|\\d+)";
        Pattern p = Pattern.compile(p2);
        Matcher m = p.matcher(userJson);
        User user = new User();
        while (m.find()) {
            String pair = m.group(0);
            //process key value pair
            pair = pair.replaceAll("\"", "");
            if (pair.contains(":")) {
                //if(DEBUG)System.out.println("Found good pair");
                String[] kv_pair = pair.split(":");
                String var = kv_pair[0];
                String val = kv_pair[1];
                switch (var) {
                    case "Fname":
                        user.setFirstname(val);
                        break;
                    case "Lname":
                        user.setLastname(val);
                        break;
                    case "Age":
                        user.setAge(Integer.valueOf(val));
                        break;
                    case "Occupation":
                        user.setOccupation(val);
                        break;
                    case "Bio":
                        user.setBio(val);
                        break;
                    case "Catchphrase":
                        user.setCatchphrase(val);
                        break;
                    case "Gender":
                        user.setGender(val);
                        break;

                }
            }
            //look for next pair
            json = json.substring(m.end());
            m = p.matcher(json);
        }
        return user;
    }
}