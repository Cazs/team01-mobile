package com.codcodes.icebreaker;

import android.content.Intent;
import android.graphics.Typeface;
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

public class LoginActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


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
                                Intent mainscreen = new Intent(getApplicationContext(),MainPageActivity.class);
                                startActivity(mainscreen);
                            }
                            else {
                               // Toast.makeText(getApplicationContext(), "User credentials are incorrect", Toast.LENGTH_LONG).show();
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
        });

    }

    private void showMainPage(View view)
    {
        Intent mainscreen = new Intent(this,MainPageActivity.class);
        startActivity(mainscreen);
    }


}
