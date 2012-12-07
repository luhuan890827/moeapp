package fm.moe.luhuan.beans.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class Sub extends MoeItemData{
	private HashMap<String, String> meta;
	private int albumId;
	private int order;
	private long saleDate;
	public HashMap<String, String> getMeta() {
		return meta;
	}
	public void setMeta(HashMap<String, String> meta) {
		this.meta = meta;
	}
	
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public long getSaleDate() {
		return saleDate;
	}
	public void setSaleDate(long saleDate) {
		this.saleDate = saleDate;
	}
	public List<Sub> getSubsFromJSON(String s,HashMap<String, String> covers){
		ArrayList<Sub> l = new ArrayList<Sub>();
		JSONArray ja = JSON.parseObject(s).getJSONArray("subs");
		for (int i = 0; i < ja.size(); i++) {
			Sub sub = getObj(ja.getJSONObject(i), covers);
			l.add(sub);
		}
		
		return l;
	}
	private static Sub getObj(JSONObject jo,HashMap<String, String> covers){
		Sub s = new Sub();
		s.setId(jo.getIntValue("sub_id"));
		s.setOrder(jo.getIntValue("sub_order"));
		s.setAlbumId(jo.getIntValue("sub_parent_id"));
		s.setTitle(jo.getString("sub_title"));
		s.setType(jo.getString("sub_type"));
		s.setWebUrl(jo.getString("sub_url"));
		s.setSaleDate(jo.getLongValue("sub_date"));
		HashMap<String, String>  meta = new HashMap<String, String>();
		s.setMeta(meta);
		JSONArray ja = jo.getJSONArray("sub_meta");
		for (int i = 0; i < ja.size(); i++) {
			JSONObject t = ja.getJSONObject(i);
			meta.put(t.getString("meta_key"), t.getString("meta_value"));
		}
		s.setCovers(covers);
		
		return s;
	}
	public int getAlbumId() {
		return albumId;
	}
	public void setAlbumId(int albumId) {
		this.albumId = albumId;
	}
	
}
