package app.contorller;

import javax.annotation.Resource;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import app.Tools;
import app.bean.ResModel;
import app.bean.User;
import app.dao.UserMapper;
import app.service.FileServiceImpl;

@Controller
public class DistributeController {

	@Autowired
	private FileServiceImpl fsi;

	private static Logger log = Logger.getLogger(DistributeController.class);

	// 跳转到登录界面
	@RequestMapping("/index")
	public String toIndex() {
		return "/index.html";
	}

	// 跳转到注册界面
	@RequestMapping("/reg")
	public String reg() {
		return "/reg.html";
	}

	// 验证用户是否登录
	@RequestMapping("/validation")
	public @ResponseBody ResModel validation(HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user == null) {
			return new ResModel(-1);
		} else {
			// 返回用户当前路径
			user.setCpath(session.getAttribute("cpath") + "");
			return new ResModel(0, user);
		}
	}

	@Resource
	private UserMapper um;

	// 用户登录
	@RequestMapping("/index/login")
	public @ResponseBody ResModel login(String name, String pwd, HttpSession session) {
		User user = um.selectUser(name, pwd);
		if (user != null) {
			// 保存用户信息
			session.setAttribute("user", user);
			session.setAttribute("root", user.getPath());
			// 保存当前用户路径 首次登录时当前路径为空
			session.setAttribute("cpath", Tools.nullPath);
			log.info("【登录】  用户:" + name);
			return new ResModel(0);
		}
		return new ResModel(-1);
	}

	// 用户退出登录
	@RequestMapping("/exit")
	public @ResponseBody ResModel exit(HttpSession session) {
		session.removeAttribute("user");
		return new ResModel(0);
	}

	// 用户注册
	@RequestMapping("/index/reg")
	public @ResponseBody ResModel reg(String name, String pwd, HttpSession session) {
		for (String s : Tools.illChar) {
			if (name.contains(s)) {
				// 用户名非法
				return new ResModel(-2);
			}
		}
		User user = um.selectUserWithName(name);
		if (user != null) {
			// 用户已存在
			return new ResModel(-1);
		}
		try {
			String userDir = fsi.createUserDir(name);
			User u = new User();
			u.setName(name);
			u.setPwd(pwd);
			u.setPath(userDir);
			u.setAvailable(Tools.available);
			u.setLevel(1);
			u.setUsed(0);
			um.insertUser(u);
			return new ResModel(0);
		} catch (Exception e) {
			log.error(e.getMessage());
			return new ResModel(-3);
		}
	}

}
