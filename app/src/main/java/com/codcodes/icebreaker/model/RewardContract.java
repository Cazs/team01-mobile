package com.codcodes.icebreaker.model;

import android.provider.BaseColumns;

/**
 * Created by Casper on 2016/10/19.
 */
public class RewardContract
{
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_INT = "INTEGER";
    public static final String SQL_CREATE_tblRewards = "CREATE TABLE IF NOT EXISTS " +
            RewardEntry.TABLE_NAME + " (" +
            RewardEntry.COL_REWARD_ID + " " + TYPE_INT+ " PRIMARY KEY," +
            RewardEntry.COL_REWARD_NAME + " " + TYPE_TEXT + " NOT NULL," +
            RewardEntry.COL_REWARD_DESCRIPTION + " " + TYPE_TEXT + " NULL," +
            RewardEntry.COL_REWARD_VALUE + " " + TYPE_INT + " NOT NULL," +
            RewardEntry.COL_REWARD_CODE + " " + TYPE_INT + " NOT NULL," +
            RewardEntry.COL_REWARD_EVENT_ID + " " + TYPE_INT + " NOT NULL" +
            ")";

    public static final String SQL_DELETE_tblRewards = "DROP TABLE IF EXISTS " + RewardEntry.TABLE_NAME;

    public RewardContract(){}

    public static abstract class RewardEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "Reward";
        public static final String COL_REWARD_ID = "Reward_id";
        public static final String COL_REWARD_NAME = "Reward_name";
        public static final String COL_REWARD_DESCRIPTION = "Reward_description";
        public static final String COL_REWARD_VALUE = "Reward_value";
        public static final String COL_REWARD_CODE = "Reward_code";
        public static final String COL_REWARD_EVENT_ID = "event_id"; //Event that Reward is applicable to
        //public static final String COL_REWARD_DATE = "Reward_date";

        public static final String COL_REWARDS_NULLABLE = "nulls";
    }
}
