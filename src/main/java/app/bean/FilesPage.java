package app.bean;

import java.util.List;

public class FilesPage {
	private String name;
	private List<SingleFod> list;
	private int size;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<SingleFod> getList() {
		return list;
	}
	public void setList(List<SingleFod> list) {
		this.list = list;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public FilesPage() {
		super();
		// TODO Auto-generated constructor stub
	}
	public FilesPage(String name) {
		super();
		this.name = name;
	}
	@Override
	public String toString() {
		return "FilesPage [name=" + name + ", list=" + list + ", size=" + size + "]";
	}
}
