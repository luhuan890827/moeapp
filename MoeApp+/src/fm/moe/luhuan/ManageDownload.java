package fm.moe.luhuan;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.List;

import fm.moe.luhuan.adapter.SimpleDataAdapter;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.utils.DataStorageHelper;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ManageDownload extends ListActivity{
@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.list_wrapper);
	DataStorageHelper helper = new DataStorageHelper(getApplicationContext());
	try {
		List<SimpleData> list = helper.readDownloadList();
		if(list!=null){
			SimpleDataAdapter adapter = new SimpleDataAdapter(getApplicationContext(), list);
			setListAdapter(adapter);
		}
		
	} catch (Exception e) {
		e.printStackTrace();
		Log.e("", "",e);
	}
	
	
}

}
