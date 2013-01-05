package fm.moe.luhuan.utils;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import fm.moe.luhuan.beans.data.SimpleData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class DataStorageHelper {
	private File songDir;
	private File coverDir;
	private MoeDbHelper dbHelper;
	private Context ctx;
	public DataStorageHelper(Context c) {
		songDir = new File(c.getExternalFilesDir(null), "song");
		if (!songDir.exists()) {
			songDir.mkdir();
		}
		coverDir = new File(c.getExternalFilesDir(null), "cover");
		if (!coverDir.exists()) {
			coverDir.mkdir();
		}
		ctx = c;
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
		db.close();
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
	public String getTempImageUri(Bitmap bm) throws IOException{
		
		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		bm.compress(CompressFormat.JPEG, 100, bas);
		File tempFile = new File(ctx.getExternalCacheDir(), "tempImage.jpg");
		OutputStream os = new FileOutputStream(tempFile);
		os.write(bas.toByteArray());
		os.close();
		
		return tempFile.getAbsolutePath();
	}
	public void updateFav (SimpleData item){
		ContentValues cv = new ContentValues();
		cv.put("is_fav", item.isFav());
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.update(MoeDbHelper.TABLE_NAME, cv, "_id=?", new String[]{item.getId()+""});
		db.close();
	}
	public void persistState(List<SimpleData> list,int n) throws IOException{
		File status = new File(ctx.getCacheDir(),".playstatus");
		JSONObject jo = new JSONObject();
		
		jo.put("index", n);
		jo.put("list", list);
		
		FileOutputStream fos = new FileOutputStream(status);
		fos.write(jo.toJSONString().getBytes());
		fos.close();
	}
	public Object[] getPersistedState() throws IOException{
		File status = new File(ctx.getCacheDir(),".playstatus");
		byte[] data = new byte[(int) status.length()];
		FileInputStream fis = new FileInputStream(status);
		fis.read(data);
		String json = new String(data);
		fis.close();
		JSONObject jo = JSON.parseObject(json);
		
		JSONArray aList = jo.getJSONArray("list");
		
		ArrayList<SimpleData> list = new ArrayList<SimpleData>();
		
		for(int i = 0;i<aList.size();i++){
			SimpleData item = JSON.parseObject(aList.getString(i), SimpleData.class);
			list.add(item);
		}
		
		return new Object[]{jo.getInteger("index"),list};
	}
}
