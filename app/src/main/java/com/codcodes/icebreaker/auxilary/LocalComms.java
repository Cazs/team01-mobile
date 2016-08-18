package com.codcodes.icebreaker.auxilary;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.MessagePollContract;
import com.codcodes.icebreaker.model.MessagePollHelper;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.model.UserContract;
import com.codcodes.icebreaker.model.UserHelper;
import com.codcodes.icebreaker.screens.MainActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Casper on 2016/08/16.
 */
public class LocalComms
{
    private static final String TAG = "IB/LocalComms";

    public static User getLocalUser(String username, Context context)
    {
        MessagePollHelper dbHelper = new MessagePollHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        dbHelper.onCreate(db);

        User u = new User();

        String query = "SELECT * FROM " + UserContract.UserEntry.TABLE_NAME + " WHERE "
                + UserContract.UserEntry.COL_USER_USERNAME + " = ?";

        Cursor c = db.rawQuery(query, new String[]
                {
                        username
                });

        if(c.getCount()>0)
        {
            c.moveToFirst();

            String usr = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_USERNAME));
            String fname = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_FNAME));
            String lname = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_FNAME));
            String gender = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_GENDER));
            String occ = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_OCCUPATION));
            String bio = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_BIO));
            int age = c.getInt(c.getColumnIndex(UserContract.UserEntry.COL_USER_AGE));
            String catch_phrase = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_CATCHPHRASE));

            u.setGender(gender);
            u.setOccupation(occ);
            u.setCatchphrase(catch_phrase);
            u.setBio(bio);
            u.setAge(age);
            u.setFirstname(fname);
            u.setLastname(lname);
            u.setUsername(usr);

            //Close DB
            db.close();

            return u;
        }
        else
        {
            //Close DB
            db.close();

            return null;
        }
    }

    public static Bitmap getImage(Context context, String filename,String ext, String path, BitmapFactory.Options options)
    {
        path = path.charAt(0) != '/' && path.charAt(0) != '\\' ? '/' + path : path;
        Bitmap bitmap = null;
        if(!ext.contains("."))//add dot to image extension if it's not there
            ext = '.' + ext;
        //Look for image locally
        if (!new File(path + '/' + filename + ext).exists())
        {
            Log.d(TAG,path+ filename + ext + " does not exist. returning default.");
            bitmap = BitmapFactory.decodeFile(MainActivity.rootDir + "/Icebreak/profile/default.png", options);
        }
        else//exists
        {
            bitmap = BitmapFactory.decodeFile(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, options);
        }
        return  bitmap;
    }

    public static void updateMessageStatusById(Context context, String id, int status)
    {
        MessagePollHelper dbHelper = new MessagePollHelper(context);//getBaseContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //Didn't create DB here because when updating there should already be a DB

        ContentValues kv_pairs = new ContentValues();
        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS, status);

        String where = "? = ?";
        String[] where_args = {MessagePollContract.MessageEntry.COL_MESSAGE_ID, id};
        db.update(MessagePollContract.MessageEntry.TABLE_NAME, kv_pairs,where,where_args);
        db.close();
        Log.d(TAG, "Successfully updated message status on remote and local DB");
    }

    public static ArrayList<Message> getInboundMessages(Context context, String receiver, MESSAGE_STATUSES status)
    {
        //TODO: add to db task queue/stack
        MessagePollHelper dbHelper = new MessagePollHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        dbHelper.onCreate(db);

        ArrayList<Message> messages = new ArrayList<Message>();

        String query = "SELECT * FROM " + MessagePollContract.MessageEntry.TABLE_NAME + " WHERE "
                + MessagePollContract.MessageEntry.COL_MESSAGE_STATUS + " = ? AND "
                + MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER + " = ?";

        Cursor c = db.rawQuery(query, new String[]
                {
                    String.valueOf(status.getStatus()),
                    receiver
                });

        while(c.moveToNext())
        {
            Message m = new Message();
            String mgid = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_ID));
            String send = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_SENDER));
            String msg = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE));
            String recv = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER));
            String time = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_TIME));
            int stat = c.getInt(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS));

            //System.err.println(msg + ":" + stat);

            m.setId(mgid);
            m.setSender(send);
            m.setMessage(msg);
            m.setReceiver(recv);
            m.setTime(time);
            m.setStatus(stat);

            messages.add(m);
        }
        //Close DB
        db.close();

        return messages;
    }

    public static void addContact(Context context, User new_contact)
    {
        UserHelper dbHelper = new UserHelper(context);//getBaseContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.onCreate(db);//Create Contacts table in DB if it doesn't exist

        ContentValues kv_pairs = new ContentValues();

        kv_pairs.put(UserContract.UserEntry.COL_USER_USERNAME, new_contact.getUsername());
        kv_pairs.put(UserContract.UserEntry.COL_USER_FNAME, new_contact.getFirstname());
        kv_pairs.put(UserContract.UserEntry.COL_USER_LNAME, new_contact.getLastname());
        kv_pairs.put(UserContract.UserEntry.COL_USER_AGE, new_contact.getAge());
        kv_pairs.put(UserContract.UserEntry.COL_USER_BIO, new_contact.getBio());
        kv_pairs.put(UserContract.UserEntry.COL_USER_CATCHPHRASE, new_contact.getCatchphrase());
        kv_pairs.put(UserContract.UserEntry.COL_USER_OCCUPATION, new_contact.getOccupation());
        kv_pairs.put(UserContract.UserEntry.COL_USER_GENDER, new_contact.getGender());

        long newRowId = -1;
        if(!userExistsInDB(context,new_contact.getUsername()))
        {
            newRowId = db.insert(UserContract.UserEntry.TABLE_NAME, null, kv_pairs);
            System.err.println("New contact ==> "+ newRowId);
        }
        else
            Log.d(TAG,"User exists in local DB");

        //Close DB connection
        db.close();
    }

    public static boolean userExistsInDB(Context ctxt, String username)
    {
        String query ="SELECT * FROM " + UserContract.UserEntry.TABLE_NAME + " WHERE "
                + UserContract.UserEntry.COL_USER_USERNAME +" = ?";

        UserHelper dbHelper = new UserHelper(ctxt);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        dbHelper.onCreate(db);

        Cursor c =  db.rawQuery(query, new String[] {username});
        int rowCount=c.getCount();
        db.close();
        if(rowCount>0)
            return true;
        else
            return  false;
    }

    public static Message getMessageById(Context context, String id)
    {
        //TODO: add to db task queue/stack
        MessagePollHelper dbHelper = new MessagePollHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        dbHelper.onCreate(db);


        String query = "SELECT * FROM " + MessagePollContract.MessageEntry.TABLE_NAME + " WHERE "
                + MessagePollContract.MessageEntry.COL_MESSAGE_ID + " = ?";

        Cursor c = db.rawQuery(query, new String[]
                {
                    id
                });

        c.moveToFirst();

        Message m = new Message();
        String mgid = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_ID));
        String send = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_SENDER));
        String msg = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE));
        String recv = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER));
        String time = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_TIME));
        int stat = c.getInt(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS));

        //System.err.println(msg + ":" + stat);

        m.setId(mgid);
        m.setSender(send);
        m.setMessage(msg);
        m.setReceiver(recv);
        m.setTime(time);
        m.setStatus(stat);

        //Close DB
        db.close();

        return m;
    }

    public static ArrayList<Message> getOutboundMessages(Context context, String sender, MESSAGE_STATUSES status)
    {
        MessagePollHelper dbHelper = new MessagePollHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        dbHelper.onCreate(db);

        ArrayList<Message> messages = new ArrayList<Message>();

        String query = "SELECT * FROM " + MessagePollContract.MessageEntry.TABLE_NAME + " WHERE "
                + MessagePollContract.MessageEntry.COL_MESSAGE_STATUS + " = ? AND "
                + MessagePollContract.MessageEntry.COL_MESSAGE_SENDER + " = ?";

        Cursor c = db.rawQuery(query, new String[]
                {
                        String.valueOf(status.getStatus()),
                        sender
                });

        while(c.moveToNext())
        {
            Message m = new Message();
            String mgid = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_ID));
            String send = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_SENDER));
            String msg = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE));
            String recv = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER));
            String time = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_TIME));
            int stat = c.getInt(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS));

            m.setId(mgid);
            m.setSender(send);
            m.setMessage(msg);
            m.setReceiver(recv);
            m.setTime(time);
            m.setStatus(stat);
        }
        //Close DB
        db.close();

        return messages;
    }

    public static void validateStoragePermissions(Activity activity)
    {
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE =
                {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
        //Check for write permissions
        int w_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int r_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (w_permission != PackageManager.PERMISSION_GRANTED || r_permission != PackageManager.PERMISSION_GRANTED)
        {
            //No permission - prompt the user for permission
            ActivityCompat.requestPermissions
                    (
                            activity,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE
                    );
        }
    }
}
