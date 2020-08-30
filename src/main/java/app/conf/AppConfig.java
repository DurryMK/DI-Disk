package app.conf;

import java.io.IOException;

import javax.servlet.MultipartConfigElement;

import org.apache.hadoop.fs.FileSystem;
import org.apache.log4j.Logger;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.unit.DataSize;

@Configuration
@ComponentScan(basePackages = { "app" })
public class AppConfig {
	private Logger log = Logger.getLogger(AppConfig.class);

	@Bean
	public FileSystem hdfs() {
		org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
		conf.set("fs.defaultFS", "hdfs://39.105.42.45:8020");
		conf.set("dfs.client.use.datanode.hostname", "true");
		conf.set("yarn.resourcemanager.hostname", "39.105.42.45");
		FileSystem fs = null;
		try {
			fs = FileSystem.get(conf);
		} catch (IOException e) {
			log.error("hadoop连接失败:" + e.getMessage());
		}
		return fs;
	}

	@Bean
	public DriverManagerDataSource mysqlDataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/test?characterEncoding");
		dataSource.setUsername("root");
		dataSource.setPassword("sys123");
		return dataSource;
	}

	@Bean
	public DataSourceTransactionManager dtm(DriverManagerDataSource ds) {
		DataSourceTransactionManager dtm = new DataSourceTransactionManager(ds);
		return dtm;
	}

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize(DataSize.ofMegabytes(100));
		// 上传数据总大小
		factory.setMaxRequestSize(DataSize.ofMegabytes(1000));
		return factory.createMultipartConfig();
	}
}
