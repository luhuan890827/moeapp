package fm.moe.luhuan.http;

public class MoeUrlBuilder {
	private String base="http://api.moefou.org/";
	private String path;
	private String query;
	private String url;
	
	public MoeUrlBuilder() {
		
	}
	public void setBase(String b){
		base = b;
	}
	public void setPath(String p){
		path = p;
	}
	public void setQuery(String q){
		query = q;
	}
	public String getUrl(){
		url = base+path+"?api=json&"+query;
		return url;
	}
}
