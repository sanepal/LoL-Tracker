package com.gta0004.lolstalker.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.gta0004.lolstalker.Riot.LastMatch;
import com.gta0004.lolstalker.Riot.SummonerDto;

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

  public void insertNewSummoner(SummonerDto summoner) {
    ContentValues values = new ContentValues();
    if (summoner == null) {
      Log.e("log_tag", "summonerdto  is null");
    }
    values.put(DatabaseHelper.COLUMN_SUMM_ID, summoner.id);
    values.put(DatabaseHelper.COLUMN_SUMM_NAME, summoner.name);
    values.put(DatabaseHelper.COLUMN_SUMM_IC, summoner.profileIconId);
    values.put(DatabaseHelper.COLUMN_SUMM_REV, summoner.revisionDate);
    values.put(DatabaseHelper.COLUMN_SUMM_LEVEL, summoner.summonerLevel);
    values.put(DatabaseHelper.COLUMN_SUMM_LAST, summoner.lastMatch.matchId);

    // Open the database
    open();

    // Insert values into database and get the ID.
    database.insertWithOnConflict(DatabaseHelper.TABLE_TARGETS, null, values, SQLiteDatabase.CONFLICT_IGNORE);

    // Close the database
    close();
  }

  public List<SummonerDto> getAllSummoners() {
    List<SummonerDto> list = new ArrayList<SummonerDto>();

    open();
    /*
     * SELECT app_items.id, app_items.title, app_items.time FROM app_items JOIN
     * app_item_year ON app_item_year.item=app_items.id WHERE app_item_year.year
     * = ?
     */

    SQLiteQueryBuilder query = new SQLiteQueryBuilder();
    query.setTables(DatabaseHelper.TABLE_TARGETS);

    Cursor cursor = query.query(database, new String[] { DatabaseHelper.COLUMN_SUMM_ID,
        DatabaseHelper.COLUMN_SUMM_NAME, DatabaseHelper.COLUMN_SUMM_IC, DatabaseHelper.COLUMN_SUMM_REV,
        DatabaseHelper.COLUMN_SUMM_LEVEL, DatabaseHelper.COLUMN_SUMM_LAST, }, null, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      SummonerDto summoner = new SummonerDto(cursor.getLong(0), cursor.getString(1), cursor.getInt(2),
          cursor.getLong(3), cursor.getInt(4));
      summoner.lastMatch = new LastMatch();
      summoner.lastMatch.matchId = cursor.getLong(5);
      list.add(summoner);
      cursor.moveToNext();
    }
    cursor.close();

    close(); // should be in activity's onDelete instead?

    return list;
  }
}
