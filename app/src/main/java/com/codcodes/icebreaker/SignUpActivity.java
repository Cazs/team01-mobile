package com.codcodes.icebreaker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.MediaCodec;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class SignUpActivity extends AppCompatActivity {

    EditText email;
    EditText username;
    EditText password;
    EditText confirmPassword;
    Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Typeface heading = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");
        TextView headingTextView = (TextView) findViewById(R.id.SignUp);
        headingTextView.setTypeface(heading);

        email = (EditText) findViewById(R.id.email_sign_up);
        username = (EditText) findViewById(R.id.username_sign_up);
        password = (EditText) findViewById(R.id.password_sign_up);
        confirmPassword = (EditText) findViewById(R.id.confirm_password);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    username.setError("Username must not include:" +
                    "\n • White spaces" +
                    "\n • Special characters");
                    return;
                }
                if(!Compare(cp,p))
                {
                    confirmPassword.setError("Passwords do not match");
                    return;
                }
                PostToDB(e,u,cp,view);

            }
        });
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
                        //SharedPreference.setUsername(getApplicationContext(),username);
                        Toast.makeText(getApplicationContext(), "Successful sign up", Toast.LENGTH_LONG).show();
                        showEditProfile(view);

                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Unsuccessful sign up", Toast.LENGTH_LONG).show();
                        finish();
                        startActivity(getIntent());
                    }
                    out.close();
                    in.close();
                }
                catch (UnknownHostException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        thread.start();


    }

}





