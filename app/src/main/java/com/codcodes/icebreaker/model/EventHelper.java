package com.codcodes.icebreaker.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Casper on 2016/09/22.
 */
public class EventHelper extends SQLiteOpenHelper
{
    public static final String DB_NAME = "IcebreakDB";
    public static final int DB_VERSION = 1;

    public EventHelper(Context ctxt)
    {
        super(ctxt,DB_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(EventContract.SQL_CREATE_tblEvents);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
    }
}
