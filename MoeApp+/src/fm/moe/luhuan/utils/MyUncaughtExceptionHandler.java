package fm.moe.luhuan.utils;

import java.lang.Thread.UncaughtExceptionHandler;

import fm.moe.luhuan.MusicBrowse;
import fm.moe.luhuan.service.PlayBackService;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyUncaughtExceptionHandler implements UncaughtExceptionHandler{
	private Context ctx;
	public  MyUncaughtExceptionHandler(Context c){
		ctx = c;
		Thread.setDefaultUncaughtExceptionHandler(this);
	}
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e("", "",ex);
		ctx.stopService(new Intent(ctx, PlayBackService.class));
		ctx.startActivity(new Intent(ctx, MusicBrowse.class));
	}

}
