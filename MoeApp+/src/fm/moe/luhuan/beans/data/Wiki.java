package fm.moe.luhuan.beans.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class Wiki extends MoeItemData{
	private HashMap<String, String> meta;
	private long saleDate;
	public HashMap<String, String> getMeta() {
		return meta;
	}
	public void setMeta(HashMap<String, String> meta) {
		this.meta = meta;
	}
	public long getSaleDate() {
		return saleDate;
	}
	public void setSaleDate(long saleDate) {
		this.saleDate = saleDate;
	}
	public static List<Wiki> getWikisFromJSON(String s){
		ArrayList<Wiki> l = new ArrayList<Wiki>();
		JSONArray ja = JSON.parseObject(s).getJSONArray("wikis");
		for (int i = 0; i < ja.size(); i++) {
			Wiki w = getObj(ja.getJSONObject(i));
			l.add(w);
		}
		return l;
	}
	private static Wiki getObj(JSONObject jo){
		Wiki w = new Wiki();
		w.setId(jo.getIntValue("wiki_id"));
		w.setTitle(jo.getString("wiki_title"));
		w.setType(jo.getString("wiki_type"));
		w.setWebUrl(jo.getString("wiki_url"));
		w.setSaleDate(jo.getLongValue("wiki_date"));
		HashMap<String, String>  meta = new HashMap<String, String>();
		w.setMeta(meta);
		JSONArray ja = jo.getJSONArray("wiki_meta");
		for (int i = 0; i < ja.size(); i++) {
			JSONObject t = ja.getJSONObject(i);
			meta.put(t.getString("meta_key"), t.getString("meta_value"));
		}
		
		
		return w;
	} 
}
