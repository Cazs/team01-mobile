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
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import com.codcodes.icebreaker.model.Achievement;
import com.codcodes.icebreaker.model.AchievementContract;
import com.codcodes.icebreaker.model.AchievementHelper;
import com.codcodes.icebreaker.model.Event;
import com.codcodes.icebreaker.model.EventContract;
import com.codcodes.icebreaker.model.EventHelper;
import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.MessagePollContract;
import com.codcodes.icebreaker.model.MessageHelper;
import com.codcodes.icebreaker.model.Metadata;
import com.codcodes.icebreaker.model.MetadataContract;
import com.codcodes.icebreaker.model.MetadataHelper;
import com.codcodes.icebreaker.model.Reward;
import com.codcodes.icebreaker.model.RewardContract;
import com.codcodes.icebreaker.model.RewardHelper;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.model.UserContract;
import com.codcodes.icebreaker.model.UserHelper;
import com.codcodes.icebreaker.screens.MainActivity;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Casper on 2016/08/16.
 */
public class LocalComms
{
    private static final String TAG = "IB/LocalComms";
    private static boolean ib_dlg_active = false;

    public static Bitmap getImage(Context context, String filename,String ext, String path, BitmapFactory.Options options) throws IOException
    {
        path = path.charAt(0) != '/' && path.charAt(0) != '\\' ? '/' + path : path;
        if(!ext.contains("."))//add dot to image extension if it's not there
            ext = '.' + ext;
        //Look for image locally
        if (!new File(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext).exists())
        {
            Log.d(TAG,MainActivity.rootDir + "/Icebreak" + path+ '/' + filename + ext + " does not exist.");
            //bitmap = BitmapFactory.decodeFile(MainActivity.rootDir + "/Icebreak"+path+"/default.png", options);
            return RemoteComms.getImage(context, filename, ext, path, options);
        }
        else//exists
        {
            //return local image
            return ImageUtils.getInstance().compressBitmapImage(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, context);
        }
    }

    public static Bitmap getUpToDateImage(Context context, String filename,String ext, String path, BitmapFactory.Options options) throws IOException
    {
        path = path.charAt(0) != '/' && path.charAt(0) != '\\' ? '/' + path : path;
        if(!ext.contains("."))//add dot to image extension if it's not there
            ext = '.' + ext;
        //Look for image locally
        if (!new File(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext).exists())
        {
            Log.d(TAG,MainActivity.rootDir + "/Icebreak" + path+ '/' + filename + ext + " does not exist.");
            //bitmap = BitmapFactory.decodeFile(MainActivity.rootDir + "/Icebreak"+path+"/default.png", options);
            return RemoteComms.getImage(context, filename, ext, path, options);
        }
        else//exists
        {
            //Get remote meta data for file
            String file_id = path+'|'+filename+ext;
            //remove any remaining slashes
            String temp="";
            for(char c:file_id.toCharArray())
                if(c=='/'||c=='\\')
                    temp += '|';
                else temp += c;
            file_id = temp;

            if(file_id.charAt(0)=='|')
                file_id=file_id.substring(1);//remove first slash

            //get remote metadata for the file
            String payload = RemoteComms.sendGetRequest("getMeta/"+file_id);
            if(payload!=null)
            {
                if(!payload.isEmpty())
                {
                    if(!payload.toLowerCase().equals("error"))
                    {
                        Metadata remote_metadata = new Metadata();
                        JSON.getJsonable(payload, remote_metadata);

                        if(remote_metadata.getEntry()==null||remote_metadata.getMeta()==null)
                        {
                            Log.d(TAG,"[~"+path + '/' + filename + ext +"] Remote Metadata Entry or Meta is null.");
                            return ImageUtils.getInstance().compressBitmapImage(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, context);
                        }

                        if(remote_metadata.getEntry().toLowerCase().equals("null")||remote_metadata.getMeta().toLowerCase().equals("null"))
                        {
                            Log.d(TAG,"[~"+path + '/' + filename + ext +"] Remote Metadata Entry or Meta is null.");
                            return ImageUtils.getInstance().compressBitmapImage(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, context);
                        }

                        if(remote_metadata.getEntry().toLowerCase().equals("error")||remote_metadata.getMeta().toLowerCase().equals("error"))
                        {
                            Log.d(TAG,"[~"+path + '/' + filename + ext +"] Remote Metadata returned an error.");
                            return ImageUtils.getInstance().compressBitmapImage(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, context);
                        }

                        String lmDat = null;
                        try
                        {
                            lmDat = LocalComms.getMetaRecord(context, file_id);
                        }catch (SQLiteException e)
                        {
                            LocalComms.logException(e);
                        }

                        /*
                         * Save Metadata to disk,
                         * It's fine to save it here because you've already loaded local
                         * Metadata to memory - if it exists.
                         */

                        LocalComms.addMetaRecord(context,remote_metadata.getEntry(),remote_metadata.getMeta());

                        if(lmDat!=null)//local file has metadata.
                        {
                            Metadata local_metadata = new Metadata(file_id,lmDat);
                            try
                            {
                                long r_dmd = Long.parseLong(remote_metadata.getAttribute(Config.META_DATE_MODIFIED.getValue()));
                                long l_dmd = Long.parseLong(local_metadata.getAttribute(Config.META_DATE_MODIFIED.getValue()));

                                if(l_dmd==r_dmd)//local file is up to date
                                {
                                    Log.d(TAG,"Local file[~"+path + '/' + filename + ext +"] is up to date.");
                                    Bitmap bmp = ImageUtils.getInstance().compressBitmapImage(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, context);
                                    if(bmp==null)
                                    {
                                        //if meta is up-to-date but image doesn't actually exist - delete record.
                                        deleteMetaRecordById(context,local_metadata.getEntry());
                                        return RemoteComms.getImage(context, filename, ext, path, options);
                                    }
                                    return bmp;
                                }else return RemoteComms.getImage(context, filename, ext, path, options);
                                /*Config mod = local_metadata.compareDateModified(r_dmd);
                                if (mod.getValue().equals(Config.META_PARAM_NEWER.getValue()))//remote file is newer
                                    return RemoteComms.getImage(context, filename, ext, path, options);
                                else if (mod.getValue().equals(Config.META_PARAM_EQUAL.getValue()))//local file is up to date
                                {
                                    Log.d(TAG,"Local file[~"+path + '/' + filename + ext +"] is up to date.");
                                    return ImageUtils.getInstance().compressBitmapImage(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, context);
                                }*/

                            } catch (NumberFormatException e)
                            {
                                LocalComms.logException(e);
                            }
                            return ImageUtils.getInstance().compressBitmapImage(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, context);
                        }else//local file doesn't have metadata yet if null
                        {
                            Log.d(TAG,"Local file[~"+path + '/' + filename + ext +"] has no local metadata.");
                            return ImageUtils.getInstance().compressBitmapImage(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, context);
                        }
                    }else
                    {
                        Log.wtf(TAG,payload);
                        return ImageUtils.getInstance().compressBitmapImage(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, context);
                    }
                }else
                {
                    Log.wtf(TAG,payload);
                    return ImageUtils.getInstance().compressBitmapImage(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, context);
                }
            }else
            {
                Log.wtf(TAG,payload);
                return ImageUtils.getInstance().compressBitmapImage(MainActivity.rootDir + "/Icebreak" + path + '/' + filename + ext, context);
            }
        }
    }

    public static void deleteMetaRecordById(Context context, String entry)  throws IOException
    {
        if(context!=null && entry!=null)
        {
            EventHelper dbHelper = new EventHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            String where = MetadataContract.MetaEntry.COL_META_ENTRY+"=?";
            String[] where_args = {entry};
            db.delete(MetadataContract.MetaEntry.TABLE_NAME,where,where_args);

            closeDB(db);
        }else
        {
            Log.d(TAG,"Context or entry is null.");
        }
    }

    public static void addEvent(Context context, Event e) throws IOException
    {
        if(e.isValid())
        {
            //Event ev = getEvent(context, e.getId()); - causes an infinite loop
            EventHelper dbHelper = new EventHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            dbHelper.onCreate(db);//create table if it doesn't exist

            String query = "SELECT * FROM " + EventContract.EventEntry.TABLE_NAME + " WHERE " +
                    EventContract.EventEntry.COL_EVENT_ID+"=?";

            Cursor c = db.rawQuery(query,new String[]{String.valueOf(e.getId())});

            long rows = c.getCount();

            if(c!=null)
                if(!c.isClosed())
                    c.close();

            closeDB(db);

            if(rows<=0)//Event DNE
            {
                db = dbHelper.getWritableDatabase();
                dbHelper.onCreate(db);

                ContentValues values = new ContentValues();
                values.put(EventContract.EventEntry.COL_EVENT_ID, e.getId());
                values.put(EventContract.EventEntry.COL_EVENT_TITLE, e.getTitle());
                values.put(EventContract.EventEntry.COL_EVENT_ADDRESS, e.getAddress());
                values.put(EventContract.EventEntry.COL_EVENT_DATE, e.getDate());
                values.put(EventContract.EventEntry.COL_EVENT_DESCRIPTION, e.getDescription());
                values.put(EventContract.EventEntry.COL_EVENT_END_DATE, e.getEndDate());

                StringBuilder loc = new StringBuilder();
                for (LatLng coord : e.getBoundary())
                    loc.append(String.valueOf(coord.latitude) + "," + String.valueOf(coord.longitude) + ";");
                String location = loc.toString();
                if (location.length() > 0)
                    location = (location.charAt(location.length() - 1) == ';' ? location.substring(0, location.length() - 1) : location);

                values.put(EventContract.EventEntry.COL_EVENT_LOCATION, location);
                String places = "";
                for (String place : e.getMeetingPlaces())
                    places += place + ';';
                if (places.length() > 0)
                    places = (places.charAt(places.length() - 1) == ';' ? places.substring(0, places.length() - 1) : places);
                values.put(EventContract.EventEntry.COL_EVENT_MEETING_PLACES, places);
                values.put(EventContract.EventEntry.COL_EVENT_ACCESS_CODE, e.getAccessCode());

                db.insert(EventContract.EventEntry.TABLE_NAME, null, values);

                closeDB(db);

                Log.d(TAG, "Inserted new Event[" + e.getId() + "].");
            }else updateEvent(context,e);//Event exists.
        }
        else
        {
            Log.d(TAG, "addEvent> Invalid Event.");
        }
    }

    public static void updateEvent(Context context, Event e)
    {
        if(e.isValid())
        {
            EventHelper dbHelper = new EventHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(EventContract.EventEntry.COL_EVENT_ID,e.getId());
            values.put(EventContract.EventEntry.COL_EVENT_TITLE,e.getTitle());
            values.put(EventContract.EventEntry.COL_EVENT_ADDRESS,e.getAddress());
            values.put(EventContract.EventEntry.COL_EVENT_DATE,e.getDate());
            values.put(EventContract.EventEntry.COL_EVENT_DESCRIPTION,e.getDescription());
            values.put(EventContract.EventEntry.COL_EVENT_END_DATE,e.getEndDate());
            String loc="";
            for(LatLng coord:e.getBoundary())
                loc+=coord.latitude+','+coord.longitude+';';
            if(loc.length()>0)
                loc = (loc.charAt(loc.length()-1)==';'?loc.substring(0,loc.length()-1):loc);
            values.put(EventContract.EventEntry.COL_EVENT_LOCATION,loc);
            String places="";
            for(String place:e.getMeetingPlaces())
                places+=place+';';
            if(places.length()>0)
                places = (places.charAt(places.length()-1)==';'?places.substring(0,places.length()-1):places);
            values.put(EventContract.EventEntry.COL_EVENT_MEETING_PLACES,places);
            values.put(EventContract.EventEntry.COL_EVENT_ACCESS_CODE,e.getAccessCode());

            String where = EventContract.EventEntry.COL_EVENT_ID+"=?";
            String[] where_args = {String.valueOf(e.getId())};
            db.update(EventContract.EventEntry.TABLE_NAME,values,where,where_args);

            closeDB(db);
            Log.d(TAG,"Updated Event["+e.getId()+"].");
        }
        else
        {
            Log.d(TAG, "updateEvent> Invalid Event.");
        }
    }

    public static Event getEvent(Context context, long id) throws IOException
    {
        Event event=null;

        EventHelper dbHelper = new EventHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        dbHelper.onCreate(db);//create table if it doesn't exist - special exception for this method

        String query = "SELECT * FROM " + EventContract.EventEntry.TABLE_NAME + " WHERE " +
                EventContract.EventEntry.COL_EVENT_ID+"=?";

        Cursor c = db.rawQuery(query,new String[]{String.valueOf(id)});

        if(c.getCount()>0)
        {
            //Record exists
            if(c.moveToFirst())
            {
                //long id = c.getLong(c.getColumnIndex(EventContract.EventEntry.COL_EVENT_ID));
                String title = c.getString(c.getColumnIndex(EventContract.EventEntry.COL_EVENT_TITLE));
                String address = c.getString(c.getColumnIndex(EventContract.EventEntry.COL_EVENT_ADDRESS));
                String date = c.getString(c.getColumnIndex(EventContract.EventEntry.COL_EVENT_DATE));
                String description = c.getString(c.getColumnIndex(EventContract.EventEntry.COL_EVENT_DESCRIPTION));
                String end_date = c.getString(c.getColumnIndex(EventContract.EventEntry.COL_EVENT_END_DATE));
                String location = c.getString(c.getColumnIndex(EventContract.EventEntry.COL_EVENT_LOCATION));
                String places = c.getString(c.getColumnIndex(EventContract.EventEntry.COL_EVENT_MEETING_PLACES));
                int access_code = c.getInt(c.getColumnIndex(EventContract.EventEntry.COL_EVENT_ACCESS_CODE));

                event = new Event();
                event.setId(id);
                event.setTitle(title);
                event.setAddress(address);
                event.setDate(date);
                event.setDescription(description);
                event.setEndDate(end_date);
                event.setBoundary(location);
                if(places!=null)
                    event.setMeetingPlaces(places.split(";"));
                event.setAccessCode(access_code);

                //Metadata stuff
                String payload = RemoteComms.sendGetRequest("getMeta/event="+id);
                if(payload!=null)
                {
                    if(!payload.isEmpty())
                    {
                        if(!payload.toLowerCase().equals("error"))
                        {
                            Metadata remote_metadata = new Metadata();
                            JSON.getJsonable(payload, remote_metadata);

                            if(remote_metadata.getEntry()==null||remote_metadata.getMeta()==null)
                            {
                                if(c!=null)
                                    if(!c.isClosed())
                                        c.close();
                                closeDB(db);
                                return event;
                            }

                            if(remote_metadata.getEntry().toLowerCase().equals("null")||remote_metadata.getMeta().toLowerCase().equals("null"))
                            {
                                if(c!=null)
                                    if(!c.isClosed())
                                        c.close();
                                closeDB(db);
                                return event;
                            }

                            if(remote_metadata.getEntry().toLowerCase().equals("error")||remote_metadata.getMeta().toLowerCase().equals("error"))
                            {
                                if(c!=null)
                                    if(!c.isClosed())
                                        c.close();
                                closeDB(db);
                                return event;
                            }

                            String lmDat = null;
                            try
                            {
                                lmDat = LocalComms.getMetaRecord(context, "event="+event.getId());
                            }catch (SQLiteException e)
                            {
                                LocalComms.logException(e);
                            }

                            /*
                             * Save Metadata to disk,
                             * It's fine to save it here because you've already loaded local
                             * Metadata to memory - if it exists.
                             */

                            LocalComms.addMetaRecord(context,remote_metadata.getEntry(),remote_metadata.getMeta());

                            if(lmDat!=null)//local file has metadata.
                            {
                                Metadata local_metadata = new Metadata("event="+event.getId(),lmDat);

                                try
                                {

                                    /*Config mod = local_metadata.compareDateModified(r_dmd);
                                    if (mod.getValue().equals(Config.META_PARAM_NEWER.getValue()))//remote data is newer
                                    {
                                        event = RemoteComms.getEvent(id);
                                        addEvent(context,event);//new Event
                                    }
                                    else if (mod.getValue().equals(Config.META_PARAM_EQUAL.getValue()))//local data is up-to-date
                                        Log.d(TAG,"[event=" + event.getId()+"] is up-to-date.");*/


                                    long r_dmd = Long.parseLong(remote_metadata.getAttribute(Config.META_DATE_MODIFIED.getValue()));
                                    long l_dmd = Long.parseLong(local_metadata.getAttribute(Config.META_DATE_MODIFIED.getValue()));

                                    if(l_dmd==r_dmd)//local file is up to date
                                    {
                                        Log.d(TAG,"[event=" + event.getId()+"] is up-to-date.");
                                    }else
                                    {
                                        Log.d(TAG,"[event=" + event.getId()+"] is outdated.");
                                        event = RemoteComms.getEvent(id);
                                        addEvent(context,event);//new Event
                                    }
                                } catch (NumberFormatException e)
                                {
                                    LocalComms.logException(e);
                                }
                            }else{}//local file doesn't have metadata yet if null
                        }else Log.wtf(TAG,">>>>>>"+payload);
                    }else Log.wtf(TAG,">>>>>>"+payload);
                }else Log.wtf(TAG,">>>>>>"+payload);
            }else Log.wtf(TAG,"getEvent> Could not move Cursor to first result-set record.");
        }
        else//Event not in local DB check remote DB
        {
            event = RemoteComms.getEvent(id);
            if(event!=null)
                Log.d(TAG,"[event=" + event.getId()+"] is not in local records.");
            addEvent(context,event);//new Event
        }

        if(c!=null)
            if(!c.isClosed())
                c.close();
        closeDB(db);

        return event;
    }

    public static void logException(Exception e)
    {
        if(e!=null)
        {
            if (e.getMessage() != null)
                Log.wtf(TAG, e.getMessage(), e);
            else
                e.printStackTrace();
        }else Log.wtf(TAG,"Null exception.",e);
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

    public static boolean addMetaRecord(Context context, String entry, String value)
    {
        if(context!=null)
        {
            String meta=null;
            try
            {
                meta = getMetaRecord(context, entry);
            }
            catch (SQLiteException e)
            {
                if(e.getMessage()!=null)
                    Log.d(TAG,e.getMessage());
                else e.printStackTrace();
            }
            if (meta == null)//dne
            {
                MetadataHelper dbHelper = new MetadataHelper(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                dbHelper.onCreate(db);

                ContentValues values = new ContentValues();
                values.put(MetadataContract.MetaEntry.COL_META_ENTRY, entry);
                values.put(MetadataContract.MetaEntry.COL_META_ENTRY_DATA, value);

                db.insert(MetadataContract.MetaEntry.TABLE_NAME, null, values);

                closeDB(db);

                Log.d(TAG,"addMetaRecord> Added new Meta Record.");
            }else
            {
                if(!meta.contains(value))//change local meta if there has been a change in metadata remotely
                    return updateMetaEntry(context,entry,value, meta);
                else Log.d(TAG,"addMetaRecord> Record["+entry+"] exists and is up-to-date.");
            }
            return true;
        }else
        {
            Log.d(TAG,"addMetaRecord> Context is null.");
            return  false;
        }
    }

    public static boolean updateMetaEntry(Context context, String entry, String value, String meta) throws SQLiteException
    {
        if(value==null)
        {
            Log.wtf(TAG,"Invalid entry value.");
            return false;
        }
        if(!value.contains("="))
        {
            Log.wtf(TAG,"Invalid entry value.");
            return false;
        }
        MetadataHelper dbHelper = new MetadataHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Metadata m = new Metadata(entry,meta);
        m.getAttribute(value.split("=")[0]);

        //Search for attribute
        String var,val;
        StringBuilder sb = new StringBuilder();//stores attributes
        if(!meta.isEmpty())
        {
            for (String attr : meta.split(";"))
            {
                if (attr.contains("="))
                {
                    var = attr.split("=")[0];
                    if (var.equals(value.split("=")[0]))//look for attribute to be updated/added
                        val = value.split("=")[1];//change that attribute to the new one
                    else val = attr.split("=")[1];//keep the original value for that attribute

                    sb.append(var + "=" + val);
                }
            }
        }else sb.append(value);

        /*if(meta.contains("="))
        {
            if(value.contains("="))
            {
                if (meta.contains(value.split("=")[0]))
                {
                    meta = meta.replaceAll("(" + value.split("=")[0] + "=\\.+)", value);
                } else
                {
                    if (meta.isEmpty())
                        meta = value;
                    else
                        meta = meta + ';' + entry + '=' + value;
                }
            }else
            {
                Log.d(TAG,"updateMetaEntry> Error: Value has no assignment.");
                return false;
            }
        } else
        {
            if (meta.isEmpty())
                meta = entry + '=' + value;
            else
                meta = meta + ';' + entry + '=' + value;
        }*/

        ContentValues values = new ContentValues();
        //values.put(MetadataContract.MetaEntry.COL_META_ENTRY,entry);
        values.put(MetadataContract.MetaEntry.COL_META_ENTRY_DATA,sb.toString());

        String where = MetadataContract.MetaEntry.COL_META_ENTRY+"=?";
        String[] where_args = {entry};
        db.update(MetadataContract.MetaEntry.TABLE_NAME,values,where,where_args);

        closeDB(db);

        Log.d(TAG,"updateMetaRecord> Record["+entry+"] successfully updated to "+value+".");
        return true;
    }

    public static String getMetaRecord(Context context, String entry) throws SQLiteException
    {
        if(context==null)
            return null;
        MetadataHelper dbHelper = new MetadataHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //no need for dbHelper.onCreate(db);
        String query = "SELECT * FROM " + MetadataContract.MetaEntry.TABLE_NAME +
                " WHERE " + MetadataContract.MetaEntry.COL_META_ENTRY + " = ?";

        String meta=null;
        Cursor c = db.rawQuery(query,new String[]{entry});
        if(c!=null)
        {
            if (c.getCount() > 0)
            {
                if (c.moveToFirst())
                //if(c.getCount()>0)
                {
                    meta = c.getString(c.getColumnIndex(MetadataContract.MetaEntry.COL_META_ENTRY_DATA));
                }
            }
            if(!c.isClosed())
                c.close();
        }
        closeDB(db);

        return meta;
    }

    public static boolean addAchievementToDB(Context context, Achievement ach) throws SQLiteException
    {
        if(context==null)
            return false;
        if(ach==null)
            return false;
        if(ach.getAchId()==null)
            return false;
        if(ach.getAchId().isEmpty())
            return false;
        if(ach.getAchId().equals("0"))
            return false;

        Achievement tmp_ach=null;
        try
        {
            tmp_ach = getAchievementFromDB(context, ach.getAchId());
        }catch (SQLiteException e)
        {
            Log.d(TAG,"Achievements table doesn't exist yet: " + e.getMessage());
        }
        if(tmp_ach==null)//if the achievement doesn't exist in DB
        {
            Log.d(TAG, "Inserting new Achievement["+ach.getAchName()+"]");

            AchievementHelper dbHelper = new AchievementHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            dbHelper.onCreate(db);

            ContentValues kv_pairs = new ContentValues();
            kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_ID, ach.getAchId());
            kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_NAME, ach.getAchName());
            kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_DESCRIPTION, ach.getAchDescription());
            kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_DATE, ach.getAchDate());
            kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_TARGET, ach.getAchTarget());
            kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_VALUE, ach.getAchValue());
            kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_NOTIFIED, 0);
            kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_USR_PTS, ach.getUserPoints());
            kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_METHOD, ach.getAchMethod());

            db.insert(AchievementContract.AchievementEntry.TABLE_NAME, null, kv_pairs);

            closeDB(db);

            return true;
        }else return updateAchievementOnDB(context,tmp_ach);
    }

    public static boolean addRewardToDB(Context context, Reward reward) throws SQLiteException
    {
        if(context==null)
            return false;
        if(reward==null)
            return false;
        if(reward.getRwId()==null)
            return false;
        if(reward.getRwId().isEmpty())
            return false;

        Reward tmp_rw=null;
        try
        {
            tmp_rw = getRewardFromDB(context, reward.getRwId());
        }catch (SQLiteException e)
        {
            Log.d(TAG,"Rewards table doesn't exist yet: " + e.getMessage());
        }
        if(tmp_rw==null)//if the Reward doesn't exist in DB
        {
            Log.d(TAG, "Inserting new Reward["+reward.getRwName()+"]");

            RewardHelper dbHelper = new RewardHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            dbHelper.onCreate(db);

            ContentValues kv_pairs = new ContentValues();
            kv_pairs.put(RewardContract.RewardEntry.COL_REWARD_ID, reward.getRwId());
            kv_pairs.put(RewardContract.RewardEntry.COL_REWARD_NAME, reward.getRwName());
            kv_pairs.put(RewardContract.RewardEntry.COL_REWARD_DESCRIPTION, reward.getRwDescription());
            kv_pairs.put(RewardContract.RewardEntry.COL_REWARD_VALUE, reward.getRwCost());
            kv_pairs.put(RewardContract.RewardEntry.COL_REWARD_EVENT_ID, reward.getRwEventID());
            kv_pairs.put(RewardContract.RewardEntry.COL_REWARD_CODE, reward.getRwCode());

            db.insert(RewardContract.RewardEntry.TABLE_NAME, null, kv_pairs);

            closeDB(db);

            return true;
        }else return updateRewardOnDB(context,tmp_rw);
    }

    public static boolean updateRewardOnDB(Context context, Reward reward) throws SQLiteException
    {
        if(context==null)
            return false;
        if(reward==null)
            return false;
        if(reward.getRwId()==null)
            return false;
        if(reward.getRwId().isEmpty())
            return false;

        Log.d(TAG, "Reward["+reward.getRwName()+"] exists on local DB, updating it.");
        AchievementHelper dbHelper = new AchievementHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //dbHelper.onCreate(db);

        ContentValues kv_pairs = new ContentValues();
        kv_pairs.put(RewardContract.RewardEntry.COL_REWARD_ID, reward.getRwId());
        kv_pairs.put(RewardContract.RewardEntry.COL_REWARD_NAME, reward.getRwName());
        kv_pairs.put(RewardContract.RewardEntry.COL_REWARD_DESCRIPTION, reward.getRwDescription());
        kv_pairs.put(RewardContract.RewardEntry.COL_REWARD_VALUE, reward.getRwCost());
        kv_pairs.put(RewardContract.RewardEntry.COL_REWARD_EVENT_ID, reward.getRwEventID());
        kv_pairs.put(RewardContract.RewardEntry.COL_REWARD_CODE, reward.getRwCode());

        String where = RewardContract.RewardEntry.COL_REWARD_ID+"=?";
        String[] where_args = {reward.getRwId()};

        db.update(RewardContract.RewardEntry.TABLE_NAME, kv_pairs, where, where_args);

        closeDB(db);

        return true;
    }

    public static boolean updateAchievementOnDB(Context context, Achievement ach) throws SQLiteException
    {
        if(context==null)
            return false;
        if(ach==null)
            return false;
        if(ach.getAchId()==null)
            return false;
        if(ach.getAchId().isEmpty())
            return false;

        Log.d(TAG, "Achievement["+ach.getAchName()+"] exists on local DB, updating it.");
        AchievementHelper dbHelper = new AchievementHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //dbHelper.onCreate(db);

        ContentValues kv_pairs = new ContentValues();
        kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_ID, ach.getAchId());
        kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_NAME, ach.getAchName());
        kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_DESCRIPTION, ach.getAchDescription());
        kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_DATE, ach.getAchDate());
        kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_TARGET, ach.getAchTarget());
        kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_VALUE, ach.getAchValue());
        kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_NOTIFIED, ach.getNotified());
        kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_USR_PTS, ach.getUserPoints());
        kv_pairs.put(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_METHOD, ach.getAchMethod());

        String where = AchievementContract.AchievementEntry.COL_ACHIEVEMENT_ID+"=?";
        String[] where_args = {ach.getAchId()};
        db.update(AchievementContract.AchievementEntry.TABLE_NAME, kv_pairs, where, where_args);

        closeDB(db);

        return true;
    }

    public static ArrayList<Achievement> getUnnotifiedAchievementsFromDB(Context context) throws SQLiteException
    {
        if (context == null)
            return null;

        ArrayList<Achievement> achievements = new ArrayList<Achievement>();
        AchievementHelper dbHelper = new AchievementHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //no need for dbHelper.onCreate(db);

        String q = "SELECT * FROM " + AchievementContract.AchievementEntry.TABLE_NAME +
                " WHERE " + AchievementContract.AchievementEntry.COL_ACHIEVEMENT_NOTIFIED + "=?";

        String[] where_args = {"0"};
        Cursor c =db.rawQuery(q,where_args);

        if(c!=null)
        {
            if (c.getCount() > 0)
            {
                while (c.moveToNext())
                {

                    String id = c.getString(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_ID));
                    String name = c.getString(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_NAME));
                    String desc = c.getString(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_DESCRIPTION));
                    long date = c.getLong(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_DATE));
                    int target = c.getInt(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_TARGET));
                    int value = c.getInt(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_VALUE));
                    int notifd = c.getInt(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_NOTIFIED));
                    int pts = c.getInt(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_USR_PTS));
                    String meth = c.getString(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_METHOD));

                    Achievement ach = new Achievement(id, name, desc, date, target, value, notifd, pts, meth);
                    achievements.add(ach);
                }
                if (!c.isClosed())
                    c.close();
                closeDB(db);
                return achievements;
            } else
            {
                Log.wtf(TAG, "No unnotified Achievements were found.");
                if (c != null)
                    if (!c.isClosed())
                        c.close();
                closeDB(db);
                return null;
            }
        }else
        {
            Log.wtf(TAG, "No unnotified Achievements were not found.");
            if (c != null)
                if (!c.isClosed())
                    c.close();
            closeDB(db);
            return null;
        }

    }

    public static ArrayList<Achievement> getAllAchievementsFromDB(Context context) throws SQLiteException
    {
        if (context == null)
            return null;

        ArrayList<Achievement> achievements = new ArrayList<Achievement>();
        AchievementHelper dbHelper = new AchievementHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //no need for dbHelper.onCreate(db);

        String q = "SELECT * FROM " + AchievementContract.AchievementEntry.TABLE_NAME;

        Cursor c =db.rawQuery(q,null);

        if(c!=null)
        {
            if (c.getCount() > 0)
            {
                while (c.moveToNext())
                {

                    String id = c.getString(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_ID));
                    String name = c.getString(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_NAME));
                    String desc = c.getString(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_DESCRIPTION));
                    long date = c.getLong(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_DATE));
                    int target = c.getInt(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_TARGET));
                    int value = c.getInt(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_VALUE));
                    int notifd = c.getInt(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_NOTIFIED));
                    int pts = c.getInt(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_USR_PTS));
                    String meth = c.getString(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_METHOD));

                    Achievement ach = new Achievement(id, name, desc, date, target, value, notifd, pts, meth);
                    achievements.add(ach);
                }
                if (!c.isClosed())
                    c.close();
                closeDB(db);
                return achievements;
            } else
            {
                Log.wtf(TAG, "No Achievements were found.");
                if (c != null)
                    if (!c.isClosed())
                        c.close();
                closeDB(db);
                return null;
            }
        }else
        {
            Log.wtf(TAG, "No Achievements were found.");
            if (c != null)
                if (!c.isClosed())
                    c.close();
            closeDB(db);
            return null;
        }
    }

    public static ArrayList<Reward> getAllRewardsFromDB(Context context) throws SQLiteException
    {
        if (context == null)
            return null;

        ArrayList<Reward> rewards = new ArrayList<Reward>();
        RewardHelper dbHelper = new RewardHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //no need for dbHelper.onCreate(db);

        String q = "SELECT * FROM " + RewardContract.RewardEntry.TABLE_NAME;

        Cursor c = db.rawQuery(q,null);

        if(c!=null)
        {
            if (c.getCount() > 0)
            {
                while (c.moveToNext())
                {
                    String id = c.getString(c.getColumnIndex(RewardContract.RewardEntry.COL_REWARD_ID));
                    String name = c.getString(c.getColumnIndex(RewardContract.RewardEntry.COL_REWARD_NAME));
                    String desc = c.getString(c.getColumnIndex(RewardContract.RewardEntry.COL_REWARD_DESCRIPTION));
                    int cost = c.getInt(c.getColumnIndex(RewardContract.RewardEntry.COL_REWARD_VALUE));
                    String ev_id = c.getString(c.getColumnIndex(RewardContract.RewardEntry.COL_REWARD_EVENT_ID));
                    String code = c.getString(c.getColumnIndex(RewardContract.RewardEntry.COL_REWARD_CODE));

                    Reward reward = new Reward(id,name,desc,code,cost,ev_id);
                    rewards.add(reward);
                }
                if (!c.isClosed())
                    c.close();
                closeDB(db);

                return rewards;
            } else
            {
                Log.wtf(TAG, "No Rewards were found.");
                if (c != null)
                    if (!c.isClosed())
                        c.close();
                closeDB(db);

                return null;
            }
        }else
        {
            Log.wtf(TAG, "No Rewards were found.");
            if (c != null)
                if (!c.isClosed())
                    c.close();
            closeDB(db);

            return null;
        }
    }

    public static Achievement getAchievementFromDB(Context context, String ach_id) throws SQLiteException
    {
        if (context == null)
            return null;
        AchievementHelper dbHelper = new AchievementHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //no need for dbHelper.onCreate(db);

        String q = "SELECT * FROM " + AchievementContract.AchievementEntry.TABLE_NAME +
                " WHERE " + AchievementContract.AchievementEntry.COL_ACHIEVEMENT_ID + "=?";
        String[] args = {ach_id};

        Cursor c =db.rawQuery(q,args);

        if(c!=null)
        {
            if (c.getCount() > 0)
            {
                c.moveToFirst();

                String id = c.getString(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_ID));
                String name = c.getString(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_NAME));
                String desc = c.getString(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_DESCRIPTION));
                long date = c.getLong(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_DATE));
                int target = c.getInt(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_TARGET));
                int value = c.getInt(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_VALUE));
                int notifd = c.getInt(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_NOTIFIED));
                int pts = c.getInt(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_USR_PTS));
                String meth = c.getString(c.getColumnIndex(AchievementContract.AchievementEntry.COL_ACHIEVEMENT_METHOD));

                Achievement ach = new Achievement(id, name, desc, date, target, value, notifd, pts, meth);

                if (!c.isClosed())
                    c.close();
                closeDB(db);
                return ach;
            } else
            {
                Log.wtf(TAG, "Achievement[" + ach_id + "] was not found.");
                if (c != null)
                    if (!c.isClosed())
                        c.close();
                closeDB(db);
                return null;
            }
        }else
        {
            Log.wtf(TAG, "Achievement[" + ach_id + "] was not found.");
            if (c != null)
                if (!c.isClosed())
                    c.close();
            closeDB(db);
            return null;
        }
    }

    public static Reward getRewardFromDB(Context context, String rw_id) throws SQLiteException
    {
        if (context == null)
            return null;
        RewardHelper dbHelper = new RewardHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //no need for dbHelper.onCreate(db);

        String q = "SELECT * FROM " + RewardContract.RewardEntry.TABLE_NAME +
                " WHERE " + RewardContract.RewardEntry.COL_REWARD_ID + "=?";
        String[] args = {rw_id};

        Cursor c =db.rawQuery(q,args);

        if(c!=null)
        {
            if (c.getCount() > 0)
            {
                c.moveToFirst();

                String id = c.getString(c.getColumnIndex(RewardContract.RewardEntry.COL_REWARD_ID));
                String name = c.getString(c.getColumnIndex(RewardContract.RewardEntry.COL_REWARD_NAME));
                String desc = c.getString(c.getColumnIndex(RewardContract.RewardEntry.COL_REWARD_DESCRIPTION));
                int cost = c.getInt(c.getColumnIndex(RewardContract.RewardEntry.COL_REWARD_VALUE));
                String ev_id = c.getString(c.getColumnIndex(RewardContract.RewardEntry.COL_REWARD_EVENT_ID));
                String code = c.getString(c.getColumnIndex(RewardContract.RewardEntry.COL_REWARD_CODE));

                Reward reward = new Reward(id,name,desc,code,cost,ev_id);

                if (!c.isClosed())
                    c.close();
                closeDB(db);

                return reward;
            } else
            {
                Log.wtf(TAG, "Reward[" + rw_id + "] was not found.");
                if (c != null)
                    if (!c.isClosed())
                        c.close();
                closeDB(db);
                return null;
            }
        }else
        {
            Log.wtf(TAG, "Reward[" + rw_id + "] was not found.");
            if (c != null)
                if (!c.isClosed())
                    c.close();
            closeDB(db);
            return null;
        }
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

        //Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/assets/sounds/notif.wav");
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        /*
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        notif = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("IceBreak")
                //.setVibrate(new long[]{2000})//new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setLights(Color.CYAN, 3000, 3000)
                .setContentText(msg)
                .setSound(notification)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentIntent(resultPendingIntent)
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
            MessageHelper dbHelper = new MessageHelper(context);//getBaseContext());
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
                updateMessageById(context, m.getId(), m.getMessage());
                Log.d(TAG, "Message already exists in Message table, updated it.");
            }
        }
        catch (SQLiteException e)
        {
            LocalComms.logException(e);
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
            MessageHelper dbHelper = new MessageHelper(ctxt);
            db = dbHelper.getReadableDatabase();
            Cursor c =  db.rawQuery(query, new String[] {id});
            rowCount = c.getCount();
        }
        catch (SQLiteException e)
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
            MessageHelper dbHelper = new MessageHelper(context);//getBaseContext());
            db = dbHelper.getWritableDatabase();
            //Didn't create DB here because when updating there should already be a DB

            String q = "UPDATE " + MessagePollContract.MessageEntry.TABLE_NAME +
                    " SET " + MessagePollContract.MessageEntry.COL_MESSAGE_STATUS + " =? WHERE " +
                    MessagePollContract.MessageEntry.COL_MESSAGE_ID + "=?";
            String[] args = {String.valueOf(status), id};
            db.execSQL(q,args);

            Log.d(TAG, "Successfully updated Message status on local DB");
        }
        catch (SQLiteException e)
        {
            LocalComms.logException(e);
        }
        finally
        {
            closeDB(db);
        }
    }

    public static void updateMessageById(Context context, String id, String msg)
    {
        SQLiteDatabase db = null;
        try
        {
            MessageHelper dbHelper = new MessageHelper(context);//getBaseContext());
            db = dbHelper.getWritableDatabase();
            //Didn't create DB here because when updating there should already be a DB

            String q = "UPDATE " + MessagePollContract.MessageEntry.TABLE_NAME +
                    " SET " + MessagePollContract.MessageEntry.COL_MESSAGE + " =? WHERE " +
                    MessagePollContract.MessageEntry.COL_MESSAGE_ID + "=?";
            String[] args = {msg, id};
            db.execSQL(q,args);

            Log.d(TAG, "Successfully updated Message on local DB");
        }
        catch (SQLiteException e)
        {
            LocalComms.logException(e);
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
            MessageHelper dbHelper = new MessageHelper(context);
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
            if(c!=null)
                if(!c.isClosed())
                    c.close();
        }
        catch (SQLiteException e)
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
        catch (SQLiteException e)
        {
            LocalComms.logException(e);
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
        catch (SQLiteException e)
        {
            LocalComms.logException(e);
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
            if(c!=null)
                if(!c.isClosed())
                    c.close();
        }
        catch (SQLiteException e)
        {
            LocalComms.logException(e);
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

            if(c!=null)
                if(!c.isClosed())
                    c.close();
        }
        catch (SQLiteException e)
        {
            LocalComms.closeDB(db);
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

            if(c!=null)
                if(!c.isClosed())
                    c.close();
        }
        catch (SQLiteException e)
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
            MessageHelper dbHelper = new MessageHelper(context);
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

            if(c!=null)
                if(!c.isClosed())
                    c.close();
        }
        catch (SQLiteException e)
        {
            LocalComms.logException(e);
        }
        finally
        {
            closeDB(db);
            return m;
        }

    }

    public static ArrayList<Message> getOutboundMessages(Context context, String sender)
    {
        SQLiteDatabase db = null;

        ArrayList<Message> messages = new ArrayList<Message>();

        String query = "SELECT * FROM " + MessagePollContract.MessageEntry.TABLE_NAME + " WHERE "
                + MessagePollContract.MessageEntry.COL_MESSAGE_SENDER + " = ? AND NOT "
                + MessagePollContract.MessageEntry.COL_MESSAGE_STATUS + " = ?";

        try
        {
            MessageHelper dbHelper = new MessageHelper(context);
            db = dbHelper.getReadableDatabase();
            dbHelper.onCreate(db);

            Cursor c = db.rawQuery(query, new String[]
                    {
                            sender,
                            String.valueOf(MESSAGE_STATUSES.ICEBREAK_DONE.getStatus())
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

            if(c!=null)
                if(!c.isClosed())
                    c.close();
        }
        catch (SQLiteException e)
        {
            LocalComms.logException(e);
        }
        finally
        {
            closeDB(db);
            return messages;
        }

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
