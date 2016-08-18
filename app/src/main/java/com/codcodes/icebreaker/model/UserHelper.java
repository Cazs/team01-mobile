package com.codcodes.icebreaker.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.codcodes.icebreaker.services.MessagePollService;

/**
 * Created by Casper on 2016/08/10.
 */
public class UserHelper extends SQLiteOpenHelper
{
    public static final String DB_NAME = "IcebreakDB";
    public static final int DB_VERSION = 1;

    public UserHelper(Context ctxt)
    {
        super(ctxt,DB_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(UserContract.SQL_CREATE_tblMSG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
