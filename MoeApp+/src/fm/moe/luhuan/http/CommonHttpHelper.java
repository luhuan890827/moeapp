package fm.moe.luhuan.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class CommonHttpHelper {
	private HttpClient client;

	public CommonHttpHelper() {
		BasicHttpParams params = new BasicHttpParams();
		HttpConnectionParams.setSoTimeout(params, 3000);
		HttpConnectionParams.setConnectionTimeout(params, 2000);
		client = new DefaultHttpClient(params);
	}

	public Bitmap getBitmap(String url) {
		HttpGet request = new HttpGet(url);
		Bitmap bm = null;
		try {
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			InputStream inStream = entity.getContent();
			ArrayList<Integer> data = new ArrayList<Integer>();
			int b = 0;
			while ((b = inStream.read()) > -1) {
				data.add(b);
			}
			byte[] dataByte = new byte[data.size()];
			for (int i = 0; i < data.size(); i++) {
				dataByte[i] = (byte) data.get(i).intValue();
			}
			bm = BitmapFactory.decodeByteArray(dataByte, 0, dataByte.length);
			inStream.close();
		} catch (Exception e) {
			Log.e("CommonHttpHelper", "getBitmap");
		}

		return bm;
	}

	public InputStream downloadRanged(String url, int start, int end) {
		InputStream is = null;
		HttpGet get = new HttpGet(url);
		get.setHeader("range", "bytes=" + start + "-" + end);
		HttpResponse resp = null;

		try {
			resp = client.execute(get);
			is = resp.getEntity().getContent();
		} catch (IOException e) {
			//Log.e("commonHttpHelper", "downloadRanged");
			//e.printStackTrace();
		}

		return is;
	}

	public int getFileLength(String url) {
		HttpGet get = new HttpGet(url);
		HttpResponse resp = null;
		try {
			resp = client.execute(get);
		} catch (IOException e) {
			Log.e("commonHttpHelper", "getFileLength");
			e.printStackTrace();
		}
		if (resp != null) {
			return (int) resp.getEntity().getContentLength();
		}
		return -1;
	}
}