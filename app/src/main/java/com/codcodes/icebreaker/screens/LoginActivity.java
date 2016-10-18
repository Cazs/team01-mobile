package com.codcodes.icebreaker.screens;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
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
import com.codcodes.icebreaker.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button btnLogin;
    private ProgressBar loginbar;
    private CheckBox showPwd;

    private final String TAG = "IB/LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        loginbar = (ProgressBar) findViewById(R.id.loginprogressbar);
        loginbar.setVisibility(View.GONE);

        Typeface heading = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        TextView headingTextView = (TextView) findViewById(R.id.LogIn);
        headingTextView.setTypeface(heading);


        username = (EditText) findViewById(R.id.username_login);
        password = (EditText) findViewById(R.id.password_login);
        btnLogin=(Button) findViewById(R.id.btn_login);
        showPwd = (CheckBox) findViewById(R.id.cbx_show_pwd);

        showPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
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

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String p = password.getText().toString();
                final String u = username.getText().toString();


                /*if (!isValidUsername(u))
                {
                    username.requestFocus();
                    username.setError("Invalid Username");
                    return;
                }

                if (!isValidPassword(p))
                {
                    password.requestFocus();
                    password.setError("Invalid Password");
                    return;
                }*/

                Thread thread = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Message message;
                        try
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    loginbar.setVisibility(View.VISIBLE);
                                }
                            });

                            User new_user = new User();
                            new_user.setUsername(u);
                            new_user.setPassword(p);
                            String response = RemoteComms.postData("signin",new_user.toString());
                            //System.err.println(response);

                            boolean found = false;

                            if(response.contains("200"))
                                found = true;
                            if(response.contains("404"))
                                found = false;


                            if(found)
                            {
                                SharedPreference.setUsername(LoginActivity.this,u);
                                message = toastHandler("Successfully Logged In.").obtainMessage();
                                message.sendToTarget();
                                //Toast.makeText(getApplicationContext(), "User credentials are correct", Toast.LENGTH_LONG).show();
                                Intent mainscreen = new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(mainscreen);
                            }
                            else
                            {
                                message = toastHandler("Invalid Credentials.").obtainMessage();
                                message.sendToTarget();
                                LoginActivity.this.runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        username.setError("Invalid password and/or username.");
                                        password.setError("Invalid password and/or username.");
                                        username.clearFocus();
                                        password.clearFocus();
                                    }
                                });
                            }
                        }
                        catch (UnknownHostException e)
                        {
                            message = toastHandler("No Internet Access").obtainMessage();
                            message.sendToTarget();
                            Log.d(TAG,e.getMessage(),e);
                        }
                        catch (IOException e)
                        {
                            if(e instanceof SocketTimeoutException)
                                message = toastHandler("Connection Timed Out.").obtainMessage();
                            else
                                message = toastHandler("IOException: " + e.getMessage()).obtainMessage();
                            message.sendToTarget();
                            Log.d(TAG,e.getMessage(),e);
                        }
                        finally
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            loginbar.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
                thread.start();
            }
        });

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
    private boolean isValidUsername(String username)
    {
        //String Username_pattern = "^(?=.{5,15}$)(?![-.])[a-zA-Z0-9._]+(?<![_.])$";
        String not_allowed_specials = "&\\)\\(\\+=\\}\\{\\]\\[:;\"\',\\?/\\|\\\\";
        String Username_pattern  = "^(?!.*\\s)^(?=.*[A-Z]{1,})^(?=.*[a-z]{1,})^(?=.*[0-9]{1,})^(?!.*["+not_allowed_specials+"]).+$";

        Pattern pattern = Pattern.compile(Username_pattern);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    private boolean isValidPassword(String password)
    {
        //String password_pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[$@#!%*?&])[A-Za-z\\d$@#!%*?&]{6,}";
        String specials = "~`!@#\\$%_\\^\\*\\-\\.><";
        String not_allowed_specials = "&\\)\\(\\+=\\}\\{\\]\\[:;\"\',\\?/\\|\\\\";
        String password_pattern  = "^(?!.*\\s)^(?=.*[A-Z]{1,})^(?=.*[a-z]{1,})^(?=.*[0-9]{1,})^(?=.*["+specials+"]{1,})^(?!.*[\"+not_allowed_specials+\"]).+$";

        Pattern pattern = Pattern.compile(password_pattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    private void showMainPage(View view)
    {
        loginbar = (ProgressBar) findViewById(R.id.loginprogressbar);
        loginbar.setVisibility(View.GONE);
        Intent mainscreen = new Intent(this,MainActivity.class);
        startActivity(mainscreen);
    }


}
