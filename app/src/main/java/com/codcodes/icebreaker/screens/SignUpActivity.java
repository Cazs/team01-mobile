package com.codcodes.icebreaker.screens;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.model.UserHelper;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity
{

    EditText email;
    EditText username;
    EditText password;
    Button btnSignUp;
    ProgressBar bar;
    CheckBox checkBox;

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker = null;
    private ProfileTracker profileTracker = null;
    private Profile profile = null;
    private AccessToken accessToken = null;

    private static final String TAG = "IB/SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Get and set hash
        try
        {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashCode = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d("KeyHash:", hashCode);
            }
        } catch (PackageManager.NameNotFoundException e)
        {
            //TODO: Better logging
            Log.wtf(TAG,e.getMessage(),e);
        } catch (NoSuchAlgorithmException e)
        {
            //TODO: Better logging
            Log.wtf(TAG,e.getMessage(),e);
        }

        //Init Facebook stuffs
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        setContentView(R.layout.activity_sign_up);

        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        //loginButton.setReadPermissions(Arrays.asList("email","user_birthday","user_about_me"));
        accessTokenTracker = new AccessTokenTracker()
        {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken)
            {
                accessToken = currentAccessToken;
            }
        };
        accessTokenTracker.startTracking();

        profileTracker = new ProfileTracker()
        {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile)
            {

                profile = currentProfile;
                if(profile!=null && accessToken!=null)//for cases like when they sign out
                {
                    final String usr = "user_" + accessToken.getUserId() + "_fb";

                    //Write Facebook profile image to local and remote storage
                    Thread tProfileReg = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                byte[] res = RemoteComms.getFBImage("https://graph.facebook.com","v2.7/"+ accessToken.getUserId()+"/picture?width=540&height=480&access_token="+accessToken.getToken());
                                //Write image to local disk
                                WritersAndReaders.saveImage(SignUpActivity.this,res,"profile/"+usr+".png");
                                //Update remote Image
                                int code = RemoteComms.imageUpload(res,"profile;"+usr,".png");
                                if(code==HttpURLConnection.HTTP_OK)
                                    Log.d(TAG,"Successfully uploaded new profile photo.");
                                else
                                    Log.wtf(TAG,"Couldn't upload new profile photo: " + code);
                            } catch (IOException e)
                            {
                                Log.d(TAG,e.getMessage(),e);
                            }
                        }
                    });
                    tProfileReg.start();

                    //Create IceBreak account
                    if (accessToken == null)
                    {
                        Log.wtf(TAG, "For some reason the Facebook access token is null.");
                        return;
                    }

                    /*LoginManager.getInstance().logInWithReadPermissions(
                            SignUpActivity.this,
                            Arrays.asList("email","user_birthday","user_about_me"));*/

                    String a_piece_of_time = String.valueOf(new Date().getTime());
                    String pwd = accessToken.getUserId().substring(0, 6) + '_' + a_piece_of_time;

                    //LoginManager.getInstance().logOut();
                    SharedPreference.setUsername(SignUpActivity.this,usr);//set new username if empty

                    User new_user = new User();
                    new_user.setFirstname(profile.getFirstName());
                    new_user.setLastname(profile.getLastName());
                    new_user.setFbID(accessToken.getUserId());
                    new_user.setFbToken(accessToken.getToken());
                    new_user.setGender("Unspecified");
                    new_user.setUsername(usr);
                    new_user.setPassword(pwd);

                    /*new_user.setEmail("");
                    new_user.setCatchphrase("");
                    new_user.setOccupation("");
                    new_user.setBio("");*/

                    Log.d(TAG, "Sending registration[new user] to server..");

                    //restart(new_user);//drop users table and add first user - the local user

                    PostToDB("signup", new_user);
                }else  Log.d(TAG,"FB Profile is null.");
            }
        };
        profileTracker.startTracking();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                // App code
            }

            @Override
            public void onCancel()
            {
                // App code
            }

            @Override
            public void onError(FacebookException exception)
            {
                // App code
            }
        });

        //* End init Facebook login button

        Typeface heading = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");
        TextView headingTextView = (TextView) findViewById(R.id.SignUp);
        headingTextView.setTypeface(heading);

        email = (EditText) findViewById(R.id.email_sign_up);
        username = (EditText) findViewById(R.id.username_sign_up);
        password = (EditText) findViewById(R.id.password_sign_up);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        checkBox = (CheckBox) findViewById(R.id.checkbox);
        bar = (ProgressBar) findViewById(R.id.progressbar);
        stopProgressBar();

        btnSignUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startProgressBar();

                final String e = email.getText().toString();
                final String p = password.getText().toString();
                final String u = username.getText().toString();

                String specials = "!@`.-$>_<#~^%*";

                if (!isValidEmail(e))
                {
                    stopProgressBar();
                    email.setError("Invalid Email");
                    return;
                }

                if(!isValidUsername(u))
                {
                    stopProgressBar();
                    username.setError("Username must:" +
                            "\n • Not include spaces" +
                            "\n • Have at least one\n\tUppercase character" +
                            "\n • Have at least one\n\tLowercase character" +
                            "\n • Have at least one\n\tspecial characters, i.e.\n "+specials+
                            "\n • Have 5-15 characters");
                    return;
                }

                if (!isValidPassword(p))
                {
                    stopProgressBar();
                    password.requestFocus();
                    password.setError("Password must include:" +
                            " \n • 6-50 characters" +
                            " \n • At least one uppercase\n\tAlphabet" +
                            " \n • At least one lowercase\n\tAlphabet" +
                            " \n • At least one number and" +
                            " \n • At least one special\n\tCharacter, i.e\n\t" +specials);
                    return;
                }

                User new_user = new User();
                if(profile!=null)
                {
                    new_user.setFirstname(profile.getFirstName());
                    new_user.setLastname(profile.getLastName());
                }
                /*else
                {
                    new_user.setFirstname("");
                    new_user.setLastname("");
                }*/

                if(accessToken!=null)
                {
                    new_user.setFbID(accessToken.getUserId());
                    new_user.setFbToken(accessToken.getToken());
                }

                new_user.setUsername(u);
                new_user.setPassword(p);
                new_user.setEmail(e);
                new_user.setGender("Unspecified");

                //restart(new_user);//add first user - the registering user

                PostToDB("signup",new_user);
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(!isChecked)
                {
                    password.setInputType(129);
                }
                else
                {
                    password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
    }

    public void restart(User u)
    {
        if(u.getEmail()==null)
            u.setEmail("<No email specified>");
        if(u.getEmail().isEmpty())
            u.setEmail("<No email specified>");

        if(u.getCatchphrase()==null)
            u.setCatchphrase("<No catchphrase specified>");
        if(u.getCatchphrase().isEmpty())
            u.setCatchphrase("<No catchphrase specified>");

        if(u.getOccupation()==null)
            u.setOccupation("<No occupation specified>");
        if(u.getOccupation().isEmpty())
            u.setOccupation("<No occupation specified>");

        if(u.getBio()==null)
            u.setBio("<No bio specified>");
        if(u.getBio().isEmpty())
            u.setBio("<No bio specified>");
        //Clear table
        UserHelper dbHelper = new UserHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.dropTable(db);
        if(db.isOpen())
            db.close();
        //Write user to disk
        SharedPreference.setUsername(getApplicationContext(), u.getUsername());
        LocalComms.addContact(this,u);
    }

    public void stopProgressBar()
    {
        SignUpActivity.this.runOnUiThread(new Runnable()
        {
            @Override

            public void run()
            {
                if(bar!=null)
                {
                    bar.setIndeterminate(false);
                    bar.setActivated(false);
                    bar.setEnabled(false);
                    bar.setVisibility(View.GONE);
                }
            }
        });
    }

    public void startProgressBar()
    {
        SignUpActivity.this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(bar!=null)
                {
                    bar.setIndeterminate(true);
                    bar.setActivated(true);
                    bar.setEnabled(true);
                    bar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
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

    private void showEditProfile(User u) throws IOException
    {
        User new_user = RemoteComms.getUser(this,u.getUsername());
        if(new_user!=null)
        {
            restart(new_user);

            Intent editScreen = new Intent(this, Edit_ProfileActivity.class);

            editScreen.putExtra("First Name", new_user.getFirstname());
            editScreen.putExtra("Last Name", new_user.getLastname());
            editScreen.putExtra("Age", String.valueOf(new_user.getAge()));
            editScreen.putExtra("Occupation", new_user.getOccupation());
            editScreen.putExtra("Catchphrase", new_user.getCatchphrase());
            editScreen.putExtra("Bio", new_user.getBio());
            editScreen.putExtra("Gender", new_user.getGender());
            editScreen.putExtra("Username", new_user.getUsername());

            startActivity(editScreen);
        }else Log.d(TAG,"User object from remote server is null.");
    }


    private boolean isValidEmail(String email)
    {
        String Email_pattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A=Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(Email_pattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isValidUsername(String username)
    {
        String specials = "~`!@#\\$%\\^&\\*\\)\\(_\\+\\-=\\}\\{\\]\\[:;'\"<>,./\\?\\|\\\\";
        String not_allowed_specials = "&\\)\\(\\+=\\}\\{\\]\\[:;\"\',\\?/\\|\\\\";
        /*//String Username_pattern  = "^(?!.*\\s)^(?=.*[A-Z]{1,})^(?=.*[a-z]{1,})^(?=.*[0-9]{1,})^(?!.*["+not_allowed_specials+"]).+$";
        String Username_pattern = "^(?=.{5,15}$)(?![-.])[a-zA-Z0-9._]+(?<![_.])$";

        Pattern pattern = Pattern.compile(Username_pattern);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();*/
        String ppat_spaces = "^(?!.*\\s+).*$";
        String ppat_upper = "^(?=.*[A-Z]+).*$";
        String ppat_lower = "^(?=.*[a-z]+).*$";
        String ppat_digit = "^(?=.*[0-9]+).*$";
        String ppat_spec = "^(?=.*["+specials+"]*).*$";
        String ppat_nspec = "^(?!.*["+not_allowed_specials+"]+).*$";
        if(username==null)
            return false;
        if(username.isEmpty())
            return false;
        if(username.length()>5)
            if(Pattern.compile(ppat_spaces).matcher(username).matches())//if(!username.contains(" "))
                if(Pattern.compile(ppat_upper).matcher(username).matches())
                    if(Pattern.compile(ppat_lower).matcher(username).matches())
                        //if(Pattern.compile(ppat_digit).matcher(username).matches())
                            //if(Pattern.compile(ppat_spec).matcher(username).matches())
                                if(Pattern.compile(ppat_nspec).matcher(username).matches())
                                    return true;
                                else Log.d(TAG,"Has illegal chars.");
                            //else Log.d(TAG,"Doesn't have special char.");
                        //else Log.d(TAG,"Doesn't have digits.");
                    else Log.d(TAG,"Doesn't lowercase chars.");
                else Log.d(TAG,"Doesn't have uppercase chars.");
            else Log.d(TAG,"Has spaces.");
        else Log.d(TAG,"Is too short.");

        return false;
    }

    private boolean isValidPassword(String password)
    {
        //String specials = "~`!@#\\$%\\^&\\*\\)\\(_\\+\\-=\\}\\{\\]\\[:;'\"<>,./\\?\\|\\\\";
        String specials = "~`!@#\\$%_\\^\\*\\-\\.><";
        String not_allowed_specials = "&\\)\\(\\+=\\}\\{\\]\\[:;\"\',\\?/\\|\\\\";
        /*String password_pattern  = "^(?!.*\\s)^(?=.*[A-Z]{1,})^(?=.*[a-z]{1,})^(?=.*[0-9]{1,})^(?=.*["+specials+"]{1,})^(?!.*[\"+not_allowed_specials+\"]).+$";
         /String password_pattern = "[A-Z]{1,}[a-z]{1,}\\d+"+specials+"]{6,50}";//"^(?=.*[a-z])(?=.*[A-Z])(?=.*[$@#!%*?&])";
        Pattern pattern = Pattern.compile(password_pattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();*/
        String ppat_spaces = "^(?!.*\\s+).*$";
        String ppat_upper = "^(?=.*[A-Z]+).*$";
        String ppat_lower = "^(?=.*[a-z]+).*$";
        String ppat_digit = "^(?=.*[0-9]+).*$";
        String ppat_spec = "^(?=.*["+specials+"]*).*$";
        String ppat_nspec = "^(?!.*["+not_allowed_specials+"]+).*$";
        if(password==null)
            return false;
        if(password.isEmpty())
            return false;
        if(password.length()>5)
            if(Pattern.compile(ppat_spaces).matcher(password).matches())
                if(Pattern.compile(ppat_upper).matcher(password).matches())
                    if(Pattern.compile(ppat_lower).matcher(password).matches())
                        if(Pattern.compile(ppat_digit).matcher(password).matches())
                            if(Pattern.compile(ppat_spec).matcher(password).matches())
                                if(Pattern.compile(ppat_nspec).matcher(password).matches())
                                    return true;
                                else Log.d(TAG,"Has illegal chars.");
                            else Log.d(TAG,"Doesn't have special char.");
                        else Log.d(TAG,"Doesn't have digits.");
                    else Log.d(TAG,"Doesn't lowercase chars.");
                else Log.d(TAG,"Doesn't have uppercase chars.");
            else Log.d(TAG,"Has spaces.");
        else Log.d(TAG,"Is too short.");

        return false;
    }

    private boolean Compare(String confirmPassword,String password)
    {
        if(password.equals(confirmPassword))
        {
            return true;
        }
        return false;

    }

    private void PostToDB(final String function, final User user)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                startProgressBar();
                try
                {
                    if(user!=null)
                    {
                        String resp = RemoteComms.postData(function, user.toString());

                        Message message;

                        if(resp.contains("200"))
                        {
                            message = toastHandler("Successfully logged in.").obtainMessage();
                            message.sendToTarget();

                            showEditProfile(user);

                            if(resp.toLowerCase().contains("exists=true"))
                            {
                                //message = toastHandler("Username exists.").obtainMessage();
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
                                LoginManager.getInstance().logOut();
                            }
                            else
                            {
                                //Toast.makeText(SignUpActivity.this, "Username already exists, please try again.", Toast.LENGTH_LONG).show();
                                message = toastHandler("Could not register your account.").obtainMessage();
                                message.sendToTarget();
                                LoginManager.getInstance().logOut();
                            }
                        }
                    } else
                    {
                        Log.wtf(TAG,"User registration payload is empty.");
                        Message message = toastHandler("Could not register your account, registration payload is empty.").obtainMessage();
                        message.sendToTarget();
                        LoginManager.getInstance().logOut();
                    }
                }
                catch (UnknownHostException e)
                {
                    Message message = toastHandler("No Internet Access..").obtainMessage();
                    message.sendToTarget();
                    if(e.getMessage()!=null)
                        Log.d(TAG,e.getMessage(),e);
                    else e.printStackTrace();
                    LoginManager.getInstance().logOut();
                }
                catch (SocketTimeoutException e)
                {
                    Message message = toastHandler("Connection Timed Out").obtainMessage();
                    message.sendToTarget();
                    if(e.getMessage()!=null)
                        Log.d(TAG,e.getMessage(),e);
                    else e.printStackTrace();
                    LoginManager.getInstance().logOut();
                }
                catch (IOException e)
                {
                    Message message = toastHandler("IOException: " + e.getMessage()).obtainMessage();
                    message.sendToTarget();
                    if(e.getMessage()!=null)
                        Log.d(TAG,e.getMessage(),e);
                    else e.printStackTrace();
                    LoginManager.getInstance().logOut();
                }
                finally
                {
                    stopProgressBar();
                }
            }
        });
        thread.start();
    }
}