package com.gdht.itasset.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GDHTOpenHelper extends SQLiteOpenHelper {

	
	
	public GDHTOpenHelper(Context context) {
		super(context, "gdht.db", null, 1);
	}
	
	public GDHTOpenHelper(Context context, int versionId) {
		super(context, "gdht.db", null, versionId);
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table if not exists scan_check_rfids("
				+ "idx integer primary key autoincrement, rfid text, user text"
				+ ")");
		db.execSQL("create table if not exists local_stock("
				+ "idx integer primary key autoincrement,assetChecklistId text, assetCheckplanId text, assetInfoId text,"
				+ "classify text, type text, rfidnumber text, barnumber text, qrnumber text,"
				+ "brand text, model text, usetype text, checkstate text, dept text, detil text,"
				+ "id text, keeper text, name text, office text, warehouseArea text, goodsShelves text,"
				+ "registerdate text, registrant text"
				+ ")");
		db.execSQL("create table if not exists local_plan("
				+ "idx integer primary key autoincrement, id text, title text,type text, depts text, number text, planstate text, detail text, deptcode text, qdtime text, wctime text, persons text"
				+ ")");
		db.execSQL("create table if not exists local_planresult("
				+ "idx integer primary key autoincrement, id text, title text,depts text, persons text, number text, detail text, planstate text, qdtime text, wctime text, wcr text, yp text,"
				+ "wp text, pk text, py text, deptcode text, type text, yprfids text, wprfids text, pkrfids text, pyrfids text"
				+ ")");
		db.execSQL("create table if not exists loginLog("
				+ "idx integer primary key autoincrement, login_name text, login_time text"
				+ ")");
		
		db.execSQL("create table if not exists realnames("
				+ "idx integer primary key autoincrement, username text, realname text"
				+ ")");
		db.execSQL("create table if not exists local_pandian("
				+ "idx integer primary key autoincrement, username text, planid text, rfid text,assetid text, type text"
				+ ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		db.execSQL("ALTER TABLE dbversion ADD COLUMN marktes integer");
	}

}








