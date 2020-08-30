package app.bean;

public class ResModel {
	private int code;
	private String msg;
	private Object obj;
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Object getObj() {
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}
	
	public ResModel() {
		super();
	}
	
	public ResModel(int code, String msg, Object obj) {
		super();
		this.code = code;
		this.msg = msg;
		this.obj = obj;
	}
	
	public ResModel(int code, Object obj) {
		super();
		this.code = code;
		this.obj = obj;
	}
	public ResModel(int code) {
		super();
		this.code = code;
	}
	@Override
	public String toString() {
		return "ResModel [code=" + code + ", msg=" + msg + ", obj=" + obj + "]";
	}
	
}
