package fm.moe.luhuan.adapters;

import java.util.List;
import java.util.zip.Inflater;

import fm.moe.luhuan.R;
import fm.moe.luhuan.beans.data.SimpleData;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SimpleDataAdapter extends BaseAdapter{
	
	private List<SimpleData> data;
	private LayoutInflater inflater;
	public SimpleDataAdapter(Context c,List<SimpleData> l){
		
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
		LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.simple_list_item, null);
		SimpleData item = data.get(position);
		TextView artist = (TextView) ll.findViewById(R.id.item_description);
		TextView title = (TextView) ll.findViewById(R.id.item_title);
		artist.setText(Html.fromHtml(item.getArtist()));
		title.setText(Html.fromHtml(item.getTitle()));
		return ll;
	}

}
