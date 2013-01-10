package fm.moe.luhuan.beans.data;

import android.os.Parcel;
import android.os.Parcelable;

public class SimpleData implements Parcelable{
	/**
	 * 
	 */
	
	private int parentId=-1;
	private String type;
	private int id;
	private String artist;
	private String title;
	private String description;
	private String mp3Url;
	private boolean isFav;
	private String albumnCoverUrl;
	private String parentTitle;
	public static final Parcelable.Creator<SimpleData> CREATOR = new Creator<SimpleData>() {
		
		@Override
		public SimpleData[] newArray(int size) {
			return new SimpleData[size];
		}
		
		@Override
		public SimpleData createFromParcel(Parcel source) {
			SimpleData item = new SimpleData();
			item.parentId = source.readInt();
			item.type = source.readString();
			item.id = source.readInt();
			item.artist = source.readString();
			item.title = source.readString();
			item.description = source.readString();
			item.mp3Url = source.readString();
			item.isFav = source.readInt()==0?false:true;
			item.albumnCoverUrl = source.readString();
			item.parentTitle = source.readString();
			return item;
		}
	};
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
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getMp3Url() {
		return mp3Url;
	}
	public void setMp3Url(String mp3Url) {
		this.mp3Url = mp3Url;
	}
	public boolean isFav() {
		return isFav;
	}
	public void setFav(boolean isFav) {
		this.isFav = isFav;
	}
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public String getAlbumnCoverUrl() {
		return albumnCoverUrl;
	}
	public void setAlbumnCoverUrl(String albumnCoverUrl) {
		this.albumnCoverUrl = albumnCoverUrl;
	}
	public String getParentTitle() {
		return parentTitle;
	}
	public void setParentTitle(String parentTitle) {
		this.parentTitle = parentTitle;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(parentId);
		dest.writeString(type);
		dest.writeInt(id);
		dest.writeString(artist);
		dest.writeString(title);
		dest.writeString(description);
		dest.writeString(mp3Url);
		dest.writeInt(isFav?1:0);
		dest.writeString(albumnCoverUrl);
		dest.writeString(parentTitle);
		
	}
	
}
