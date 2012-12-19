package fm.moe.luhuan;

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
				+ " (_id INTEGER  not null primary key"
				+ ",title text not null"
				+ ",artist text,media_path text not null"
				+ ",cover_path text not null"
				+ ",insert_time int not null"
				+");");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
