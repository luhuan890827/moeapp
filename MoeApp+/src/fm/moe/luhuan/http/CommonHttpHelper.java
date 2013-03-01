package fm.moe.luhuan.http;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
	public String httpGet(String url){
		
		return null;
	}
	public Bitmap getBitmap(String url) throws IllegalStateException, IOException {
		HttpGet request = new HttpGet(url);
		Bitmap bm = null;
	
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			InputStream inStream = entity.getContent();
			bm = BitmapFactory.decodeStream(inStream);
		

		return bm;
	}

	public InputStream downloadRanged(String url, int start, int end) throws ClientProtocolException, IOException {
		InputStream is = null;
		HttpGet get = new HttpGet(url);
		get.setHeader("range", "bytes=" + start + "-" + end);
		HttpResponse resp = null;

		
			resp = client.execute(get);
			is = resp.getEntity().getContent();
		

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
