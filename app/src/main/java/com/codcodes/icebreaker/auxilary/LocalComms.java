package com.codcodes.icebreaker.auxilary;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.MessagePollContract;
import com.codcodes.icebreaker.model.MessagePollHelper;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.model.UserContract;
import com.codcodes.icebreaker.model.UserHelper;
import com.codcodes.icebreaker.screens.MainActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Casper on 2016/08/16.
 */
public class LocalComms
{
    private static final String TAG = "IB/LocalComms";

    public static User getLocalUser(String username, Context context)
    {
        UserHelper dbHelper = new UserHelper(context);
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
            String lname = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_LNAME));
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
        if(!ext.contains("."))//add dot to image extension if it's not there
            ext = '.' + ext;
        //Look for image locally
        if (!new File(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext).exists())
        {
            Log.d(TAG,path+ filename + ext + " does not exist. returning default.");
            //bitmap = BitmapFactory.decodeFile(MainActivity.rootDir + "/Icebreak"+path+"/default.png", options);
            return null;
        }
        else//exists
            return BitmapFactory.decodeFile(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, options);
    }

    public static void showImageProgressBar(ProgressBar pb)
    {
        if(pb!=null)
        {
            pb.setVisibility(View.VISIBLE);
            pb.setIndeterminate(true);
            pb.setActivated(true);
            pb.setEnabled(true);
        } else Log.d(TAG,"ProgressBar is null.");
    }

    public static void hideImageProgressBar(ProgressBar pb)
    {
        if(pb!=null)
        {
            pb.setVisibility(View.GONE);
            pb.setIndeterminate(false);
            pb.setActivated(false);
            pb.setEnabled(false);
        } else Log.d(TAG,"ProgressBar is null.");
    }

    public static  void updateLocalMessage(Context context, SQLiteDatabase db, Message m)
    {
        ContentValues kv_pairs = new ContentValues();
        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_ID, m.getId());
        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE, m.getMessage());
        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_SENDER, m.getSender());
        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER, m.getReceiver());
        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS, m.getStatus());
        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_TIME, m.getTime());

        if(!messageIdExistsInDB(context,m.getId()))//Shouldn't technically ever happen
        {

            if (kv_pairs.size() > 0)
            {
                long newRowId = db.insert(MessagePollContract.MessageEntry.TABLE_NAME, null, kv_pairs);
                Log.d(TAG, "Inserted into Message table: new row=" + newRowId);
            }
        }
        else
        {
            String where = MessagePollContract.MessageEntry.COL_MESSAGE_ID + " = ?";
            String[] where_args = {m.getId()};

            db.update(MessagePollContract.MessageEntry.TABLE_NAME, kv_pairs,where,where_args);
            Log.d(TAG, "Outbound Message exists (locally and remotely), updated status to " + m.getStatus());
        }
    }

    public static void showNotification(Context context, String msg, int notifId)
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle("IceBreak")
                        .setContentText(msg);
        //.setContentText("New IceBreak request from "+sender+"!");

        Intent resultIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(notifId, mBuilder.build());
    }

    public static String getValidatedName(User u)
    {
        if(u.getFirstname()==null)
            u.setFirstname("");
        if(u.getLastname()==null)
            u.setLastname("");
        String fname = u.getFirstname().length()<=0 ? "X" : u.getFirstname();
        String lname = u.getFirstname().length()<=0 ? "X" : u.getLastname();
        String name = "";
        if (!fname.equals("X") && !lname.equals("X"))
            name = fname + " " + lname.charAt(0) + '.';
        else
            name = "Anonymous";
        return name;
    }

    public static void addMessageToLocalDB(Context context, Message m) throws IOException
    {
        MessagePollHelper dbHelper = new MessagePollHelper(context);//getBaseContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.onCreate(db);

        //Update local and remote statuses on DB
        //Check for Icebreaks
        if (m.getStatus() == MESSAGE_STATUSES.ICEBREAK_SERV_RECEIVED.getStatus())
        {
            Log.d(TAG, "New IceBreak request from " + m.getSender());
            //showNotification(context, "New IceBreak request from " + m.getSender(), NOTIFICATION_ID.NOTIF_REQUEST.getId());
            m.setStatus(MESSAGE_STATUSES.ICEBREAK_DELIVERED.getStatus());//set message to delivered in temp memory

            //Change Message status to DELIVERED remotely
            if(RemoteComms.sendMessage(context,m))
            {
                Log.d(TAG, "Updated remote status.");
            }
            else
            {
                Log.d(TAG, "Couldn't update remote status.");
            }
        }

        //Change Message status to DELIVERED locally
        if (!messageIdExistsInDB(context, m.getId()))//new Message
        {
            ContentValues kv_pairs = new ContentValues();
            kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_ID, m.getId());
            kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE, m.getMessage());
            kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_SENDER, m.getSender());
            kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER, m.getReceiver());
            kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS, m.getStatus());
            kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_TIME, m.getTime());

            long newRowId = db.insert(MessagePollContract.MessageEntry.TABLE_NAME, null, kv_pairs);
            Log.d(TAG, "Inserted into Message table: new row=" + newRowId);
        }
        else //Existing Message
        {
            updateMessageStatusById(context,m.getId(),m.getStatus());
            Log.d(TAG, "Message already exists table, updated status");
        }
        Log.d(TAG, "Updated local DB");
        db.close();
    }

    public static boolean messageIdExistsInDB(Context ctxt, String id)
    {
        String query ="SELECT * FROM "+MessagePollContract.MessageEntry.TABLE_NAME+" WHERE "
                + MessagePollContract.MessageEntry.COL_MESSAGE_ID +" = ?";

        MessagePollHelper dbHelper = new MessagePollHelper(ctxt);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c =  db.rawQuery(query, new String[] {id});
        int rowCount=c.getCount();
        db.close();
        if(rowCount>0)
            return true;
        else
            return  false;
    }

    public static void updateMessageStatusById(Context context, String id, int status)
    {
        MessagePollHelper dbHelper = new MessagePollHelper(context);//getBaseContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //Didn't create DB here because when updating there should already be a DB

        /*ContentValues kv_pairs = new ContentValues();
        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS, status);*/

        /*String where = "? = ?";
        String[] where_args = {MessagePollContract.MessageEntry.COL_MESSAGE_ID, id};
        db.update(MessagePollContract.MessageEntry.TABLE_NAME, kv_pairs,where,where_args);*/
        String q = "UPDATE " + MessagePollContract.MessageEntry.TABLE_NAME +
                " SET " + MessagePollContract.MessageEntry.COL_MESSAGE_STATUS + " =? WHERE " +
                MessagePollContract.MessageEntry.COL_MESSAGE_ID + "=?";
        String[] args = {String.valueOf(status), id};
        db.execSQL(q,args);
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

        try
        {
            Cursor c = db.rawQuery(query, new String[]
                    {
                            String.valueOf(status.getStatus()),
                            receiver
                    });

            while (c.moveToNext()) {
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
        }
        catch (SQLiteCantOpenDatabaseException e)
        {
            Log.wtf(TAG,e.getMessage(),e);
            //TODO: Better logging
        }
        //Close DB
        closeDB(db);
        return messages;
    }

    private static void closeDB(SQLiteDatabase db)
    {
        if(db!=null)
            if(db.isOpen())
                db.close();
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

    public static User getContact(Context ctxt, String username)
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
        {
            if(c.moveToFirst())
            {
                User u = new User();
                String usr = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_USERNAME));
                String fname = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_FNAME));
                String lname = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_LNAME));
                int age = c.getInt(c.getColumnIndex(UserContract.UserEntry.COL_USER_AGE));
                String phrase = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_CATCHPHRASE));
                String occ = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_OCCUPATION));
                String gend = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_GENDER));
                String bio = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_BIO));
                //String eml = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_EMAIL));
                u.setUsername(username);
                u.setOccupation(occ);
                u.setEmail("NULL");
                return u;
            }
        }
        return null;
    }

    public static ArrayList<User> getContacts(Context ctxt)
    {
        ArrayList<User> contacts = new ArrayList<>();

        String query ="SELECT * FROM " + UserContract.UserEntry.TABLE_NAME;

        UserHelper dbHelper = new UserHelper(ctxt);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        dbHelper.onCreate(db);

        Cursor c =  db.rawQuery(query, new String[] {});
        int rowCount=c.getCount();
        db.close();
        if(rowCount>0)
        {
            while (c.moveToNext())
            {
                User u = new User();
                String usr = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_USERNAME));
                String fname = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_FNAME));
                String lname = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_LNAME));
                int age = c.getInt(c.getColumnIndex(UserContract.UserEntry.COL_USER_AGE));
                String phrase = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_CATCHPHRASE));
                String occ = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_OCCUPATION));
                String gend = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_GENDER));
                String bio = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_BIO));
                //String eml = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_EMAIL));
                u.setUsername(usr);
                u.setOccupation(occ);
                u.setEmail("NULL");
                contacts.add(u);
            }
            return contacts;
        }
        return null;
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

    public static ArrayList<Message> getOutboundMessages(Context context, String sender)
    {
        MessagePollHelper dbHelper = new MessagePollHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        dbHelper.onCreate(db);

        ArrayList<Message> messages = new ArrayList<Message>();

        String query = "SELECT * FROM " + MessagePollContract.MessageEntry.TABLE_NAME + " WHERE NOT "
                + MessagePollContract.MessageEntry.COL_MESSAGE_STATUS + " = ? AND "
                + MessagePollContract.MessageEntry.COL_MESSAGE_SENDER + " = ?";
        /*String query = "SELECT * FROM " + MessagePollContract.MessageEntry.TABLE_NAME + " WHERE "
                + MessagePollContract.MessageEntry.COL_MESSAGE_SENDER + " = ?";*/

        Cursor c = db.rawQuery(query, new String[]
                {
                        String.valueOf(MESSAGE_STATUSES.ICEBREAK_DONE.getStatus()),
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

            messages.add(m);
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
