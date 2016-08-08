package com.codcodes.icebreaker.model;

/**
 * Created by USER on 2016/08/02.
 */
public class User implements IJsonable {
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

    public String getFirstname() {
        return firstname;
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

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return age + " " + occupation + " " + bio + " " + catchphrase + " " + gender + " " + username;
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
                setGender(value);
                break;
            case "Occupaton":
                setOccupation(value);
                break;
            case "Catchphrase":
                setCatchphrase(value);
                break;
        }
    }
}
