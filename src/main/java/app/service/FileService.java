package app.service;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import app.bean.FilesPage;

public interface FileService {
	FilesPage getFilePath(FilesPage fp);

	boolean mkDir(String path, String newDir);

	boolean uploadFile(String path, MultipartFile uploadFile);

	void downLoad(String path, String[] list, HttpServletResponse response);

	void zipDownLoad(String path, String[] list, HttpServletResponse response);

	boolean delete(String files, String path);

	FilesPage search(String key, String path);

	FilesPage filter(int type, String root);

	String createUserDir(String name);
}
