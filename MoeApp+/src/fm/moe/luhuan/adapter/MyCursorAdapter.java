package fm.moe.luhuan.adapter;

import fm.moe.luhuan.R;
import android.content.Context;
import android.database.Cursor;

import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.TextView;

public class MyCursorAdapter extends CursorAdapter{
	private LayoutInflater inflater;
	
	public MyCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		inflater = LayoutInflater.from(context);
		
	}
	@Override
	public View newView(Context context, Cursor cursor,
			ViewGroup parent) {
		
		LinearLayout ll = (LinearLayout) inflater.inflate(
				R.layout.simple_list_item, null);
		TextView title = (TextView) ll
				.findViewById(R.id.item_title);
		TextView description = (TextView) ll
				.findViewById(R.id.item_description);
		title.setText(Html.fromHtml(cursor.getString(1)));
		String desText = cursor.getString(2);
		if(desText==null||desText.equals("")){
			desText="未知艺术家";
		}
		description.setText(cursor.getString(2));
		return ll;
		
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		LinearLayout ll = (LinearLayout) view;
		TextView title = (TextView) ll
				.findViewById(R.id.item_title);
		TextView description = (TextView) ll
				.findViewById(R.id.item_description);
		title.setText(Html.fromHtml(cursor.getString(1)));
		String desText = cursor.getString(2);
		if(desText==null||desText.equals("")){
			desText="未知艺术家";
		}
		description.setText(desText);
	}
}
