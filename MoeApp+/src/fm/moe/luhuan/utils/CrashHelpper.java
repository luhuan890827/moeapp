package fm.moe.luhuan.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;

public class CrashHelpper implements UncaughtExceptionHandler {
	private static CrashHelpper instance = new CrashHelpper();
	private Context c;

	private CrashHelpper() {
	}

	public static CrashHelpper getInstance() {

		return instance;
	}

	public void register(Context ctx) {
		c = ctx;
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	public void uncaughtException(Thread arg0, final Throwable arg1) {

		new Thread() {
			public void run() {
				String message = "";
				message += arg1.getMessage()+","+arg1.getCause() + "\n";
				
				StackTraceElement[] traces = arg1.getStackTrace();
				for (int i = 0; i < traces.length; i++) {
					message += traces[i].getClassName() + " at line "
							+ traces[i].getLineNumber();
					message += "\n";
				}
				SimpleDateFormat format = new SimpleDateFormat("MM-dd-kk:mm:ss");

				File logDir = new File(c.getExternalFilesDir(null) + "/errLog");
				if (!logDir.exists()) {
					logDir.mkdirs();
				}
				try {
					FileWriter fw = new FileWriter(new File(logDir,
							format.format(new Date(System.currentTimeMillis()))
									+ ".log.txt"));
					fw.write(message);
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			};
		}.start();
	}

}
