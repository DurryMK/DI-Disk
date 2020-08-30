package app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "app.dao")
public class AppStarter {
	public static void main(String[] args) {
		SpringApplication.run(AppStarter.class);
	}
}
