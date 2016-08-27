package com.codcodes.icebreaker.screens;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.auxilary.ImageUtils;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.model.User;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Edit_ProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private ImageView circularImageView;
    private EditText Firstname;
    private EditText Lastname;
    private EditText Age;
    private EditText Occupation;
    private EditText Bio;
    private EditText Catchphrase;
    private EditText Email;
    private EditText Password;
    private EditText Username;
    private String Gender;
    private String profilePicture;
    private User user;

    private Bitmap circularbitmap,bitmap;

    private static final boolean DEBUG = true;
    private final String TAG = "ICEBREAK";
    private static boolean CHUNKED = false;

    private ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final String username = SharedPreference.getUsername(getApplicationContext());

        Firstname = (EditText) findViewById(R.id.editName);
        Lastname = (EditText) findViewById(R.id.editLastName);
        Age = (EditText) findViewById(R.id.editAge);
        Occupation = (EditText) findViewById(R.id.editOccupation);
        Bio = (EditText) findViewById(R.id.editbio);
        Catchphrase = (EditText) findViewById(R.id.CatchPhrase);

        Typeface h = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");
        TextView name = (TextView) toolbar.findViewById(R.id.Edit_Heading);
        name.setTypeface(h);

        circularImageView = (ImageView) findViewById(R.id.editprofilepic);


        Typeface EditFont = Typeface.createFromAsset(getAssets(), "Infinity.ttf");
        TextView editphoto = (TextView) findViewById(R.id.editphoto);
        editphoto.setTypeface(EditFont);


        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender, R.layout.spinner);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


        ImageView done = (ImageView) findViewById(R.id.edit_profile_done);


        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            Firstname.setText(extras.getString("First Name"));
            Lastname.setText(extras.getString("Last Name"));
            Age.setText(extras.getString("Age"));
            Occupation.setText(extras.getString("Occupation"));
            Catchphrase.setText(extras.getString("Catchphrase"));
            Bio.setText(extras.getString("Bio"));
            Gender = extras.getString("Gender");
            profilePicture = extras.getString("Picture");
            int gender = 0;
            switch(Gender)
            {
                case "Male":
                    gender = 0;
                    break;
                case "Female":
                    gender = 1;
                    break;
                case "Unspecified":
                    gender = 2;
                    break;
            }
            bitmap = ImageUtils.getInstant().compressBitmapImage(Environment.getExternalStorageDirectory().getPath().toString()
                    + profilePicture, getApplicationContext());
            circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
            circularImageView.setImageBitmap(circularbitmap);
            spinner.setSelection(gender);
        }

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fname = Firstname.getText().toString();
                String lname = Lastname.getText().toString();
                String age = Age.getText().toString();
                String occupation = Occupation.getText().toString();
                String bio = Bio.getText().toString();
                String catchphrase = Catchphrase.getText().toString();
                String g = Gender;

                if(isEmpty(fname))
                {
                    Firstname.setError("Cannot be empty");
                    return;
                }
                if(isEmpty(lname))
                {
                    Lastname.setError("Cannot be empty");
                    return;
                }if(isEmpty(bio))
                {
                    Bio.setError("Cannot be empty");
                    return;
                }if(isEmpty(age))
                {
                    Age.setError("Cannot be empty");
                    return;
                }
                if(isEmpty(catchphrase))
                {
                    Catchphrase.setError("Cannot be empty");
                    return;
                }
                if(!isInt(age))
                {
                    Age.setError("Must be an integer");
                    return;
                }
                updateProfile(username,fname, lname, age, occupation, bio, catchphrase,g);
            }

        });

        editphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Bitmap b = circularImageView.getDrawingCache(false);
                startActivityForResult(intent, 0);

            }
        });

    }

    public void showProgressBar(String msg)
    {
        if(progress==null)
            progress = new ProgressDialog(this);
        if(!progress.isShowing())
        {
            progress.setMessage(msg);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setProgress(0);
            progress.show();
        }
    }

    public void hideProgressBar()
    {
        if(progress!=null)
            if(progress.isShowing())
                progress.dismiss();
    }

    public void onActivityResult(int requstCode,int resltCode,Intent data)
    {
        super.onActivityResult(requstCode,resltCode,data);
        Uri targetUri = data.getData();

        try
        {
            final String usr = SharedPreference.getUsername(this).toString();
            Bitmap bitmap = bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,5,stream);
            final byte[] bmp_arr = stream.toByteArray();

            showProgressBar("Updating image...");
            //Save  copy of image to app directory
            WritersAndReaders.saveImage(bmp_arr,"/profile/"+usr+".png");
            //Set image view
            circularImageView.setImageBitmap(bitmap);
            bitmap.recycle();
            Thread t = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Looper.prepare();
                    int res_code = 0;//TODO: fix directory structure on server and local
                    try
                    {
                        res_code = RemoteComms.imageUpload(bmp_arr, "profile|"+usr, ".png");
                        if(res_code== HttpURLConnection.HTTP_OK)
                        {
                            Log.d(TAG,"Image upload successful");
                            Toast.makeText(getApplicationContext(),"Image upload successful",Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Log.wtf(TAG,"Image upload unsuccessful: " + res_code);
                            Toast.makeText(getApplicationContext(),"Image upload successful: " + res_code,Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e)
                    {
                        hideProgressBar();
                        Log.wtf(TAG,e.getMessage(),e);
                    }
                    hideProgressBar();
                }
            });
            t.start();
        }
        catch (FileNotFoundException e)
        {
            Log.wtf(TAG,e.getMessage(),e);
            //TODO: Better logging
        }
        catch (IOException e)
        {
            Log.wtf(TAG,e.getMessage(),e);
            //TODO: Better logging
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Gender = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    public void updateProfile(final String username,final String firstname, final String lastname, final String age,final String occupation, final String bio, final String catchphrase,final String gender)
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

                    String data = URLEncoder.encode("fname", "UTF-8") + "=" + URLEncoder.encode(firstname, "UTF-8") + "&"
                            + URLEncoder.encode("lname", "UTF-8") + "=" + URLEncoder.encode(lastname, "UTF-8") + "&"
                            + URLEncoder.encode("age", "UTF-8") + "=" + URLEncoder.encode(age, "UTF-8") + "&"
                            + URLEncoder.encode("bio", "UTF-8") + "=" + URLEncoder.encode(bio, "UTF-8") + "&"
                            + URLEncoder.encode("gender", "UTF-8") + "=" + URLEncoder.encode(gender, "UTF-8") + "&"
                            + URLEncoder.encode("occupation", "UTF-8") + "=" + URLEncoder.encode(occupation, "UTF-8") + "&"
                            + URLEncoder.encode("catchphrase", "UTF-8") + "=" + URLEncoder.encode(catchphrase, "UTF-8");

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
                    if(found)
                    {
                        //TODO: Figure out how o go back to profile fragment
                        if (DEBUG) System.out.println("Success");
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        if (DEBUG) System.out.println("UnSuccess");
                        //TODO: send message that editing was unsucessful try again
                        finish();
                        startActivity(getIntent());
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

    public static boolean imageUpload(Socket soc,String iconName) throws IOException
    {
        System.out.println("Sending image upload request");
        PrintWriter out = new PrintWriter(soc.getOutputStream());
        //Android: final String base64 = ;
        String headers = "GET /IBUserRequestService.svc/imageUpload/"+iconName+" HTTP/1.1\r\n"
                + "Host: icebreak.azurewebsites.net\r\n"
                //+ "Content-Type: application/x-www-form-urlencoded\r\n"
                + "Content-Type: text/plain;\r\n"// charset=utf-8
                + "Content-Length: 0\r\n\r\n";

        out.print(headers);
        out.flush();

        //TODO: Not Sure what do
        BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
        String resp;
        while(!in.ready()){}
        while((resp = in.readLine())!=null)
        {
            //System.out.println(resp);

            if(resp.toLowerCase().contains("payload"))
            {
                String base64bytes = resp.split(":")[1];
                base64bytes = base64bytes.substring(1, base64bytes.length());
                byte[] binFileArr = android.util.Base64.decode(base64bytes, android.util.Base64.DEFAULT);
                WritersAndReaders.saveImage(binFileArr,iconName);
                return true;
            }

            if(!in.ready())
            {
                //if(DEBUG)System.out.println(">>Done<<");
                break;
            }
        }
        out.close();
        //in.close();
        soc.close();
        return false;
    }
    private boolean isEmpty(String check)
    {
        if(check.isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean isInt(String check)
    {
        if(check.matches("^\\d+$")) {
            return true;
        }
        return false;
    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }


}