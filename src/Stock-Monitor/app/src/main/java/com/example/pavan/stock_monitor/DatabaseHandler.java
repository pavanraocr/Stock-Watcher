package com.example.pavan.stock_monitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by pavan on 3/19/17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHandler";

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    // DB Name
    private static final String DATABASE_NAME = "StockAppDB";
    // DB Table Name
    private static final String TABLE_NAME = "StockWatchTable";
    ///DB Columns
    private static final String SYMBOL = "StockSymbol";
    private static final String COMPANY = "CompanyName";
    // DB Table Create Code
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    SYMBOL + " TEXT not null unique," +
                    COMPANY + " TEXT not null)";

    private SQLiteDatabase database;

    private static DatabaseHandler instance;

    public static DatabaseHandler getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseHandler(context);
        return instance;
    }

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
        Log.d(TAG, "DatabaseHandler: C'tor DONE");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // onCreate is only called is the DB does not exist
        Log.d(TAG, "onCreate: Making New DB");
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void setupDb() {
        database = getWritableDatabase();
    }

    public ArrayList<Stocks> loadStocks() {

        Log.d(TAG, "loadStocks: LOADING Symbol-Company DATA FROM DB");
        ArrayList<Stocks> stockslst = new ArrayList<>();

        Cursor cursor = database.query(
                TABLE_NAME,  // The table to query
                new String[]{SYMBOL, COMPANY}, // The columns to return
                null, // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                null); // The sort order
        if (cursor != null) {
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                Stocks s = new Stocks();
                s.setCompanySym(cursor.getString(0));
                s.setCompanyName(cursor.getString(1));
                stockslst.add(s);

                cursor.moveToNext();
            }
            cursor.close();
        }
        Log.d(TAG, "LoadStocks: DONE LOADING Stocks DATA FROM DB");

        return stockslst;
    }

    public void addStocks(Stocks s) {
        ContentValues values = new ContentValues();
        values.put(SYMBOL, s.getCompanySym());
        values.put(COMPANY, s.getCompanyName());

        deleteStocks(s.getCompanySym());
        long key = database.insert(TABLE_NAME, null, values);
        Log.d(TAG, "addStocks: " + key);
    }

    public void updateStocks(Stocks s) {
        ContentValues values = new ContentValues();
        values.put(SYMBOL, s.getCompanySym());
        values.put(COMPANY, s.getCompanyName());

        long key = database.update(
                TABLE_NAME, values, SYMBOL + " = ?", new String[]{s.getCompanySym()});

        Log.d(TAG, "updateStock: " + key);
    }

    public void deleteStocks(String name) {
        Log.d(TAG, "deleteStock: " + name);
        int cnt = database.delete(TABLE_NAME, SYMBOL + " = ?", new String[]{name});
        Log.d(TAG, "deleteStock: " + cnt);
    }

    public void dumpLog() {
        Cursor cursor = database.rawQuery("select * from " + TABLE_NAME, null);
        if (cursor != null) {
            cursor.moveToFirst();

            Log.d(TAG, "dumpLog: vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
            for (int i = 0; i < cursor.getCount(); i++) {
                String symbol = cursor.getString(0);
                String company = cursor.getString(1);
                Log.d(TAG, "dumpLog: " +
                        String.format("%s %-18s", SYMBOL + ":", symbol) +
                        String.format("%s %-18s", COMPANY + ":", company));
                cursor.moveToNext();
            }
            cursor.close();
        }

        Log.d(TAG, "dumpLog: ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    }

    public void shutDown() {
        database.close();
    }
}
