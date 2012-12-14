package fm.moe.luhuan.beans.data;

public class SongDetail extends SimpleData{
	/**
	 * 
	 */
	
//	private int id;
//	private String artist;
//	private String title;
//	private String description;
//	private String mp3Url;
	private static final long serialVersionUID = 1L;
	private int parentId;
	private String coverUrl;
	private boolean isFav;
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public String getCoverUrl() {
		return coverUrl;
	}
	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}
	public boolean isFav() {
		return isFav;
	}
	public void setFav(boolean isFav) {
		this.isFav = isFav;
	}
	
}
