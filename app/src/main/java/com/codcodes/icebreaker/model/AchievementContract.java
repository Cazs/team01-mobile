package com.codcodes.icebreaker.model;

import android.provider.BaseColumns;

/**
 * Created by Casper on 2016/10/18.
 */
public class AchievementContract
{
        public static final String TYPE_TEXT = "TEXT";
        public static final String TYPE_INT = "INTEGER";

        public static final String SQL_CREATE_tblAchievements = "CREATE TABLE IF NOT EXISTS " +
                AchievementEntry.TABLE_NAME + " (" +
                AchievementEntry.COL_ACHIEVEMENT_ID + " " + TYPE_INT+ " PRIMARY KEY," +
                AchievementEntry.COL_ACHIEVEMENT_NAME + " " + TYPE_TEXT + " NOT NULL," +
                AchievementEntry.COL_ACHIEVEMENT_DESCRIPTION + " " + TYPE_TEXT + " NULL," +
                AchievementEntry.COL_ACHIEVEMENT_VALUE + " " + TYPE_INT + " NOT NULL," +
                AchievementEntry.COL_ACHIEVEMENT_TARGET + " " + TYPE_INT + " NOT NULL," +
                AchievementEntry.COL_ACHIEVEMENT_DATE + " " + TYPE_INT + " NOT NULL," +
                AchievementEntry.COL_ACHIEVEMENT_NOTIFIED + " " + TYPE_INT + " NOT NULL," +
                AchievementEntry.COL_ACHIEVEMENT_USR_PTS + " " + TYPE_INT + " NOT NULL," +
                AchievementEntry.COL_ACHIEVEMENT_METHOD + " " + TYPE_TEXT + " NOT NULL" +
                ")";

        public static final String SQL_DELETE_tblAchievements = "DROP TABLE IF EXISTS " + AchievementEntry.TABLE_NAME;

        public AchievementContract(){}

        public static abstract class AchievementEntry implements BaseColumns
        {
            public static final String TABLE_NAME = "Achievements";
            public static final String COL_ACHIEVEMENT_ID = "Achievement_id";
            public static final String COL_ACHIEVEMENT_NAME = "Achievement_name";
            public static final String COL_ACHIEVEMENT_DESCRIPTION = "Achievement_description";
            public static final String COL_ACHIEVEMENT_VALUE = "Achievement_value";
            public static final String COL_ACHIEVEMENT_TARGET = "Achievement_target";
            public static final String COL_ACHIEVEMENT_NOTIFIED = "Achievement_notified";
            public static final String COL_ACHIEVEMENT_DATE = "Achievement_date";
            public static final String COL_ACHIEVEMENT_USR_PTS = "Achievement_usr_pts";
            public static final String COL_ACHIEVEMENT_METHOD = "Achievement_method";

            public static final String COL_ACHIEVEMENT_NULLABLE = "nulls";
        }
}
