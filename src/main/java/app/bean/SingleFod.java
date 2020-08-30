package app.bean;

public class SingleFod {
	private String name;//文件名
	private String modTime;//修改时间
	private String size;//大小
	private int isFile;//文件或目录
	private String path;//
	public SingleFod(String name, String modTime, String size, int isFile) {
		super();
		this.name = name;
		this.modTime = modTime;
		this.size = size;
		this.isFile = isFile;
	}
	public SingleFod() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getModTime() {
		return modTime;
	}
	public void setModTime(String modTime) {
		this.modTime = modTime;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public int getIsFile() {
		return isFile;
	}
	public void setIsFile(int isFile) {
		this.isFile = isFile;
	}
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	@Override
	public String toString() {
		return "SingleFod [name=" + name + ", modTime=" + modTime + ", size=" + size + ", isFile=" + isFile + ", path="
				+ path + "]";
	}
	
}
