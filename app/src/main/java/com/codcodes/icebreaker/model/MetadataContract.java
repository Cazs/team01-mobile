package com.codcodes.icebreaker.model;

import android.provider.BaseColumns;

/**
 * Created by Casper on 2016/09/16.
 */
public class MetadataContract
{
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_INT = "INTEGER";
    public static final String SQL_CREATE_tblMeta = "CREATE TABLE IF NOT EXISTS " +
            MetaEntry.TABLE_NAME + " (" +
            MetaEntry.COL_META_ENTRY + " " + TYPE_TEXT+ " PRIMARY KEY," +
            MetaEntry.COL_META_ENTRY_DATA + " " + TYPE_TEXT + " NULL)";
    public static final String SQL_DELETE_tblMeta = "DROP TABLE IF EXISTS " + MetaEntry.TABLE_NAME;

    public MetadataContract(){};

    public static abstract class MetaEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "Meta";
        public static final String COL_META_ENTRY = "entry";
        public static final String COL_META_ENTRY_DATA = "entry_data";
        public static final String COL_MESSAGE_NULLABLE = "nulls";
    }
}
