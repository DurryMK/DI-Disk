package app.contorller;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.hadoop.fs.FileSystem;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import app.Tools;
import app.bean.FilesPage;
import app.bean.ResModel;
import app.service.FileServiceImpl;

@Controller
public class HdfsController {
	@Autowired
	private FileServiceImpl fsi;

	@SuppressWarnings("unused")
	@Autowired
	private FileSystem fs;
	

	private static Logger log = Logger.getLogger(HdfsController.class);

	// 获取指定页面的所有文件和目录信息
	@RequestMapping("/getList")
	public @ResponseBody ResModel getList(String path, HttpSession session) {
		if (path.contains(Tools.reFile)) {
			path = File.separator + path.replace(Tools.reFile, File.separator);
		}
		// 需要查询的路径
		String hdfsPath = Tools.nullPath;
		// 用户的根目录
		String root = (String) session.getAttribute("root");
		// 当前用户的目录
		String cpath = (String) session.getAttribute("cpath");
		if (Tools.nullPath.equals(path)) {
			hdfsPath = root;
			// 更新当前路径
			session.setAttribute("cpath", path);
		} else if (Tools.previous.equals(path)) {
			// 回到父目录
			// 父目录
			if (Tools.nullPath.equals(cpath)) {
				hdfsPath = cpath;
			} else {
				hdfsPath = cpath.substring(0, cpath.lastIndexOf(File.separator));
			}
			// 更新当前路径
			session.setAttribute("cpath", hdfsPath);
			// 最终查询路径
			hdfsPath = root + hdfsPath;
		} else if (path.contains(File.separator)) {
			// 查询一个完整的目录
			// 更新当前路径
			session.setAttribute("cpath", path);
			// 最终查询路径
			hdfsPath = root + path;
		} else {
			// 进入子目录
			hdfsPath = cpath + File.separator + path;
			// 更新当前路径
			session.setAttribute("cpath", hdfsPath);
			// 最终查询路径
			hdfsPath = (String) session.getAttribute("root") + hdfsPath;
		}
		// 获取路径下所有的文件信息
		FilesPage page = fsi.getFilePath(new FilesPage(hdfsPath));
		if (page != null) {
			return new ResModel(0, (String) session.getAttribute("cpath"), Tools.sortPage(page));
		}
		return new ResModel(-1);
	}

	// 指定目录新建文件夹
	@RequestMapping("/mkDir")
	public @ResponseBody ResModel mkDir(String newDir, HttpSession session) {
		// 用户的根目录
		String root = (String) session.getAttribute("root");
		// 当前用户的目录
		String cpath = (String) session.getAttribute("cpath");
		String dirPath = root + cpath;
		FilesPage page = fsi.getFilePath(new FilesPage(dirPath));
		if (fsi.mkDir(dirPath, newDir)) {
			page = fsi.getFilePath(new FilesPage(dirPath));
			return new ResModel(0, Tools.sortPage(page));
		}
		return new ResModel(-1, Tools.sortPage(page));
	}

	// 搜索所有的文件
	@RequestMapping("/searchOp")
	public @ResponseBody ResModel searchOp(String key, HttpSession session) {
		session.setAttribute("cpath", Tools.nullPath);
		FilesPage page = fsi.search(key, (String) session.getAttribute("root"));
		if (page != null) {
			return new ResModel(0, Tools.sortPage(page));
		}
		return new ResModel(-1);
	}

	// 上传文件
	@RequestMapping("/uploadFile")
	public @ResponseBody ResModel uploadFile(MultipartFile uploadFile, HttpSession session) throws IOException {
		log.info("【上传】  文件"+uploadFile.getOriginalFilename());
		// 用户的根目录
		String root = (String) session.getAttribute("root");
		// 当前用户的目录
		String cpath = (String) session.getAttribute("cpath");
		// 当前用户目录
		String filePath = root + cpath;
		FilesPage page = fsi.getFilePath(new FilesPage(filePath));
		if (fsi.uploadFile(filePath, uploadFile)) {
			page = fsi.getFilePath(new FilesPage(filePath));
			log.info("【上传】  文件"+uploadFile.getOriginalFilename()+"成功");
			return new ResModel(0, Tools.sortPage(page));
		}
		return new ResModel(-1, Tools.sortPage(page));
	}

	// 下载文件
	@RequestMapping("/downLoad")
	public @ResponseBody void downLoad(String files, HttpServletResponse response, HttpSession session)
			throws IOException {
		files = files.replace(Tools.reFile, File.separator);
		// 用户的根目录
		String root = (String) session.getAttribute("root");
		// 当前用户的目录
		String cpath = (String) session.getAttribute("cpath");
		// 当前用户目录
		String downPath = root + cpath + File.separator;
		// 文件列表
		String[] list = files.replace("[", "").replace("]", "").replace("\"", "").split(",");
		// 下载文件数大于1时使用文件压缩下载
		if (list.length > 1) {
			fsi.zipDownLoad(downPath, list, response);
		} else {
			fsi.downLoad(downPath, list, response);
		}
		log.info("【下载】  文件"+files);
	}

	// 删除文件
	@RequestMapping("/delete")
	public @ResponseBody ResModel delete(String files, HttpSession session) throws IOException {
		// 用户的根目录
		String root = (String) session.getAttribute("root");
		// 当前用户的目录
		String cpath = (String) session.getAttribute("cpath");
		// 当前用户目录
		String delPath = root + cpath;
		FilesPage page = fsi.getFilePath(new FilesPage(delPath));
		if (fsi.delete(files, delPath)) {
			page = fsi.getFilePath(new FilesPage(delPath));
			log.info("【删除】  文件"+files);
			return new ResModel(0, Tools.sortPage(page));
		}
		return new ResModel(-1, Tools.sortPage(page));
	}

	// 过滤文件
	@RequestMapping("/filter")
	public @ResponseBody ResModel filter(int type, HttpSession session) throws IOException {
		FilesPage page = fsi.filter(type, (String) session.getAttribute("root"));
		if (page != null) {
			session.setAttribute("cpath", "");
			return new ResModel(0, Tools.sortPage(page));
		}
		return new ResModel(-1);
	}
}
