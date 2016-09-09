package com.codcodes.icebreaker.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by USER on 2016/08/02.
 */
public class User implements IJsonable, Parcelable
{
    private String firstname;
    private String lastname;
    private int age;
    private String occupation;
    private String bio;
    private String catchphrase;
    private String email;
    private String password;
    private String gender;
    private String username;
    private String fb_id;
    private String fb_token;
    private Event event;

    private final String TAG = "IB/User";

    public User() {}

    public User(String firstname, String lastname, int age, String occupation, String bio, String catchphrase, String email, String password, String gender, String username) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.age = age;
        this.occupation = occupation;
        this.bio = bio;
        this.catchphrase = catchphrase;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.username = username;
    }

    public Event getEvent(){return  this.event;}

    public void setEvent(Event e){this.event = e;}

    public String getFirstname() {
        return this.firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCatchphrase() {
        return catchphrase;
    }

    public void setCatchphrase(String catchphrase) {
        this.catchphrase = catchphrase;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {this.password = password;}

    public String getGender() {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFbID() {
        return this.fb_id;
    }

    public void setFbID(String id) {
        this.fb_id = id;
    }

    public String getFbToken() {
        return this.fb_token;
    }

    public void setFbToken(String token) {
        this.fb_token = token;
    }

    @Override
    public String toString()
    {
        String s = "";
        StringBuilder result = new StringBuilder();
        try
        {
            if (this.username != null)
                result.append(URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(this.username, "UTF-8") + "&");
            if (this.firstname != null)
                result.append(URLEncoder.encode("fname", "UTF-8") + "=" + URLEncoder.encode(this.firstname, "UTF-8") + "&");
            if (this.lastname != null)
                result.append(URLEncoder.encode("lname", "UTF-8") + "=" + URLEncoder.encode(this.lastname, "UTF-8") + "&");
            if (this.age > 0)
                result.append(URLEncoder.encode("age", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(this.age), "UTF-8") + "&");
            if (this.bio != null)
                result.append(URLEncoder.encode("bio", "UTF-8") + "=" + URLEncoder.encode(this.bio, "UTF-8") + "&");
            if (this.catchphrase != null)
                result.append(URLEncoder.encode("catchphrase", "UTF-8") + "=" + URLEncoder.encode(this.catchphrase, "UTF-8") + "&");
            if (this.occupation != null)
                result.append(URLEncoder.encode("occupation", "UTF-8") + "=" + URLEncoder.encode(this.occupation, "UTF-8") + "&");
            if (this.gender != null)
                result.append(URLEncoder.encode("gender", "UTF-8") + "=" + URLEncoder.encode(this.gender, "UTF-8") + "&");
            if (this.email != null)
                result.append(URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(this.email, "UTF-8") + "&");
            if (this.password != null)
                result.append(URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(this.password, "UTF-8") + "&");
            if (this.event != null)
                result.append(URLEncoder.encode("event_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(this.event.getId()), "UTF-8") + "&");
            if (this.fb_token != null)
                result.append(URLEncoder.encode("fb_token", "UTF-8") + "=" + URLEncoder.encode(this.fb_token, "UTF-8") + "&");
            if (this.fb_id != null)
                result.append(URLEncoder.encode("fb_id", "UTF-8") + "=" + URLEncoder.encode(this.fb_id, "UTF-8") + "&");

            s = result.toString();
            //Remove ending '&'
            if(s!=null)
                if(s.length()>0)
                    s=s.substring(0,s.length()-1);

        }
        catch (UnsupportedEncodingException e)
        {
            //TODO: Better logging
            Log.wtf(TAG,e.getMessage(),e);
        }
        finally
        {
            return s;
        }
    }

    @Override
    public void setVarValue(String var, String value)
    {
        switch (var)
        {
            case "Age":
                setAge(Integer.valueOf(value));
                break;
            case "Bio":
                setBio(value);
                break;
            case "Event_id":
                break;
            case "Access_level":
                break;
            case "Fname":
                setFirstname(value);
                break;
            case "Lname":
                setLastname(value);
                break;
            case "Username":
                setUsername(value);
                break;
            case "Email":
                setEmail(value);
                break;
            case "Gender":
                setGender(value.toLowerCase());
                break;
            case "Occupation":
                setOccupation(value);
                break;
            case "Catchphrase":
                setCatchphrase(value);
                break;
        }
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeString(getUsername());
        parcel.writeString(getFirstname());
        parcel.writeString(getLastname());
        parcel.writeString(getBio());
        parcel.writeString(getCatchphrase());
        parcel.writeInt(getAge());
        parcel.writeString(getEmail());
        parcel.writeString(getGender());
        parcel.writeString(getOccupation());
    }

    //Used to regenerate Message object.
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>()
    {
        public User createFromParcel(Parcel in)
        {
            User u = new User();
            u.setUsername(in.readString());
            u.setFirstname(in.readString());
            u.setLastname(in.readString());
            u.setBio(in.readString());
            u.setCatchphrase(in.readString());
            u.setAge(in.readInt());
            u.setEmail(in.readString());
            u.setGender(in.readString());
            u.setOccupation(in.readString());
            return u;
        }

        public User[] newArray(int size)
        {
            return new User[size];
        }
    };
}
