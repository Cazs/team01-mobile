package com.codcodes.icebreaker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class RegistrationActivity extends AppCompatActivity
{
    private UserRegTask mRegTask = null;
    private Backend backend;
    private EditText txtUsername,txtPassword,
            txtPassword2,txtFName,txtLName,txtEmail;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        try
        {
            backend = new Backend(this, InetAddress.getByName("192.168.43.93"),4242);/*TODO:check IP address*/
            Toast.makeText(this,"Successfully connected to server",Toast.LENGTH_SHORT).show();
        }
        catch (UnknownHostException e)
        {
            Toast.makeText(this,"UHE: " + e.getMessage(),Toast.LENGTH_LONG).show();
        }
        catch (IOException e)
        {
            Toast.makeText(this,"IOE: " + e.getMessage(),Toast.LENGTH_LONG).show();
        }

        txtUsername = (EditText) findViewById(R.id.reg_txt_username);
        txtPassword = (EditText) findViewById(R.id.reg_txt_password);
        txtPassword2 = (EditText) findViewById(R.id.reg_txt_password2);
        txtFName = (EditText) findViewById(R.id.reg_txt_firstname);
        txtLName = (EditText) findViewById(R.id.reg_txt_lastname);
        txtEmail = (EditText) findViewById(R.id.reg_txt_email);
        btnCreate = (Button) findViewById(R.id.reg_btn_create);

        btnCreate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                attemptSignup();
            }
        });
    }

    private void attemptSignup()
    {
        if (mRegTask != null)//if the task is not busy
        {
            return;
        }

        // Reset errors.
        /*txtUsername.setError(null);
        txtPassword.setError(null);
        txtPassword2.setError(null);
        txtFName.setError(null);
        txtLName.setError(null);
        txtEmail.setError(null);*/

        // Store values at the time of the login attempt.
        String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();
        String fname = txtFName.getText().toString();
        String lname = txtLName.getText().toString();
        String email = txtEmail.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(username))
        {
            txtUsername.setError("Invalid Username.");
            focusView = txtUsername;
            cancel = true;
        }

        if(TextUtils.isEmpty(password))
        {
            txtPassword.setError("Invalid Password.");
            focusView = txtPassword;
            cancel = true;
        }
        if(TextUtils.isEmpty(fname))
        {
            txtFName.setError("Invalid First Name");
            focusView = txtFName;
            cancel = true;
        }
        if(TextUtils.isEmpty(lname))
        {
            txtLName.setError("Invalid Last Name");
            focusView = txtLName;
            cancel = true;
        }

        if(TextUtils.isEmpty(email))
        {
            txtEmail.setError("Invalid Email Address");
            focusView = txtEmail;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);
            mRegTask = new UserRegTask(username, password,fname,lname,email);
            mRegTask.execute((Void) null);
        }
    }

    /**
     * Represents an asynchronous login task used to authenticate
     * the user.
     */
    private class UserRegTask extends AsyncTask<Void, Void, Boolean>
    {
        private String username = "";
        private String password = "";
        private String fname = "";
        private String lname = "";
        private String email = "";

        UserRegTask(String username, String password, String fname, String lname, String email)
        {
            this.username = username;
            this.password = password;
            this.fname = fname;
            this.lname = lname;
            this.email = email;
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            try
            {
                //Socket conn = new Socket("127.0.0.1",4242);
                String resID = "qmuPzPyq7BB0JMor";
                String raw_cols = "fname,lname,pwd,email";
                if (username.length() > 1 && password.length() > 1 && fname.length() > 0 && lname.length() > 0 && email.length() > 1)
                {
                    if (raw_cols.contains(","))
                    {
                        Looper.prepare();

                        String formatted = resID + "/" + username + "=";//TODO: HTTPS
                        String cols[] = raw_cols.split(",");
                        for (String col : cols)
                            formatted += col + ".";

                        formatted = formatted.substring(0, formatted.length() - 1);//Remove last dot
                        String msg = "PUT /" + formatted + " HTTP/1.1";

                        Log.d("SVR_COMMS>Reg",msg);
                        backend.sendMessage(msg);

                        BufferedReader in = backend.getResponse();

                        Log.d("SVR_COMMS>Reg","Waiting for server response...");

                        String response = String.valueOf(in.readLine());

                        Log.d("SVR_COMMS>Reg",response);
                        if(response.contains("200 OK"))
                        {
                            if(in!=null)
                                in.close();

                            formatted = String.format("fname:%s.lname:%s.pwd:%s.email:%s",
                                    fname,lname,password,email);

                            msg = "PUT /" + resID + "/" + username + "/" + "creds=" +formatted + " HTTP/1.1";

                            Log.d("SVR_COMMS>Profile Data",msg);

                            backend.sendMessage(msg);

                            in = backend.getResponse();
                            response = String.valueOf(in.readLine());
                            Log.d("SVR_COMMS>Profile data",response);

                            if(response.contains("200 OK"))
                                return true;
                            else
                                return false;
                        }
                        else
                            return false;
                    }
                    else
                    {
                        System.err.println("Invalid column format. Must be in the format col1,col2,col3,...");
                        //logger.println("Invalid column format. Must be in the format col1,col2,col3,...");
                        //return;
                    }
                }
                else
                {
                    System.err.println("Error: Input data is too short");
                    //logger.println("Error: Empty input data");
                }
            }
            catch (IOException e)
            {
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            mRegTask = null;
            //showProgress(false);

            if (success)
            {
                //finish();
                startActivity(new Intent(getBaseContext(), LoginActivity.class));
                //Toast.makeText(getBaseContext(),"Success",Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getBaseContext(),"Cannot register",Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled()
        {
            mRegTask = null;
            //showProgress(false);
        }
    }
}
