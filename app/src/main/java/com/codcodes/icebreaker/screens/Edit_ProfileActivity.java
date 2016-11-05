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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.auxilary.ImageUtils;
import com.codcodes.icebreaker.auxilary.LocalComms;
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
    //private User user;

    private Bitmap circularbitmap,bitmap;

    private final String TAG = "IB/EditProfActivity";

    private ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        final String username = SharedPreference.getUsername(getApplicationContext());

        Firstname = (EditText) findViewById(R.id.editName);
        Lastname = (EditText) findViewById(R.id.editLastName);
        Age = (EditText) findViewById(R.id.editAge);
        Occupation = (EditText) findViewById(R.id.editOccupation);
        Bio = (EditText) findViewById(R.id.editbio);
        Catchphrase = (EditText) findViewById(R.id.CatchPhrase);
        TextView name = (TextView) findViewById(R.id.main_heading);
        TextView edit_image_link = (TextView) findViewById(R.id.editphoto);

        Typeface h = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");
        Typeface ttf_infinity = Typeface.createFromAsset(getAssets(), "Infinity.ttf");

        name.setTypeface(h);
        name.setTextSize(26);
        edit_image_link.setTypeface(ttf_infinity);
        edit_image_link.setTextSize(18);

        circularImageView = (ImageView) findViewById(R.id.editprofilepic);

        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender, R.layout.spinner);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


        Button done = (Button) findViewById(R.id.edit_profile_done);

        done.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                updateUserInfo();
            }
        });

        Bundle extras = getIntent().getExtras();

        if (extras != null)
        {
            Firstname.setText(extras.getString("First Name"));
            Lastname.setText(extras.getString("Last Name"));
            Age.setText(extras.getString("Age"));
            Occupation.setText(extras.getString("Occupation"));
            Catchphrase.setText(extras.getString("Catchphrase"));
            Bio.setText(extras.getString("Bio"));
            Gender = extras.getString("Gender");
            profilePicture = extras.getString("Username");
            if(Gender==null)
                Gender="unspecified";
            else
                Gender = Gender.toLowerCase();
            int gender = 0;
            switch(Gender)
            {
                case "male":
                    gender = 0;
                    break;
                case "female":
                    gender = 1;
                    break;
                case "unspecified":
                    gender = 2;
                    break;
            }
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ALPHA_8;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run()
                {
                    try
                    {
                        bitmap = LocalComms.getImage(getApplicationContext(),profilePicture,".png","/profile",options);

                        circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                circularImageView.setImageBitmap(circularbitmap);
                            }
                        });
                    } catch (IOException e)
                    {
                        LocalComms.logException(e);
                    }
                }
            });
            t.start();
            spinner.setSelection(gender);
        }

        edit_image_link.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Bitmap b = circularImageView.getDrawingCache(false);
                startActivityForResult(intent, 0);
            }
        });
    }

    public void updateUserInfo()
    {
        String fname = Firstname.getText().toString();
        String lname = Lastname.getText().toString();
        int age = 0;
        try
        {
            if(!isInt(Age.getText().toString()))
                throw new NumberFormatException("Value '"+Age.getText().toString()+"' is not a number.");
            age = Integer.valueOf(Age.getText().toString());
        }
        catch (NumberFormatException e)
        {
            Message message = toastHandler("Value '"+Age.getText().toString()+"' is not a number.").obtainMessage();
            message.sendToTarget();
            return;
        }

        String occupation = Occupation.getText().toString();
        String bio = Bio.getText().toString();
        String catchphrase = Catchphrase.getText().toString();
        String gender = Gender;

        if(isEmpty(fname))
        {
            Firstname.setError("Cannot be empty");
            return;
        }
        if(isEmpty(lname))
        {
            Lastname.setError("Cannot be empty");
            return;
        }

        if(isEmpty(bio))
            bio = "<No bio>";//Bio.setError("Cannot be empty");

        if(age<=0)
        {
            Age.setError("Must be a number greater than zero.");
            return;
        }
        if(isEmpty(catchphrase))
            catchphrase = "<No catchphrase>";

        progress = LocalComms.showProgressDialog(this,"Updating your information...");

        User new_user = new User();
        new_user.setFirstname(fname);
        new_user.setLastname(lname);
        new_user.setAge(age);
        new_user.setOccupation(occupation);
        new_user.setGender(gender);
        new_user.setCatchphrase(catchphrase);
        new_user.setBio(bio);
        new_user.setUsername(SharedPreference.getUsername(this).toString());

        updateProfile(new_user);
    }



    public void onActivityResult(int requstCode,int resltCode,Intent data)
    {
        super.onActivityResult(requstCode,resltCode,data);
        if(data!=null)
        {
            progress = LocalComms.showProgressDialog(this,"Updating image...");

            Uri targetUri = data.getData();

            try
            {
                final String usr = SharedPreference.getUsername(this).toString();
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 20, stream);
                final byte[] bmp_arr = stream.toByteArray();
                stream.close();
                //Set image view
                circularImageView.setImageBitmap(bitmap);
                bitmap.recycle();
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        int res_code = 0;
                        try
                        {
                            res_code = RemoteComms.imageUpload(bmp_arr, "profile;" + usr, ".png");
                            if (res_code == HttpURLConnection.HTTP_OK)
                            {
                                Log.d(TAG, "Image upload successful");
                                Message message = toastHandler("Image upload successful").obtainMessage();
                                message.sendToTarget();

                                //Save  copy of image to app directory iff it was uploaded successfully
                                WritersAndReaders.saveFile(Edit_ProfileActivity.this,bmp_arr, "/profile/" + usr + ".png");
                            } else
                            {
                                Message message = toastHandler("Image upload successful: " + res_code).obtainMessage();
                                message.sendToTarget();
                                Log.wtf(TAG, "Image upload unsuccessful: " + res_code);
                            }
                        }catch (IOException e)
                        {
                            LocalComms.hideProgressBar(progress);
                            Log.wtf(TAG, e.getMessage(), e);
                        }
                        LocalComms.hideProgressBar(progress);
                    }
                });
                t.start();
            } catch (IOException e)
            {
                Log.wtf(TAG, e.getMessage(), e);
                //TODO: Better logging
            }
        }
        else Log.d(TAG,"Data from image picker is null.");
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        Gender = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

    private Handler toastHandler(final String text)
    {
        Handler toastHandler = new Handler(Looper.getMainLooper())
        {
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        };
        return toastHandler;
    }

    public void updateProfile(final User user)//final String username,final String firstname, final String lastname, final String age,final String occupation, final String bio, final String catchphrase,final String gender)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                try
                {
                    if(user!=null)
                    {
                        String resp = RemoteComms.postData("userUpdate/"+user.getUsername(), user.toString());

                        Message message;

                        if(resp.contains("200"))
                        {
                            message = toastHandler("Successfully updated your account.").obtainMessage();
                            message.sendToTarget();

                            //Update user locally
                            LocalComms.updateContact(Edit_ProfileActivity.this,user);

                            if(resp.toLowerCase().contains("exists=true"))
                            {
                                //message = toastHandler("Username already exists on remote server.").obtainMessage();
                                //message.sendToTarget();
                                Log.d(TAG,"Username already exists on remote server.");
                            }
                        }
                        else
                        {
                            if(resp.toLowerCase().contains("exists=true"))
                            {
                                //Toast.makeText(SignUpActivity.this, "Username already exists, please try again.", Toast.LENGTH_LONG).show();
                                message = toastHandler("Username already exists, please try again.").obtainMessage();
                                message.sendToTarget();
                            }
                            else
                            {
                                //Toast.makeText(SignUpActivity.this, "Username already exists, please try again.", Toast.LENGTH_LONG).show();
                                message = toastHandler("Could not update your account:"+resp).obtainMessage();
                                message.sendToTarget();
                                Log.d(TAG,resp);
                            }
                        }
                    } else
                    {
                        Log.wtf(TAG,"User update payload is empty.");
                        Message message = toastHandler("Could not register your account, registration payload is empty.").obtainMessage();
                        message.sendToTarget();
                    }
                }
                catch (UnknownHostException e)
                {
                    Message message = toastHandler("No Internet Access..").obtainMessage();
                    message.sendToTarget();
                    Log.d(TAG,e.getMessage(),e);
                }
                catch (IOException e)
                {
                    Message message = toastHandler(e.getMessage()).obtainMessage();
                    message.sendToTarget();
                    Log.d(TAG,e.getMessage(),e);
                }
                finally
                {
                    LocalComms.hideProgressBar(progress);
                }
            }
        });
        thread.start();
    }

    private boolean isEmpty(String check)
    {
        if(check.isEmpty())
            return true;
        return false;
    }

    private boolean isInt(String check)
    {
        if(check.matches("^\\d+$"))
            return true;
        else
            return false;
    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
    }


}