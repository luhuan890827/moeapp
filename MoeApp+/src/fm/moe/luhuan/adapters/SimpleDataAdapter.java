package fm.moe.luhuan.adapters;

import java.util.List;
import java.util.zip.Inflater;

import fm.moe.luhuan.R;
import fm.moe.luhuan.beans.data.SimpleData;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SimpleDataAdapter extends BaseAdapter {

	private List<SimpleData> data;
	private LayoutInflater inflater;

	public SimpleDataAdapter(Context c, List<SimpleData> l) {

		data = l;
		inflater = LayoutInflater.from(c);
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LinearLayout ll = (LinearLayout) inflater.inflate(
				R.layout.simple_list_item, null);
		//Log.e("class", data.get(position).getClass().toString());
		
		
		SimpleData item = data.get(position);

		TextView artist = (TextView) ll.findViewById(R.id.item_description);
		TextView title = (TextView) ll.findViewById(R.id.item_title);
//		Log.e("id", item.getId()+"");
//		Log.e("title", item.getTitle()+"");
		if (item.getArtist() == null || item.getArtist().equals("")) {
			artist.setText("δ֪������");
		} else {
			artist.setText(Html.fromHtml(item.getArtist()));
		}
		ll.setTag(R.string.item_id,item.getId());
		ll.setTag(R.string.item_description,item.getDescription());
		if(item.getMp3Url()!=null){
			//Log.e("in adapter","mp3 url = " +item.getMp3Url());
			ll.setTag(R.string.item_mp3_url,item.getMp3Url());
		}
		
		title.setText(Html.fromHtml(item.getTitle()));
		return ll;
	}
	public List<SimpleData> getData(){
		return data;
	}

}
