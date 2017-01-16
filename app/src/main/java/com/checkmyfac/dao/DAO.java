package com.checkmyfac.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DAO extends SQLiteOpenHelper {

    protected SQLiteDatabase mybase;

    public DAO(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    protected synchronized void openDb() {
        mybase = getWritableDatabase();
    }

    protected synchronized void closeDb() {
        mybase.close();
    }




}
