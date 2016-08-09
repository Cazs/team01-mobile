package com.codcodes.icebreaker.screens;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.CustomListAdapter;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.auxilary.Restful;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.model.Event;
import com.codcodes.icebreaker.model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.android.gms.internal.zzir.runOnUiThread;

public class EventDetailActivity extends AppCompatActivity {


    private static final boolean DEBUG = true;
    private final String TAG = "ICEBREAK";

    private int Eventid;
    private ArrayList<User> users;
    private ArrayList<String> Name;
    private ArrayList<String> Catchphrase;
    private ArrayList<String> userIcon;
    private int AccessCode;
    private ListView lv;
    private ViewFlipper vf;
    private TextView eventDetails;
    private ProgressDialog progress;
    private static boolean CHUNKED = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       // getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Name = new ArrayList<String>();
        Catchphrase= new ArrayList<String>();
        userIcon = new ArrayList<String>();
        final String username = SharedPreference.getUsername(getApplicationContext());

        Bundle extras = getIntent().getExtras();
        final Activity act =this;

        if(extras != null)
        {
            String evtName = extras.getString("Event Name");
            TextView eventName = (TextView)findViewById(R.id.event_name);
            eventName.setText(evtName);


            Eventid = extras.getInt("Event ID");
            AccessCode = extras.getInt("Access ID");
            System.out.println(AccessCode);

            TextView eventDescription = (TextView)findViewById(R.id.event_description);
            eventDescription.setText(extras.getString("Event Description"));

            String imagePath = Environment.getExternalStorageDirectory().getPath().toString()
                    + extras.getString("Image ID");

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ALPHA_8;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imagePath);
            Bitmap circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, 100);
            ImageView eventImage = (ImageView) findViewById((R.id.event_image));

            eventImage.setImageBitmap(circularbitmap);
            bitmap.recycle();
        }



        eventDetails = (TextView)findViewById(R.id.Event_Heading);
        Typeface heading = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        eventDetails.setTypeface(heading);
        lv= (ListView) findViewById(R.id.contactList);
        final EditText accessCode = (EditText) findViewById(R.id.AccessCode);
        accessCode.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionID, KeyEvent event)
            {
                if (actionID== EditorInfo.IME_ACTION_DONE)
                {
                    if(matchAccessCode(Integer.parseInt(accessCode.getText().toString())))
                    {
                        download();
                        updateProfile(Eventid,username);
                        listPeople(act);

                    }
                    else
                    {
                        accessCode.setError("Invalid Access Code Entered");
                    }
                }
                return false;
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(TAG,"Clicked on item: " + position);
                User user = users.get(position);
                // TODO Auto-generated method stub
                /*String Selcteditem = EventNames[+position];
                String eventDescrip = EventDescrp[+position];
                int imageID = imgid[+position];
                */

                Intent intent = new Intent(view.getContext(),Other_Profile.class);
                intent.putExtra("First Name",user.getFirstname());
                intent.putExtra("Last Name",user.getLastname());
                intent.putExtra("Age",user.getAge());
                intent.putExtra("Gender",user.getGender());
                intent.putExtra("Occupation",user.getOccupation());
                intent.putExtra("Bio",user.getBio());
                intent.putExtra("ImageID",userIcon.get(position));

                startActivity(intent);
            }
        });



    }

    public void download(){
        progress=new ProgressDialog(this);
        progress.setMessage("Loading List");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setProgress(0);
        progress.show();
    }

    public void updateProfile(final int eventID,final String username)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try
                {
                    Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
                    System.out.println("Connection established");
                    PrintWriter out = new PrintWriter(soc.getOutputStream());
                    System.out.println("Sending request");

                    String data = URLEncoder.encode("event_ID", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(eventID), "UTF-8");

                    out.print("POST /IBUserRequestService.svc/userUpdate/"+username+" HTTP/1.1\r\n"
                            + "Host: icebreak.azurewebsites.net\r\n"
                            + "Content-Type: text/plain; charset=utf-8\r\n"
                            + "Content-Length: " + data.length() + "\r\n\r\n"
                            + data);
                    out.flush();

                    BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                    String resp;
                    boolean found = false;
                    while((resp = in.readLine())!=null)
                    {
                        if (DEBUG) System.out.println(resp);
                        Log.d("ICEBREAK",resp);
                        if(resp.contains("HTTP/1.1 200 OK"))
                        {
                            Log.d("ICEBREAK","Found HTTP attr");
                            found = true;
                            break;
                        }
                    }

                    out.close();
                    in.close();
                }
                catch (UnknownHostException e)
                {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "No internet access", Toast.LENGTH_LONG).show();

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("com.codcodes.icebreaker.Back",true);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

    public boolean matchAccessCode(int code)
    {
        if(code == AccessCode)
        {
            return true;
        }
        return false;
    }



    private void listPeople(final Activity act)
    {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                users = readUsersAtEvents(Integer.toString(Eventid));

                if(users==null)
                {
                    //TODO: Notify user
                    Log.d(TAG,"Something went wrong while we were trying to read the events.");
                }
                else if(users.isEmpty())
                {
                    //TODO: Notify user
                    Log.d(TAG,"No users were found");
                }
                else//All is well
                {
                   //Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
                        //Log.d(TAG,"Connection established");
                        for(User u:users)
                        {
                            Name.add(u.getFirstname()+" "+ u.getLastname());
                            Catchphrase.add(u.getCatchphrase());
                            //eventIcons.add(R.drawable.ultra_icon);//temporarily use this icon for all events
                            String iconName = "profile_"+u.getUsername()+".png";
                            //String iconName = "event_icons-10.png";
                            userIcon.add("/Icebreak/"+iconName);
                            //Download the file only if it has not been cached
                            if(!new File(Environment.getExternalStorageDirectory().getPath()+"/Icebreak/users/" + iconName).exists())
                            {
                                Log.d(TAG,"No cached "+iconName+",Image download in progress..");
                                if(Restful.imageDownloader(iconName,".png", "/users",act))
                                    Log.d(TAG,"Image download successful");
                                else
                                    Log.d(TAG,"Image download unsuccessful");
                            }
                        }

                   /*String[] eventNamesArr = (String[])eventNames.toArray();
                   Integer[] eventIconsArr = (Integer[])eventIcons.toArray();
                   String[] eventDescriptionsArr = (String[])eventDescriptions.toArray();*/
                    String[] userNamesArr = new String[users.size()];
                    String[] userIconsArr = new String[users.size()];
                    String[] catchphrase = new String[users.size()];
                    userNamesArr = Name.toArray(userNamesArr);
                    catchphrase = Catchphrase.toArray(catchphrase);
                    userIconsArr = userIcon.toArray(userIconsArr);

                   /*Object[] eventNamesArr = eventNames.toArray();
                   Object[] eventIconsArr = eventIcons.toArray();
                   Object[] eventDescriptionsArr = eventDescriptions.toArray();*/
                    Log.d(TAG,"Preparing to read events..");
                    final CustomListAdapter adapter = new CustomListAdapter(act,userNamesArr,userIconsArr,catchphrase);
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                            lv.setAdapter(adapter);
                            vf = (ViewFlipper) findViewById(R.id.viewFlipper);
                            eventDetails.setText("List Of People");
                            vf.showNext();
                            progress.hide();
                        }
                    });
                    Log.d(TAG,"Done reading events");
                }

            }
        });
        t.start();

        //read from database
    }


    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent i = new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("com.codcodes.icebreaker.Back",true);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    public ArrayList<User> readUsersAtEvents(final String eventID)
    {
        try
        {
            Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
            Log.d(TAG,"Connection established");
            PrintWriter out = new PrintWriter(soc.getOutputStream());
            Log.d(TAG,"Sending request");

            out.print("GET /IBUserRequestService.svc/getUsersAtEvent/"+eventID+" HTTP/1.1\r\n"
                    + "Host: icebreak.azurewebsites.net\r\n"
                    + "Content-Type: text/plain;\r\n"// charset=utf-8
                    + "Content-Length: 0\r\n\r\n");
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            String resp;
            //Wait for response indefinitely TODO: Time-Out
            while(!in.ready()){}

            String usersJson = "";
            boolean openEventRead = false;
            while((resp = in.readLine())!=null)
            {
                if(DEBUG)System.out.println(resp);

                if(resp.equals("0"))
                {
                    out.close();
                    //in.close();
                    soc.close();
                    if(DEBUG)System.out.println(">>Done<<");
                    break;//EOF
                }

                if(resp.isEmpty())
                    if(DEBUG)System.out.println("\n\nEmpty Line\n\n");

                if(resp.contains("["))
                {
                    if(DEBUG)System.out.println("Opening at>>" + resp.indexOf("["));
                    openEventRead = true;
                }

                if(openEventRead)
                    usersJson += resp;//.substring(resp.indexOf('['));

                if(resp.contains("]"))
                {
                    if(DEBUG)System.out.println("Closing at>>" + resp.indexOf("]"));
                    openEventRead = false;
                }
            }

            if(DEBUG)System.out.println("Reading users.");
            //System.out.println(eventsJson);
            ArrayList<User> users = getUsers(usersJson);
            return users;
        }
        catch (UnknownHostException e)
        {
            System.err.println(e.getMessage());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<User> getUsers(String json)
    {
        ArrayList<User> users = new ArrayList<User>();
        //remove square brackets
        json = json.replaceAll("\\[", "");
        json = json.replaceAll("\\]", "");
        //System.out.println("Processing: " + json);
        while(json.contains("{") && json.contains("}"))
        {
            int endPos = json.indexOf("}");
            int startPos = json.indexOf("{");
            String user = json.substring(startPos,endPos+1);//remove braces
            if(DEBUG)System.out.println("User>>"+user);
            User u = getUser(user);
            users.add(u);
            /*if(!(json.contains("{") && json.contains("}")))
                break;*/
            if(json.length()>endPos+2)
                json = json.substring(endPos+2, json.length());
            else
                break;
            if(DEBUG)System.out.println("new JSON: " + json);
        }
        return users;
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

    public static boolean imageDownload(String iconName) throws IOException
    {
        Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
        System.out.println("Sending image download request");
        PrintWriter out = new PrintWriter(soc.getOutputStream());
        //Android: final String base64 = ;
        String headers = "GET /IBUserRequestService.svc/imageDownload/"+iconName+" HTTP/1.1\r\n"
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
            WritersAndReaders.saveImage(binFileArr,iconName);
            return true;
        }
        else
        {
            return false;
        }
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

}
