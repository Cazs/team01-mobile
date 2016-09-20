package com.codcodes.icebreaker.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Casper on 2016/09/16.
 */
public class MetadataHelper extends SQLiteOpenHelper
{
    public static final String DB_NAME = "IcebreakDB";
    public static final int DB_VERSION = 1;

    public MetadataHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        sqLiteDatabase.execSQL(MetadataContract.SQL_CREATE_tblMeta);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {

    }
}
