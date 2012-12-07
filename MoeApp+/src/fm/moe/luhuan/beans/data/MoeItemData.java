package fm.moe.luhuan.beans.data;

import java.util.HashMap;

public class MoeItemData {
	private int id;
	private String title;
	private String type;
	private String webUrl;
	
	private HashMap<String, String> covers;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
	}

	public HashMap<String, String> getCovers() {
		return covers;
	}

	public void setCovers(HashMap<String, String> covers) {
		this.covers = covers;
	}

}
