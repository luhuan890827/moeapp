package fm.moe.luhuan.utils;

import java.lang.Thread.UncaughtExceptionHandler;

import fm.moe.luhuan.MusicBrowse;
import fm.moe.luhuan.service.PlayBackService;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;

public class MyUncaughtExceptionHandler implements UncaughtExceptionHandler{
	private Context ctx;
	private static MyUncaughtExceptionHandler instance = new MyUncaughtExceptionHandler();
	private UncaughtExceptionHandler defaultHandler;
	private  MyUncaughtExceptionHandler(){
	}
	public static MyUncaughtExceptionHandler getInstance (){
		return instance; 
	}
	public void bind(Context c){
		ctx = c;
		defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e("", "",ex);
		
		
//		ctx.stopService(new Intent(ctx, PlayBackService.class));
//		Intent i = new Intent(ctx, MusicBrowse.class) ;
//		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		ctx.startActivity(i);
//		Looper.loop();
		defaultHandler.uncaughtException(thread, ex);
	}

}
