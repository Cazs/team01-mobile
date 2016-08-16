package com.codcodes.icebreaker.model;

import android.provider.BaseColumns;

/**
 * Created by Casper on 2016/08/15.
 */
public final class ContactsContract
{
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_INT = "INTEGER";
    public static final String SQL_CREATE_tblContacts = "CREATE TABLE IF NOT EXISTS " +
            ContactEntry.TABLE_NAME +
            " (" +
                ContactEntry.COL_CONTACT_ID + " " + TYPE_INT + " PRIMARY KEY," +
                ContactEntry.COL_CONTACT + " " + TYPE_TEXT + " NOT NULL," +
            ")";
    public static final String SQL_DELETE_tblContacts = "DROP TABLE IF EXISTS " + ContactEntry.TABLE_NAME;

    public ContactsContract(){};

    public static abstract class ContactEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "Contacts";
        public static final String COL_CONTACT_ID = "Contact_id";
        public static final String COL_CONTACT = "Contact";
        public static final String  COL_CONTACT_NULLABLE = "Nulls";
    }
}
