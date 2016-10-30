package bluemix;

public class Image {
	
	String path;
	String category;
	
	public Image(String path, String category) {
		super();
		this.path = path;
		this.category = category;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	
	
}
