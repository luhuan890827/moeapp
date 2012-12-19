package fm.moe.luhuan;

import java.io.File;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Environment;

public class FileStorageHelper {
	private File songDir;
	private File imageDir;
	private Context ctx;
	private DownloadManager downloadManager;

	public FileStorageHelper(Context c) {
		ctx = c;
		songDir = c.getExternalFilesDir("music");
		imageDir = c.getExternalFilesDir("image");
		if (!songDir.exists()) {
			songDir.mkdir();
		}
		if (!imageDir.exists()) {
			imageDir.mkdir();
		}
		downloadManager = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);

	}
	public void downloadFile(String url){
		
	}
}
