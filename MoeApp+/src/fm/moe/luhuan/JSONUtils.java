package fm.moe.luhuan;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

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
			if (key.contains("简介")) {
				artist = val;
			}
			if (key.contains("艺术")) {
				description = val;
			}

		}
		if (artist!= null) {
			data.setArtist(artist);
		} else if(description!=null){
			data.setArtist(description);
		}else{
			data.setArtist("未知艺术家");
		}

		return data;
	}

	private static SimpleData getSub(JSONObject obj, String description,boolean reverseWiki) {
		
		SimpleData data = new SimpleData();
		data.setId(obj.getIntValue("sub_id"));
		data.setTitle(obj.getString("sub_title"));
		String info = null;
		JSONArray arr = obj.getJSONArray("sub_meta");
		if (arr != null) {
			for (int i = 0; i < arr.size(); i++) {
				JSONObject t = arr.getJSONObject(i);
				String key = t.getString("meta_key");
				String val = t.getString("meta_value");
				int type = t.getIntValue("meta_type");
				if (key.contains("演唱")||key.contains("简介")) {
					info = val;
					break;
				}

			}

		}
		SimpleData wikiData = null;
		if(obj.getJSONObject("wiki")!=null){
			wikiData = getSimpleData(obj.getJSONObject("wiki"));
		}
		
		
		if(wikiData!=null){
			info = wikiData.getArtist();
		}else
		if (info == null) {
			info = description;
		}
		data.setArtist(info);
		return data;

	}

	public static List<SimpleData> getSimpelDataList(String json, String key) {
		List<SimpleData> l = new ArrayList<SimpleData>();
		JSONArray arr = JSON.parseObject(json).getJSONObject("response")
				.getJSONArray(key);
		for (int i = 0; i < arr.size(); i++) {
			SimpleData data = getSimpleData(arr.getJSONObject(i));
			l.add(data);
		}
		return l;
	}

	public static List<SimpleData> getSubsList(String json, String description) {
		List<SimpleData> l = new ArrayList<SimpleData>();
		JSONArray arr = JSON.parseObject(json).getJSONObject("response")
				.getJSONArray("subs");
		for (int i = 0; i < arr.size(); i++) {
			SimpleData data = getSub(arr.getJSONObject(i), description,false);
			l.add(data);
		}
		return l;
	}

	public static List<SimpleData> getFavs(String json, String type) {
		List<SimpleData> l = new ArrayList<SimpleData>();
		JSONArray arr = JSON.parseObject(json).getJSONObject("response")
				.getJSONArray("favs");
		if(type.equals("wiki")){
			for(int i = 0;i<arr.size();i++){
				JSONObject obj = arr.getJSONObject(i).getJSONObject("obj");
				//Log.e("jo", obj.toJSONString());
				
				SimpleData data = getSimpleData(obj);
				//Log.e("id", data.getId()+"");
				l.add(data);
			}
		}else{
			for(int i = 0;i<arr.size();i++){
				JSONObject obj = arr.getJSONObject(i).getJSONObject("obj");
				//Log.e("", obj.toJSONString());
				SimpleData data = getSub(obj,"一些信息",true);
				
				l.add(data);
			}
		}
		return l;
	}
	public static List<SimpleData> getSimpleDataFromPlayList(String json){
		List<SimpleData> l = new ArrayList<SimpleData>();
		JSONArray arr= JSON.parseObject(json).getJSONObject("response").getJSONArray("playlist");
		for(int i = 0 ;i<arr.size();i++){
			JSONObject obj = arr.getJSONObject(i);
			SimpleData data = getSimpleDataFromPlayListItem(obj);
			l.add(data);
		}
		return l;
	}

	private static SimpleData getSimpleDataFromPlayListItem(JSONObject obj) {
		SimpleData data = new SimpleData();
		//Log.e("raw", obj.getString("url"));
		data.setArtist(obj.getString("artist"));
		data.setId(obj.getIntValue("sub_id"));
		data.setTitle(obj.getString("sub_title"));
		data.setMp3Url(obj.getString("url"));
		return data;
	}
}
