package com.codcodes.icebreaker.model;

import android.provider.BaseColumns;

/**
 * Created by Casper on 2016/08/10.
 */
public final class UserContract
{
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_INT = "INTEGER";
    public static final String SQL_CREATE_tblMSG = "CREATE TABLE IF NOT EXISTS " +
            UserEntry.TABLE_NAME + " (" +
            UserEntry.COL_USER_USERNAME + " " + TYPE_TEXT+ " PRIMARY KEY," +
            UserEntry.COL_USER_FNAME + " " + TYPE_TEXT + " NULL," +
            UserEntry.COL_USER_LNAME + " " + TYPE_TEXT + " NULL," +
            UserEntry.COL_USER_AGE + " " + TYPE_INT + "," +
            UserEntry.COL_USER_BIO + " " + TYPE_TEXT + " NOT NULL," +
            UserEntry.COL_USER_CATCHPHRASE + " " + TYPE_TEXT + " NOT NULL," +
            UserEntry.COL_USER_OCCUPATION + " " + TYPE_TEXT + " NOT NULL," +
            UserEntry.COL_USER_GENDER + " " + TYPE_TEXT + " NOT NULL"
            + ")";
    public static final String SQL_DELETE_tblMSG = "DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME;

    public UserContract(){};

    public static abstract class UserEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "Contacts";
        public static final String COL_USER_USERNAME = "username";
        public static final String COL_USER_FNAME = "fname";
        public static final String COL_USER_LNAME = "lname";
        public static final String COL_USER_AGE = "Age";
        public static final String COL_USER_BIO = "Bio";
        public static final String COL_USER_CATCHPHRASE = "Catchphrase";
        public static final String COL_USER_OCCUPATION = "Occupation";
        public static final String COL_USER_GENDER = "Gender";

        public static final String  COL_MESSAGE_NULLABLE = "Nulls";
    }
}
