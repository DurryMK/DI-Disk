package app.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import app.bean.User;

public interface UserMapper {

	@Select("select id,name,available,used,level,path from diskusers where name=#{name} and pwd=#{pwd}")
	User selectUser(@Param(value = "name") String name, @Param(value = "pwd") String pwd);

	@Select("select name from diskusers where name=#{name}")
	User selectUserWithName(@Param(value = "name") String name);
	
	@Insert("insert into diskusers(name,pwd,available,used,level,path) values(#{name},#{pwd},#{available},#{used},#{level},#{path}) ")
	void insertUser(User user);

}
