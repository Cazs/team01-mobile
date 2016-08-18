package com.codcodes.icebreaker.model;

import android.provider.BaseColumns;

/**
 * Created by Casper on 2016/08/10.
 */
public final class MessagePollContract
{
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_INT = "INTEGER";
    public static final String SQL_CREATE_tblMSG = "CREATE TABLE IF NOT EXISTS " +
            MessageEntry.TABLE_NAME + " (" +
            MessageEntry.COL_MESSAGE_ID + " " + TYPE_TEXT + " PRIMARY KEY," +
            MessageEntry.COL_MESSAGE_SENDER + " " + TYPE_TEXT + " NOT NULL," +
            MessageEntry.COL_MESSAGE_RECEIVER + " " + TYPE_TEXT + " NOT NULL," +
            MessageEntry.COL_MESSAGE_STATUS + " " + TYPE_INT + "," +
            MessageEntry.COL_MESSAGE_TIME + " " + TYPE_TEXT + " NOT NULL," +
            MessageEntry.COL_MESSAGE + " " + TYPE_TEXT + " NOT NULL"
            + ")";
    public static final String SQL_DELETE_tblMSG = "DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME;

    public MessagePollContract(){};

    public static abstract class MessageEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "Message";
        public static final String COL_MESSAGE_ID = "Message_id";
        public static final String COL_MESSAGE_SENDER = "Message_sender";
        public static final String COL_MESSAGE_RECEIVER = "Message_receiver";
        public static final String COL_MESSAGE_STATUS = "Message_status";
        public static final String COL_MESSAGE_TIME = "Message_time";
        public static final String COL_MESSAGE = "Msg";
        public static final String  COL_MESSAGE_NULLABLE = "Nulls";
    }
}
