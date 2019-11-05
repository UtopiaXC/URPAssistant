package com.utopiaxc.urpassistant.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLHelperTimeTable extends SQLiteOpenHelper {

    private static Integer Version = 1;


    public SQLHelperTimeTable(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public SQLHelperTimeTable(Context context,String name,int version) {
        this(context,name,null,version);
    }

    public SQLHelperTimeTable(Context context,String name) {
        this(context, name, Version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table "
                + "user"
                + " (ClassId text primary key, " +
                "ClassName text, " +
                "Credit text," +
                "ClassAttribute text," +
                "ExamAttribute text," +
                "Teacher text," +
                "Way text," +
                "Week text," +
                "Data text," +
                "Time text," +
                "Count text," +
                "School text," +
                "Building text," +
                "Room text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + "user";
        db.execSQL(sql);
        onCreate(db);
    }
}
