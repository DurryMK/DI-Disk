package a.MyDisk;

import java.io.IOException;


import javax.annotation.Resource;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import app.conf.AppConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { AppConfig.class })
public class AppTest {
	@Resource
	@Autowired
	private FileSystem fs;

	@Test
	public void a() {
		try {
			FSDataOutputStream out = fs.create(new Path("/a/a.txt"));
			String string = "nimamaipi";
			byte[] b = new byte[1024 * 1024];
			b = string.getBytes();
			out.write(b, 0, b.length);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
