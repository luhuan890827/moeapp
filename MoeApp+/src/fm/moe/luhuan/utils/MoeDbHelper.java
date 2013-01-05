package fm.moe.luhuan.utils;

import java.util.Date;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MoeDbHelper extends SQLiteOpenHelper {

	public static final String TABLE_NAME = "local_songs_info";

	public MoeDbHelper(Context context) {
		super(context, "moe_fm_db", null, 1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("create table "+" if not exists "
				+ TABLE_NAME 
				+ " (_id INTEGER  not null primary key"//0
				+ ",title text not null"//1
				+ ",artist text" //2
				+ ",parent_id integer not null"//3
				+ ",parent_title text not null"//4
				+ ",is_fav boolean"//5
				+ ",media_path text not null"//6
				+ ",cover_path text not null"//7
				+ ",insert_time int not null"//8
				+");");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	

}
