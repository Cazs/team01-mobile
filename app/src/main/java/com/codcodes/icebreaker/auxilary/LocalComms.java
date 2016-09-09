package com.codcodes.icebreaker.auxilary;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Path;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
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

    public static Bitmap getImage(Context context, String filename,String ext, String path, BitmapFactory.Options options)
    {
        path = path.charAt(0) != '/' && path.charAt(0) != '\\' ? '/' + path : path;
        if(!ext.contains("."))//add dot to image extension if it's not there
            ext = '.' + ext;
        //Look for image locally
        if (!new File(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext).exists())
        {
            Log.d(TAG,MainActivity.rootDir + "/Icebreak" + path+ '/' + filename + ext + " does not exist.");
            //bitmap = BitmapFactory.decodeFile(MainActivity.rootDir + "/Icebreak"+path+"/default.png", options);
            return null;
        }
        else//exists
        {
            Log.d(TAG,filename + ext+" exists.");
            //Bitmap bitmap = BitmapFactory.decodeFile(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, options);
            Bitmap compressed = ImageUtils.getInstance().compressBitmapImage(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext,context);
            //bitmap.recycle();
            return compressed;
        }
    }

    public static ProgressDialog showProgressDialog(Context context, String msg)
    {
        ProgressDialog progress = new ProgressDialog(context);
        progress.setCanceledOnTouchOutside(false);

        //if(!progress.isShowing())
        {
            progress.setMessage(msg);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setCancelable(false);
            progress.setIndeterminate(true);
            progress.show();
        }

        return progress;
    }

    public static void hideProgressBar(ProgressDialog progress)
    {
        if(progress!=null)
            if(progress.isShowing())
                progress.dismiss();
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
        AssetManager mgr = context.getAssets();
        Notification notif = null;
        Intent resultIntent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/assets/sounds/notif.wav");

        notif = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("IceBreak")
                .setVibrate(new long[]{500})
                .setContentText("New IceBreak Request")
                .setSound(soundUri)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentIntent(resultPendingIntent)
                .setLights(Color.CYAN,700,1000)
                .build();

        mNotificationManager.notify(notifId, notif);
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
        SQLiteDatabase db = null;
        try
        {
            MessagePollHelper dbHelper = new MessagePollHelper(context);//getBaseContext());
            db = dbHelper.getWritableDatabase();
            dbHelper.onCreate(db);

            //Update local and remote statuses on DB
            //Check for Icebreaks
            if (m.getStatus() == MESSAGE_STATUSES.ICEBREAK_SERV_RECEIVED.getStatus())
            {
                Log.d(TAG, "New IceBreak request from " + m.getSender());
                //showNotification(context, "New IceBreak request from " + m.getSender(), NOTIFICATION_ID.NOTIF_REQUEST.getId());
                m.setStatus(MESSAGE_STATUSES.ICEBREAK_DELIVERED.getStatus());//set message to delivered in temp memory

                //Change Message status to DELIVERED remotely
                if (RemoteComms.sendMessage(context, m)) {
                    Log.d(TAG, "Updated remote status.");
                } else {
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
            } else //Existing Message
            {
                updateMessageStatusById(context, m.getId(), m.getStatus());
                Log.d(TAG, "Message already exists table, updated status");
            }
        }
        catch (SQLiteCantOpenDatabaseException e)
        {
            Log.wtf(TAG,e.getMessage(),e);
            //TODO: Better logging
        }
        finally
        {
            closeDB(db);
        }
    }

    public static boolean messageIdExistsInDB(Context ctxt, String id)
    {
        SQLiteDatabase db=null;
        int rowCount = 0;
        String query ="SELECT * FROM "+MessagePollContract.MessageEntry.TABLE_NAME+" WHERE "
                + MessagePollContract.MessageEntry.COL_MESSAGE_ID +" = ?";

        try
        {
            MessagePollHelper dbHelper = new MessagePollHelper(ctxt);
            db = dbHelper.getReadableDatabase();
            Cursor c =  db.rawQuery(query, new String[] {id});
            rowCount = c.getCount();
        }
        catch (SQLiteCantOpenDatabaseException e)
        {
            Log.wtf(TAG,e.getMessage(),e);
            //TODO: Better logging
        }
        finally
        {
            closeDB(db);
            if(rowCount>0)
                return true;
            else
                return  false;
        }
    }

    public static void updateMessageStatusById(Context context, String id, int status)
    {
        SQLiteDatabase db = null;
        try
        {
            MessagePollHelper dbHelper = new MessagePollHelper(context);//getBaseContext());
            db = dbHelper.getWritableDatabase();
            //Didn't create DB here because when updating there should already be a DB

            String q = "UPDATE " + MessagePollContract.MessageEntry.TABLE_NAME +
                    " SET " + MessagePollContract.MessageEntry.COL_MESSAGE_STATUS + " =? WHERE " +
                    MessagePollContract.MessageEntry.COL_MESSAGE_ID + "=?";
            String[] args = {String.valueOf(status), id};
            db.execSQL(q,args);

            Log.d(TAG, "Successfully updated message status on remote and local DB");
        }
        catch (SQLiteCantOpenDatabaseException e)
        {
            Log.wtf(TAG,e.getMessage(),e);
            //TODO: Better logging
        }
        finally
        {
            closeDB(db);
        }
    }

    public static ArrayList<Message> getInboundMessages(Context context, String receiver)
    {
        //TODO: add to db task queue/stack
        SQLiteDatabase db = null;
        ArrayList<Message> messages = new ArrayList<Message>();

        String query = "SELECT * FROM " + MessagePollContract.MessageEntry.TABLE_NAME + " WHERE "
                + MessagePollContract.MessageEntry.COL_MESSAGE_STATUS + " = ? AND "
                + MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER + " = ?";

        try
        {
            MessagePollHelper dbHelper = new MessagePollHelper(context);
            db = dbHelper.getReadableDatabase();
            dbHelper.onCreate(db);

            Cursor c = db.rawQuery(query, new String[]
                    {
                            String.valueOf(MESSAGE_STATUSES.ICEBREAK_DELIVERED.getStatus()),
                            receiver
                    });

            while (c.moveToNext())
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
        }
        catch (SQLiteCantOpenDatabaseException e)
        {
            Log.wtf(TAG,e.getMessage(),e);
            //TODO: Better logging
        }
        finally
        {
            closeDB(db);
        }
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
        SQLiteDatabase db = null;
        try
        {
            UserHelper dbHelper = new UserHelper(context);//getBaseContext());
            db = dbHelper.getWritableDatabase();
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
            kv_pairs.put(UserContract.UserEntry.COL_USER_EMAIL, new_contact.getEmail());

            long newRowId = -1;
            if (!userExistsInDB(context, new_contact.getUsername()))
            {
                newRowId = db.insert(UserContract.UserEntry.TABLE_NAME, null, kv_pairs);
                Log.d(TAG,"*New contact ==> " + newRowId);
            } else
            {
                updateContact(context,new_contact);
                Log.d(TAG, "User exists in local DB");
            }
        }
        catch (SQLiteCantOpenDatabaseException e)
        {
            Log.wtf(TAG,e.getMessage(),e);
            //TODO: Better logging
        }
        finally
        {
            closeDB(db);
        }
    }

    public static void updateContact(Context context, User contact)
    {
        SQLiteDatabase db = null;
        try
        {
            UserHelper dbHelper = new UserHelper(context);//getBaseContext());
            db = dbHelper.getWritableDatabase();
            dbHelper.onCreate(db);//Create Contacts table in DB if it doesn't exist

            ContentValues kv_pairs = new ContentValues();

            kv_pairs.put(UserContract.UserEntry.COL_USER_USERNAME, contact.getUsername());
            kv_pairs.put(UserContract.UserEntry.COL_USER_FNAME, contact.getFirstname());
            kv_pairs.put(UserContract.UserEntry.COL_USER_LNAME, contact.getLastname());
            kv_pairs.put(UserContract.UserEntry.COL_USER_AGE, contact.getAge());
            kv_pairs.put(UserContract.UserEntry.COL_USER_BIO, contact.getBio());
            kv_pairs.put(UserContract.UserEntry.COL_USER_CATCHPHRASE, contact.getCatchphrase());
            kv_pairs.put(UserContract.UserEntry.COL_USER_OCCUPATION, contact.getOccupation());
            kv_pairs.put(UserContract.UserEntry.COL_USER_GENDER, contact.getGender());
            kv_pairs.put(UserContract.UserEntry.COL_USER_EMAIL, contact.getEmail());

            if (userExistsInDB(context, contact.getUsername()))
            {
                String where = UserContract.UserEntry.COL_USER_USERNAME + " = ?";
                String[] where_args = {contact.getUsername()};

                db.update(UserContract.UserEntry.TABLE_NAME, kv_pairs,where,where_args);
                Log.d(TAG,"*Updated contact '" + LocalComms.getValidatedName(contact)+"'");
            } else
                Log.d(TAG, "User exists in local DB");
        }
        catch (SQLiteCantOpenDatabaseException e)
        {
            Log.wtf(TAG,e.getMessage(),e);
            //TODO: Better logging
        }
        finally
        {
            closeDB(db);
        }
    }

    public static AlertDialog showAlertDialog(Context context, String title, String msg)
    {
        AlertDialog.Builder alrt = new AlertDialog.Builder(context);
        alrt.setTitle(title);
        alrt.setMessage(msg);
        return  alrt.show();
    }

    public static void hideAlertDialog(AlertDialog alrt)
    {
        if(alrt!=null)
            if(alrt.isShowing())
                alrt.dismiss();
    }

    public static User getContact(Context ctxt, String username)
    {
        SQLiteDatabase db = null;
        User u =null;
        String query ="SELECT * FROM " + UserContract.UserEntry.TABLE_NAME + " WHERE "
                + UserContract.UserEntry.COL_USER_USERNAME +" = ?";

        try
        {
            UserHelper dbHelper = new UserHelper(ctxt);
            db = dbHelper.getReadableDatabase();
            dbHelper.onCreate(db);

            Cursor c =  db.rawQuery(query, new String[] {username});
            int rowCount=c.getCount();

            if(rowCount>0)
            {
                if(c.moveToFirst())
                {
                    u = new User();
                    String usr = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_USERNAME));
                    String fname = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_FNAME));
                    String lname = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_LNAME));
                    int age = c.getInt(c.getColumnIndex(UserContract.UserEntry.COL_USER_AGE));
                    String phrase = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_CATCHPHRASE));
                    String occ = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_OCCUPATION));
                    String gend = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_GENDER));
                    String bio = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_BIO));
                    String eml = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_EMAIL));

                    u.setUsername(username);
                    u.setFirstname(fname);
                    u.setLastname(lname);
                    u.setAge(age);
                    u.setCatchphrase(phrase);
                    u.setOccupation(occ);
                    u.setGender(gend);
                    u.setBio(bio);
                    u.setEmail("NULL");
                }else Log.wtf(TAG,"The impossible has happened, couldn't set DB cursor to first entry when trying get a User even though " +
                        "the username was found.");
            }
        }
        catch (SQLiteCantOpenDatabaseException e)
        {
            Log.wtf(TAG,e.getMessage(),e);
            //TODO: Better logging
        }
        finally
        {
            closeDB(db);
            return u;
        }
    }

    public static ArrayList<User> getContacts(Context ctxt)
    {
        SQLiteDatabase db = null;
        ArrayList<User> contacts = new ArrayList<>();

        String query ="SELECT * FROM " + UserContract.UserEntry.TABLE_NAME;

        try
        {
            UserHelper dbHelper = new UserHelper(ctxt);
            db = dbHelper.getReadableDatabase();
            dbHelper.onCreate(db);

            Cursor c = db.rawQuery(query, new String[]{});
            int rowCount = c.getCount();

            if (rowCount > 0)
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
            }
            else Log.d(TAG,"No contacts were found.");
        }
        catch (SQLiteCantOpenDatabaseException e)
        {
            Log.wtf(TAG,e.getMessage(),e);
            //TODO: Better logging
        }
        finally
        {
            closeDB(db);
            return contacts;
        }
    }

    public static boolean userExistsInDB(Context ctxt, String username)
    {
        SQLiteDatabase db = null;
        int rowCount = 0;

        String query ="SELECT * FROM " + UserContract.UserEntry.TABLE_NAME + " WHERE "
                + UserContract.UserEntry.COL_USER_USERNAME +" = ?";

        try
        {
            UserHelper dbHelper = new UserHelper(ctxt);
            db = dbHelper.getReadableDatabase();
            dbHelper.onCreate(db);

            Cursor c =  db.rawQuery(query, new String[] {username});
            rowCount = c.getCount();
        }
        catch (SQLiteCantOpenDatabaseException e)
        {
            Log.wtf(TAG,e.getMessage(),e);
            //TODO: Better logging
        }
        finally
        {
            closeDB(db);
            if(rowCount>0)
                return true;
            else
                return  false;
        }
    }

    public static Message getMessageById(Context context, String id)
    {
        //TODO: add to db task queue/stack
        SQLiteDatabase db = null;

        String query = "SELECT * FROM " + MessagePollContract.MessageEntry.TABLE_NAME + " WHERE "
                + MessagePollContract.MessageEntry.COL_MESSAGE_ID + " = ?";
        Message m = null;
        try
        {
            MessagePollHelper dbHelper = new MessagePollHelper(context);
            db = dbHelper.getReadableDatabase();
            dbHelper.onCreate(db);

            Cursor c = db.rawQuery(query, new String[]
                    {
                            id
                    });

            if(c.moveToFirst())
            {
                m = new Message();
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
            }else Log.wtf(TAG,"The impossible has happened, couldn't set DB cursor to first entry when trying get Message even though " +
            "the Message_id was found.");
        }
        catch (SQLiteCantOpenDatabaseException e)
        {
            Log.wtf(TAG,e.getMessage(),e);
            //TODO: Better logging
        }
        finally
        {
            closeDB(db);
        }
        return m;
    }

    public static ArrayList<Message> getOutboundMessages(Context context, String sender)
    {
        SQLiteDatabase db = null;

        ArrayList<Message> messages = new ArrayList<Message>();

        String query = "SELECT * FROM " + MessagePollContract.MessageEntry.TABLE_NAME + " WHERE NOT "
                + MessagePollContract.MessageEntry.COL_MESSAGE_STATUS + " = ? AND "
                + MessagePollContract.MessageEntry.COL_MESSAGE_SENDER + " = ?";

        try
        {
            MessagePollHelper dbHelper = new MessagePollHelper(context);
            db = dbHelper.getReadableDatabase();
            dbHelper.onCreate(db);

            Cursor c = db.rawQuery(query, new String[]
                    {
                            String.valueOf(MESSAGE_STATUSES.ICEBREAK_DONE.getStatus()),
                            sender
                    });

            while (c.moveToNext())
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
        }
        catch (SQLiteCantOpenDatabaseException e)
        {
            Log.wtf(TAG,e.getMessage(),e);
            //TODO: Better logging
        }
        finally
        {
            closeDB(db);
        }
        return messages;
    }

    public static void validateStoragePermissions(Activity activity)
    {
        int REQUEST = 1;
        String[] PERMISSIONS =
                {
                        Manifest.permission.INTERNET,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                };
        //Check for write permissions
        //int w_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //int r_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        //if (w_permission != PackageManager.PERMISSION_GRANTED || r_permission != PackageManager.PERMISSION_GRANTED)
        ArrayList<String> not_granted = new ArrayList<>();
        for(String s:PERMISSIONS)
        {
            int permission = ActivityCompat.checkSelfPermission(activity, s);
            if(permission != PackageManager.PERMISSION_GRANTED)
            {
                not_granted.add(s);
            }
        }

        Object[] arr = not_granted.toArray();
        String[] perms = new String[arr.length];
        System.arraycopy(arr,0,perms,0,arr.length);

        if(not_granted.size()>0)
        {
            //Some permissions not granted - prompt the user for permission
            ActivityCompat.requestPermissions
                    (
                            activity,
                            perms,
                            REQUEST
                    );
        }
    }
}
