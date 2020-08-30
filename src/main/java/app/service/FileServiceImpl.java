package app.service;

import java.io.File;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import app.Tools;
import app.bean.FilesPage;
import app.bean.SingleFod;
import app.contorller.DistributeController;

@Service
public class FileServiceImpl implements FileService {

	@Autowired
	private FileSystem fs;

	private static Logger log = Logger.getLogger(DistributeController.class);
	public FilesPage getFilePath(FilesPage fp) {

		try {
			// 取出要查询的根路径
			Path root = new Path(fp.getName());
			// 文件路径是否存在
			if (!fs.exists(root)) {
				return null;
			}
			// 获取所有的目录与文件
			FileStatus[] list = fs.listStatus(root);
			// 存放路径下所有的目录
			List<SingleFod> items = new ArrayList<SingleFod>();
			// 设置时间格式
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// 遍历
			for (FileStatus next : list) {
				// 目录名
				String name = next.getPath().getName();
				// 上一次的修改时间
				String time = df.format(next.getModificationTime());
				// 文件1 目录0
				if (next.isDirectory()) {
					// 名字,修改时间,大小,是否是文件
					SingleFod sf = new SingleFod(name, time, "--", 0);
					items.add(sf);
				}
				if (next.isFile()) {
					// 文件大小
					String size = Tools.formatSize(next.getLen());
					SingleFod sf = new SingleFod(name, time, size, 1);
					items.add(sf);
				}
			}
			fp.setList(items);
			// 目录与文件的总数
			fp.setSize(items.size());
			return fp;
		} catch (Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}

	// 创建新文件夹 创建成功返回新文件夹路径 创建失败返回空字符串
	public boolean mkDir(String path, String newDir) {
		try {
			// 文件夹名称为空
			if (newDir.isEmpty()) {
				newDir = "新建文件夹";
			}
			// 获取路径下所有的文件夹名称
			FilesPage list = getFilePath(new FilesPage(path));
			for (SingleFod sf : list.getList()) {
				// 如果文件名重复则在新文件名后加上时间戳
				if (sf.getIsFile() == 0 && sf.getName().equals(newDir)) {
					newDir = newDir + System.currentTimeMillis();
				}
			}
			// 创建新文件
			path = path + "/" + newDir;
			fs.mkdirs(new Path(path));
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		}
	}

	public boolean uploadFile(String path, MultipartFile uploadFile) {
		FSDataOutputStream out = null;
		FileInputStream in = null;
		try {
			out = fs.create(new Path(path + "/" + uploadFile.getOriginalFilename()));
			File f = Tools.MultipartFileToFile(uploadFile);
			in = new FileInputStream(f);
			byte[] b = new byte[1024 * 1024];
			int read = 0;
			while ((read = in.read(b)) > 0) {
				out.write(b, 0, read);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		}
		return true;
	}

	public void downLoad(String path, String[] list, HttpServletResponse response) {
		// http输出流
		OutputStream out = null;
		// hdfs文件输出流
		FSDataInputStream in = null;
		try {
			// hdfs的文件路径
			Path filepath = new Path(path + "/" + list[0]);
			FileStatus status = fs.getFileStatus(filepath);
			// 下载文件
			if (status.isFile()) {
				byte[] buffer = new byte[1024];
				// 设置头域
				response.setContentType("application/x-download");
				response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(list[0], "UTF-8"));
				// 获取输出流
				out = response.getOutputStream();
				// 获取输入流
				in = fs.open(filepath);
				int i = in.read(buffer);
				while (i != -1) {
					out.write(buffer, 0, i);
					i = in.read(buffer);
				}
				out.flush();
			}
			// 如果是文件
			if (status.isDirectory()) {
				zipDownLoad(path, list, response);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		}
	}

	public void zipDownLoad(String path, String[] list, HttpServletResponse response) {
		try {
			Map<String, FSDataInputStream> inlist = new HashMap<>();
			// 设置头域
			response.setContentType("application/x-download");
			response.addHeader("Content-Disposition", "attachment;filename=DIDownLoad.zip");
			// 只实现多个文件压缩下载
			for (String name : list) {
				// 文件路径字符串
				String pathString = path + File.separator + name;
				// hdfs的文件路径
				Path filepath = new Path(pathString);
				// hdfs的文件状态
				FileStatus status = fs.getFileStatus(filepath);
				// 将每个待下载文件的名字和输出流保存
				if (status.isFile()) {
					// 文件类型直接保存
					FSDataInputStream input = fs.open(filepath);
					inlist.put(name, input);
				}

				if (status.isDirectory()) {
					// 如果是目录则递归获取目录下所有的文件
					List<String> pl = new ArrayList<>();
					pl = Tools.pathInDirectory(pathString, pl, fs);
					// 获取所有文件的输出流
					for (String l : pl) {
						String lname = l.substring(l.lastIndexOf(File.separator) + 1, l.length());
						FSDataInputStream linput = fs.open(new Path(l));
						inlist.put(lname, linput);
					}
				}
			}
			Tools.zipUtil(inlist, response.getOutputStream());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public boolean delete(String files, String path) {
		String[] list = files.replace("[", "").replace("]", "").replace("\"", "").split(",");
		try {
			for (String name : list) {
				if (!(path + name).trim().equals(path)) {
					Path p = new Path(path + File.separator + name);
					fs.delete(p, true);
				}
			}
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		}
	}

	public FilesPage search(String key, String path) {
		Map<String, String> allPath = new HashMap<>();
		// 查询到的所有结果
		FilesPage fp = new FilesPage();
		try {
			// 存放路径下所有的目录
			List<SingleFod> items = new ArrayList<SingleFod>();
			// 设置时间格式
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// 获取路径下所有的文件 其文件名与文件路径的键值对
			allPath = Tools.allPath(path, allPath, fs);
			for (Map.Entry<String, String> m : allPath.entrySet()) {
				if (m.getKey().contains(key)) {
					// 文件名
					String name = m.getKey();
					// 文件路径
					String mpath = m.getValue();
					// 文件信息
					FileStatus fss = fs.getFileStatus(new Path(mpath));
					// 上一次的修改时间
					String time = df.format(fss.getModificationTime());
					// 文件1 目录0
					if (fss.isDirectory()) {
						// 名字,修改时间,大小,是否是文件
						SingleFod sf = new SingleFod(name, time, "--", 0);
						// 需要去掉File.separator 否则会生成不正确的下载url地址
						sf.setPath(
								m.getValue().replace(path + File.separator, "").replace(File.separator, Tools.reFile));
						items.add(sf);
					}
					if (fss.isFile()) {
						// 文件大小
						String size = Tools.formatSize(fss.getLen());
						SingleFod sf = new SingleFod(name, time, size, 1);
						sf.setPath(
								m.getValue().replace(path + File.separator, "").replace(File.separator, Tools.reFile));
						items.add(sf);
					}
				}
			}
			fp.setList(items);
			// 目录与文件的总数
			fp.setSize(items.size());
			return fp;
		} catch (IllegalArgumentException | IOException e) {
			log.error(e.getMessage());
			return null;
		}
	}

	// 文件过滤
	public FilesPage filter(int type, String root) {
		Map<String, String> allPath = new HashMap<>();
		// 查询到的所有结果
		FilesPage fp = new FilesPage();
		try {
			// 存放路径下所有的目录
			List<SingleFod> items = new ArrayList<SingleFod>();
			// 设置时间格式
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// 获取路径下所有的文件 其文件名与文件路径的键值对
			allPath = Tools.pathFilter(root, allPath, fs, type);
			for (Map.Entry<String, String> m : allPath.entrySet()) {
				// 文件名
				String name = m.getKey();
				// 文件路径
				String mpath = m.getValue();
				// 文件信息
				FileStatus fss = fs.getFileStatus(new Path(mpath));
				// 上一次的修改时间
				String time = df.format(fss.getModificationTime());
				// 只获取文件
				if (fss.isFile()) {
					// 文件大小
					String size = Tools.formatSize(fss.getLen());
					SingleFod sf = new SingleFod(name, time, size, 1);
					// 将文件分割符用%代替
					sf.setPath(m.getValue().replace(root + File.separator, "").replace(File.separator, Tools.reFile));
					items.add(sf);
				}
			}
			fp.setList(items);
			// 目录与文件的总数
			fp.setSize(items.size());
			return fp;
		} catch (IllegalArgumentException | IOException e) {
			log.error(e.getMessage());
			return null;
		}
	}

	public String createUserDir(String name) {
		String userDir = File.separator + name + UUID.randomUUID();
		try {
			fs.mkdirs(new Path(userDir));
			return userDir;
		} catch (Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}
}
