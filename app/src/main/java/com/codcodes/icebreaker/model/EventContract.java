package com.codcodes.icebreaker.model;

import android.provider.BaseColumns;

/**
 * Created by Casper on 2016/09/22.
 */
public class EventContract
{
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_INT = "INTEGER";
    public static final String SQL_CREATE_tblEvents = "CREATE TABLE IF NOT EXISTS " +
            EventEntry.TABLE_NAME + " (" +
            EventEntry.COL_EVENT_ID + " " + TYPE_INT+ " PRIMARY KEY," +
            EventEntry.COL_EVENT_TITLE + " " + TYPE_TEXT + " NULL," +
            EventEntry.COL_EVENT_DESCRIPTION + " " + TYPE_TEXT + " NULL," +
            EventEntry.COL_EVENT_ADDRESS + " " + TYPE_TEXT + " NULL," +
            EventEntry.COL_EVENT_LOCATION + " " + TYPE_TEXT + " NULL," +
            EventEntry.COL_EVENT_DATE + " " + TYPE_INT + " NULL," +
            EventEntry.COL_EVENT_MEETING_PLACES + " " + TYPE_TEXT + " NULL," +
            EventEntry.COL_EVENT_END_DATE + " " + TYPE_INT + " NULL," +
            EventEntry.COL_EVENT_ACCESS_CODE + " " + TYPE_INT + " NULL" +
            ")";
    public static final String SQL_DELETE_tblMeta = "DROP TABLE IF EXISTS " + EventEntry.TABLE_NAME;

    public EventContract(){}

    public static abstract class EventEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "Events";
        public static final String COL_EVENT_ID = "event_id";
        public static final String COL_EVENT_TITLE = "event_title";
        public static final String COL_EVENT_DESCRIPTION = "event_description";
        public static final String COL_EVENT_ADDRESS = "event_address";
        public static final String COL_EVENT_LOCATION = "event_gps_location";
        public static final String COL_EVENT_DATE = "date";
        public static final String COL_EVENT_MEETING_PLACES = "event_meeting_places";
        public static final String COL_EVENT_END_DATE = "event_end_date";
        public static final String COL_EVENT_ACCESS_CODE = "event_access_code";

        public static final String COL_MESSAGE_NULLABLE = "nulls";
    }
}
