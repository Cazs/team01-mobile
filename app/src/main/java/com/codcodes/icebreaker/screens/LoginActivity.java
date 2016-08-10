package com.codcodes.icebreaker.screens;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.SharedPreference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    Button btnLogin;
    ProgressBar loginbar;

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


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String p = password.getText().toString();
                final String u = username.getText().toString();


                if (!isValidUsername(u))
                {
                    username.setError("Invalid Username");
                    return;
                }

                if (!isValidPassword(p))
                {
                    password.setError("Invalid Password");
                    return;
                }

                if(isValidUsername(u)||isValidPassword(p))
                {
                    loginbar.setVisibility(View.VISIBLE);
                }


                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try
                        {

                            Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
                            System.out.println("Connection established");
                            PrintWriter out = new PrintWriter(soc.getOutputStream());
                            System.out.println("Sending request");

                            String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(u, "UTF-8") + "&"
                                    + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(p, "UTF-8");

                            out.print("POST /IBUserRequestService.svc/signin HTTP/1.1\r\n"
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
                                if(resp.contains("HTTP/1.1 200 OK"))
                                {
                                    found = true;
                                    Log.d("ICEBREAKER","USer found");
                                    break;
                                }
                            }
                            if(found) {

                                SharedPreference.setUsername(getApplicationContext(),u);
                                //Toast.makeText(getApplicationContext(), "User credentials are correct", Toast.LENGTH_LONG).show();
                                Intent mainscreen = new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(mainscreen);
                            }
                            else {
                                if (!isValidUsername(u))
                                {
                                    username.setError("Invalid Username");
                                    return;
                                }

                                if (!isValidPassword(p))
                                {
                                    password.setError("Invalid Password");
                                    return;
                                }
                                finish();
                                startActivity(getIntent());
                            }
                            out.close();
                            in.close();
                        }
                        catch (UnknownHostException e)
                        {
                            loginbar.setVisibility(View.GONE);
                            Message messaage = toastHandler("No Internet Access").obtainMessage();
                            messaage.sendToTarget();
                            e.printStackTrace();
                        }
                        catch (IOException e)
                        {
                            loginbar.setVisibility(View.GONE);
                            Message messaage = toastHandler("Couldn't refreash feeds").obtainMessage();
                            messaage.sendToTarget();
                            e.printStackTrace();
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

    private void showMainPage(View view)
    {
        loginbar = (ProgressBar) findViewById(R.id.loginprogressbar);
        loginbar.setVisibility(View.GONE);
        Intent mainscreen = new Intent(this,MainActivity.class);
        startActivity(mainscreen);
    }


}
