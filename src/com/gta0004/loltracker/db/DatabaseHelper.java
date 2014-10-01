package com.gta0004.loltracker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "tracker.db";
  private static final int DATABASE_VERSION = 5;

  private static final String COLUMN_ID = "id";
  public static final String COLUMN_SUMM_ID = "summoner_id";
  public static final String COLUMN_SUMM_NAME = "summoner_name";
  public static final String COLUMN_SUMM_REGION = "summoner_region";
  public static final String COLUMN_SUMM_LEVEL = "summoner_level";
  public static final String COLUMN_SUMM_IC = "summoner_icon";

  public static final String TABLE_TARGETS = "targets";

  private static final String CREATE_TABLE_TARGETS = "CREATE TABLE " + TABLE_TARGETS + " ( " + COLUMN_ID
      + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + COLUMN_SUMM_ID + " INTEGER, " + COLUMN_SUMM_NAME
      + " TEXT, " + COLUMN_SUMM_IC + " INTEGER, " + COLUMN_SUMM_REGION + " TEXT, " + COLUMN_SUMM_LEVEL
      + " INTEGER) ";

  private static final String DELETE_TABLE_TARGETS = "DROP TABLE IF EXISTS " + TABLE_TARGETS;

  private Context mContext;

  public DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    mContext = context;
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(CREATE_TABLE_TARGETS);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL(DELETE_TABLE_TARGETS);
    onCreate(db);
  }

}
