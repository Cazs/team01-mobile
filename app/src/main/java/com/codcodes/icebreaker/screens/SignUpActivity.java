package com.codcodes.icebreaker.screens;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
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
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    EditText email;
    EditText username;
    EditText password;
    EditText confirmPassword;
    Button btnSignUp;
    ProgressBar bar;
    CheckBox checkBox;

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker = null;
    private AccessToken accessToken = null;

    private static final String TAG = "IB/SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Get Hash and set hash
        try
        {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
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

        //Init Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        setContentView(R.layout.activity_sign_up);

        //* Begin init Facebook login button

        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        accessTokenTracker = new AccessTokenTracker()
        {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken)
            {
                accessToken = currentAccessToken;
            }
        };
        accessTokenTracker.startTracking();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                // App code
                System.err.println("LoginResult: " + loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
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
        bar.setVisibility(View.GONE);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bar = (ProgressBar) findViewById(R.id.progressbar);


                final String e = email.getText().toString();
                final String p = password.getText().toString();
                final String u = username.getText().toString();
                final String cp = confirmPassword.getText().toString();
                if (!isValidEmail(e))
                {
                    email.setError("Invalid Email");
                    return;
                }
                if (!isValidPassword(p))
                {
                    bar.setVisibility(View.GONE);
                    password.setError("Password must include:" +
                            " \n • A minimum of 6 characters" +
                            " \n • At least one uppercase alphabet" +
                            " \n • One lowercase alphabet" +
                            " \n • One number and one" +
                            " \n • Special case character");
                    return;
                }
                if(!isValidUsername(u))
                {
                    bar.setVisibility(View.GONE);
                    username.setError("Username must not include:" +
                            "\n • White spaces" +
                            "\n • Special characters");
                    return;
                }

                bar.setVisibility(View.VISIBLE);
                PostToDB(e,u,cp,view);

            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {
                    password.setInputType(129);
                } else {
                    password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        System.err.println(resultCode);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private Handler toastHandler(final String text)
    {
        Handler toastHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        };
        return toastHandler;
    }

    private void showEditProfile(View view)
    {
        Intent editScreen = new Intent(this,Edit_ProfileActivity.class);
        startActivity(editScreen);
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
        String Username_pattern = "^(?=.{5,15}$)(?![-.])[a-zA-Z0-9._]+(?<![_.])$";

        Pattern pattern = Pattern.compile(Username_pattern);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    private boolean isValidPassword(String password)
    {
        String password_pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[$@#!%*?&])[A-Za-z\\d$@#!%*?&]{6,}";

        Pattern pattern = Pattern.compile(password_pattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    private boolean Compare(String confirmPassword,String password)
    {
        if(password.equals(confirmPassword))
        {
            return true;
        }
        return false;

    }

    private void PostToDB(final String email, final String username, final String confirmPassword, final View view)
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

                    String data = URLEncoder.encode("fname", "UTF-8") + "=" + URLEncoder.encode(" ", "UTF-8") + "&"
                            + URLEncoder.encode("lname", "UTF-8") + "=" + URLEncoder.encode(" ", "UTF-8") + "&"
                            + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") + "&"
                            + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") + "&"
                            + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(confirmPassword, "UTF-8");

                    out.print("POST /IBUserRequestService.svc/signup HTTP/1.1\r\n"
                            + "Host: icebreak.azurewebsites.net\r\n"
                            //+ "Content-Type: application/x-www-form-urlencoded\r\n"
                            + "Content-Type: text/plain; charset=utf-8\r\n"
                            + "Content-Length: " + data.length() + "\r\n\r\n"
                            + data);
                    out.flush();

                    BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                    String resp;
                    boolean found = false;
                    while((resp = in.readLine())!=null)
                    {
                        Log.d("ICEBREAK",resp);
                        if(resp.contains("HTTP/1.1 200 OK"))
                        {
                            Log.d("ICEBREAK","Found HTTP attr");
                            found = true;
                            break;
                        }
                    }
                    if(found) {
                        SharedPreference.setUsername(getApplicationContext(),username);
                        //Toast.makeText(getBaseContext(), "Successful sign up", Toast.LENGTH_LONG).show();
                        //findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                        showEditProfile(view);


                    }
                    else {
                        //Toast.makeText(getApplicationContext(), "Unsuccessful sign up", Toast.LENGTH_LONG).show();
                        Message messaage = toastHandler("Connection Error").obtainMessage();
                        messaage.sendToTarget();
                        finish();
                        startActivity(getIntent());
                    }
                    out.close();
                    in.close();
                }
                catch (UnknownHostException e)
                {
                    bar.setVisibility(View.GONE);
                    Message messaage = toastHandler("No Internet Access").obtainMessage();
                    messaage.sendToTarget();
                    e.printStackTrace();

                }
                catch (IOException e)
                {
                    bar.setVisibility(View.GONE);
                    Message messaage = toastHandler("Couldn't refreash feeds").obtainMessage();
                    messaage.sendToTarget();
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}