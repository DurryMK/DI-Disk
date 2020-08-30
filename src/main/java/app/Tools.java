package app;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import org.springframework.web.multipart.MultipartFile;

import app.bean.FilesPage;
import app.bean.SingleFod;

public class Tools {
	private static Logger log = Logger.getLogger(Tools.class);
	// 空地址
	public static final String nullPath = "";
	// 返回上一页地址
	public static final String previous = "@#$^";
	// 文件分隔符的替代
	public static final String reFile = "*";
	// 非法字符
	public static final String[] illChar = new String[] { "@", "*", "#", "$", "^", "/" };
	// 默认可用空间
	public static final long available = 524288000;

	public static void main(String[] args) {

	}

	public static void inputStreamToFile(InputStream ins, File file) {
		try {
			OutputStream os = new FileOutputStream(file);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.close();
			ins.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// MultipartFile=>File
	static public File MultipartFileToFile(MultipartFile file) {
		File f = null;
		try {

			if (file.equals("") || file.getSize() <= 0) {
				file = null;
			} else {
				InputStream ins = file.getInputStream();
				f = new File(file.getOriginalFilename());
				inputStreamToFile(ins, f);
			}
			return f;
		} catch (Exception e) {
			e.printStackTrace();
			return f;
		}
	}

	// 打包多个文件
	public static void zipUtil(Map<String, FSDataInputStream> ins, OutputStream out) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(out);
		try {
			for (Map.Entry<String, FSDataInputStream> in : ins.entrySet()) {
				// 取出每个文件输入到zos
				String name = in.getKey();
				FSDataInputStream fis = in.getValue();
				try {
					byte[] buffer = new byte[1024];
					int len;
					// 创建zip实体（一个文件对应一个ZipEntry）
					ZipEntry entry = new ZipEntry(name);
					// 获取需要下载的文件流
					zos.putNextEntry(entry);
					// 文件流循环写入ZipOutputStream
					while ((len = fis.read(buffer)) != -1) {
						zos.write(buffer, 0, len);
					}
				} catch (Exception e) {
					log.error(name + "压缩时出错...");
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			zos.close();
			for (Map.Entry<String, FSDataInputStream> in : ins.entrySet()) {
				in.getValue().close();
			}
		}
	}

	// 获取路径下所有文件的输出流
	public static List<String> pathInDirectory(String path, List<String> pl, FileSystem fs)
			throws FileNotFoundException, IllegalArgumentException, IOException {
		FileStatus[] listStatus = fs.listStatus(new Path(path));

		for (FileStatus f : listStatus) {
			String fpath = path + File.separator + f.getPath().getName();
			if (f.isFile()) {
				pl.add(fpath);
			}
			if (f.isDirectory()) {
				pl = pathInDirectory(fpath, pl, fs);
			}
		}
		return pl;
	}

	private static final String[] imgtype = { "WEBP", "BMP", "PCX", "TIF", "GIF", "JPG", "JPEG", "TGA", "EXIF", "FPX",
			"SVG", "PSD", "CDR", "PCD", "DXF", "UFO", "EPS", "AI", "PNG", "HDRI", "RAW", "WMF", "FLIC", "EMF", "ICO" };
	private static final String[] wdtype = { "txt", "doc", "docx", "wps", "csv", "html", "css", "js", "java", "py" };

	private static final Set<String> imgSet = new HashSet<String>(Arrays.asList(imgtype));
	private static final Set<String> wdSet = new HashSet<String>(Arrays.asList(wdtype));

	// 获取路径下所有的目录,文件的路径=>(名字,路径)
	public static Map<String, String> allPath(String path, Map<String, String> pl, FileSystem fs)
			throws FileNotFoundException, IllegalArgumentException, IOException {
		FileStatus[] listStatus = fs.listStatus(new Path(path));
		for (FileStatus f : listStatus) {
			String fpath = path + File.separator + f.getPath().getName();
			if (f.isFile()) {
				pl.put(f.getPath().getName(), fpath);
			}
			if (f.isDirectory()) {
				pl.put(f.getPath().getName(), fpath);
				pl = allPath(fpath, pl, fs);
			}
		}
		return pl;
	}

	// 获取路径下所有的目录,文件的路径=>(名字,路径)
	public static Map<String, String> pathFilter(String path, Map<String, String> pl, FileSystem fs, int type)
			throws FileNotFoundException, IllegalArgumentException, IOException {
		pl = allPath(path, pl, fs);
		List<String> dellist = new ArrayList<>();
		for (Map.Entry<String, String> me : pl.entrySet()) {
			String last = me.getKey().substring(me.getKey().lastIndexOf(".") + 1, me.getKey().length());
			String lastUp = last.toUpperCase();
			String lastlow = last.toLowerCase();
			if (type == 1) {
				if (!imgSet.contains(lastUp) && !imgSet.contains(lastlow)) {
					dellist.add(me.getKey());
				}
			} else if (type == 2) {
				if (!wdSet.contains(lastUp) && !wdSet.contains(lastlow)) {
					dellist.add(me.getKey());
				}
			} else {
				if (imgSet.contains(lastUp) || imgSet.contains(lastlow) || wdSet.contains(last)
						|| wdSet.contains(lastlow)) {
					dellist.add(me.getKey());
				}
			}
		}
		for (String s : dellist) {
			pl.remove(s);
		}
		return pl;
	}

	// 对目录集进行排序 文件在前目录在后 按首字母排序
	public static FilesPage sortPage(FilesPage fp) {

		List<SingleFod> lsf = fp.getList();

		TreeMap<String, SingleFod> file = new TreeMap<>();
		TreeMap<String, SingleFod> dir = new TreeMap<>();
		for (SingleFod sf : lsf) {
			if (sf.getIsFile() == 1) {
				file.put(sf.getName(), sf);
			} else {
				dir.put(sf.getName(), sf);
			}
		}
		lsf.clear();
		for (Map.Entry<String, SingleFod> me : file.entrySet()) {
			lsf.add(me.getValue());
		}
		for (Map.Entry<String, SingleFod> me : dir.entrySet()) {
			lsf.add(me.getValue());
		}
		fp.setList(lsf);
		return fp;
	}

	public static String formatSize(long len) {
		String size = "";
		if (len / 1024 / 1024 / 1024 / 1024 > 0) {
			size = len / 1024 / 1024 / 1024 / 1024 + "T";
		} else if (len / 1024 / 1024 / 1024 > 0) {
			size = len / 1024 / 1024 / 1024 + "G";
		} else if (len / 1024 / 1024 > 0) {
			size = len / 1024 / 1024 + "M";
		} else if (len / 1024 > 0) {
			size = len / 1024 + "KB";
		} else {
			size = len + "b";
		}
		return size;
	}
}
