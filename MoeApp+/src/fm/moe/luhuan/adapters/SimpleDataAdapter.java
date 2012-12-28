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

	private void setDetail(View v, SimpleData item) {
		TextView tag = (TextView) v.findViewById(R.id.item_description);
		TextView title = (TextView) v.findViewById(R.id.item_title);
		String tagText = item.getArtist();
		if (tagText == null || tagText.equals("")) {
			tagText = item.getDescription();
			if (tagText == null || tagText.equals("")) {
				tagText = "未知艺术家";
			}
		}
		v.setTag(R.string.item_id, item.getId());
		title.setText(Html.fromHtml(item.getTitle()));
		if(item.getId()!=-1){
			tag.setText(Html.fromHtml(tagText));
		}
		
	}

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
		SimpleData item = data.get(position);
		if (convertView == null||item.getId()==-1) {
			LinearLayout ll = (LinearLayout) inflater.inflate(
					R.layout.simple_list_item, null);
			setDetail(ll, item);
			return ll;
		} else {
			if ((Integer) convertView.getTag(R.string.item_id) != item.getId()) {
				setDetail(convertView, item);
			}
			return convertView;
		}

	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		SimpleData item = data.get(position);
		if (item.getId() == -1) {
			return false;
		}
		return true;

	}

	public List<SimpleData> getData() {
		return data;
	}

}
