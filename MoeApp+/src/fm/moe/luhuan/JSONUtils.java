package fm.moe.luhuan;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import fm.moe.luhuan.beans.data.SimpleData;

public class JSONUtils {
	private static SimpleData getSimpleData(JSONObject obj) {
		SimpleData data = new SimpleData();
		data.setTitle(obj.getString("wiki_title"));
		data.setId(obj.getIntValue("wiki_id"));
		JSONArray arr = obj.getJSONArray("wiki_meta");
		String artist = null;
		String description = null;
		for (int i = 0; i < arr.size(); i++) {
			JSONObject t = arr.getJSONObject(i);
			String key = t.getString("meta_key");
			String val = t.getString("meta_value");
			int type = t.getIntValue("meta_type");
			if(key.contains("¼ò½é")){
				artist = val;
			}
			if(key.contains("ÒÕÊõ")){
				description = val;
			}

		}
		if (artist == null) {
			data.setArtist(description);
		} else {
			data.setArtist(artist);
		}

		return data;
	}
	private static SimpleData getSub(JSONObject obj,String description){
		SimpleData data = new SimpleData();
		data.setId(obj.getIntValue("sub_id"));
		data.setTitle(obj.getString("sub_title"));
		String info = null;
		JSONArray arr = obj.getJSONArray("sub_meta");
		if(arr!=null){
			for (int i = 0; i < arr.size(); i++) {
				JSONObject t = arr.getJSONObject(i);
				String key = t.getString("meta_key");
				String val = t.getString("meta_value");
				int type = t.getIntValue("meta_type");
				if(key.contains("ÑÝ³ª")){
					info = val;
					break;
				}
				

			}
			
		}
		if(info==null){
			info = description;
		}
		data.setArtist(info);
		return data;
		
	}
	public static List<SimpleData> getSimpelDataList(String json,
			String key) {
		List<SimpleData> l = new ArrayList<SimpleData>();
		JSONArray arr = JSON.parseObject(json).getJSONObject("response")
				.getJSONArray(key);
		for (int i = 0; i < arr.size(); i++) {
			SimpleData data = getSimpleData(arr.getJSONObject(i));
			l.add(data);
		}
		return l;
	}
	public static List<SimpleData> getSubsList(String json,String description){
		List<SimpleData> l = new ArrayList<SimpleData>();
		JSONArray arr = JSON.parseObject(json).getJSONObject("response")
				.getJSONArray("subs");
		for (int i = 0; i < arr.size(); i++) {
			SimpleData data = getSub(arr.getJSONObject(i),description);
			l.add(data);
		}
		return l;
	}
}
