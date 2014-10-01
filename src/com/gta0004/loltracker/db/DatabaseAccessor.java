package com.gta0004.loltracker.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.gta0004.loltracker.riot.Summoner;

public class DatabaseAccessor {
  private SQLiteDatabase database;
  private DatabaseHelper dbHelper;

  public DatabaseAccessor(Context context) {
    dbHelper = new DatabaseHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  public void insertNewSummoner(Summoner summoner) {
    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.COLUMN_SUMM_ID, summoner.id);
    values.put(DatabaseHelper.COLUMN_SUMM_NAME, summoner.name);
    values.put(DatabaseHelper.COLUMN_SUMM_IC, summoner.profileIconId);
    //values.put(DatabaseHelper.COLUMN_SUMM_REV, summoner.revisionDate);
    values.put(DatabaseHelper.COLUMN_SUMM_LEVEL, summoner.summonerLevel);
    //values.put(DatabaseHelper.COLUMN_SUMM_LAST, summoner.lastMatch.matchId);
    values.put(DatabaseHelper.COLUMN_SUMM_REGION, summoner.region);

    // Open the database
    open();

    // Insert values into database and get the ID.
    database.insertWithOnConflict(DatabaseHelper.TABLE_TARGETS, null, values, SQLiteDatabase.CONFLICT_IGNORE);

    // Close the database
    close();
  }

  public ArrayList<Summoner> getAllSummoners() {
    ArrayList<Summoner> list = new ArrayList<Summoner>();

    open();

    SQLiteQueryBuilder query = new SQLiteQueryBuilder();
    query.setTables(DatabaseHelper.TABLE_TARGETS);

    Cursor cursor = query.query(database, new String[] { DatabaseHelper.COLUMN_SUMM_ID,
        DatabaseHelper.COLUMN_SUMM_NAME, DatabaseHelper.COLUMN_SUMM_IC, DatabaseHelper.COLUMN_SUMM_REGION,
        DatabaseHelper.COLUMN_SUMM_LEVEL}, null, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Summoner summoner = new Summoner();
      summoner.id = cursor.getLong(0);
      summoner.name = cursor.getString(1);
      summoner.profileIconId = cursor.getInt(2);
      summoner.region = cursor.getString(3);
      summoner.summonerLevel = cursor.getInt(4);
      list.add(summoner);
      cursor.moveToNext();
    }
    cursor.close();

    close(); // should be in activity's onDelete instead?

    return list;
  }
}
