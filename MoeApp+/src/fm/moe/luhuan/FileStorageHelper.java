package fm.moe.luhuan;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import fm.moe.luhuan.beans.data.SimpleData;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.util.Log;

public class FileStorageHelper {
	private File songDir;
	private File coverDir;
	private MoeDbHelper dbHelper;
	public FileStorageHelper(Context c) {
		songDir = new File(c.getExternalFilesDir(null), "song");
		if (!songDir.exists()) {
			songDir.mkdir();
		}
		coverDir = new File(c.getExternalFilesDir(null), "cover");
		if (!coverDir.exists()) {
			coverDir.mkdir();
		}
		dbHelper = new MoeDbHelper(c);
	}
	public Bitmap getItemCoverBitmap(SimpleData item){
		Bitmap bm = null;
		File f = new File(coverDir,item.getParentId()+".jpg");
		if(f.exists()){
			bm = BitmapFactory.decodeFile(f.getAbsolutePath());
		}
		return bm;
	}
	public String getItemMp3Url(SimpleData item){
		String url=null;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c = db.rawQuery("select media_path from "+MoeDbHelper.TABLE_NAME+" where _id="+item.getId(), null);
		if(c.getCount()>0){
			c.moveToFirst();
			url = c.getString(0);
		}else{
			url = item.getMp3Url();
		}
		return url;
	}
	public boolean isItemSaved(SimpleData item){
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c = db.rawQuery("select media_path from "+MoeDbHelper.TABLE_NAME+" where _id="+item.getId(), null);
		return c.getCount()>0;
	}
	public void insertItemIntoDb(SimpleData item){
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put("_id", item.getId());
		values.put("title", item.getTitle());
		values.put("artist", item.getArtist());
		values.put("media_path", songDir.getAbsolutePath()+"/"+item.getId()+".mp3");
		values.put("cover_path", coverDir.getAbsolutePath()+"/"+item.getParentId()+".jpg");
		values.put("insert_time", (new Date()).getTime());
		values.put("parent_id", item.getParentId());
		values.put("parent_title", item.getParentTitle());
		values.put("is_fav", item.isFav());
		db.insert(MoeDbHelper.TABLE_NAME, null, values);
		db.close();
	}
	public void saveCover(SimpleData item,Bitmap bm){
		File cover = new File(coverDir,item.getParentId()+".jpg");
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(cover);
			bm.compress(CompressFormat.JPEG, 100, os);
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	public int getSongFileLength(SimpleData item){
		File song = new File(songDir, item.getId()+".mp3");
		return (int) song.length();
	}
	public void writeDataToSong(SimpleData item,byte[] data, int length) {
		File song = new File(songDir, item.getId()+".mp3");
		try{
			OutputStream os = new FileOutputStream(song,true);
			os.write(data);
			os.close();
		}catch(Exception e){
			Log.e("FileStorageHelper", "write data to song err");
		}
		
	}
}
