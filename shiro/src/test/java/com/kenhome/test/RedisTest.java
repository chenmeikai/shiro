/**   
 * Copyright © 2018 www.chenmeikai.com
 * @Package: DemoTest.java 
 * @author: Administrator   
 * @date: 2018年2月20日 上午11:15:30 
 */
package com.kenhome.test;

import com.kenhome.config.redis.RedisClient;
import com.kenhome.run.RunApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**   
 * @ClassName:  RedisTest   
 * @Description:TODO  
 * @author: cmk 
 */

@RunWith(SpringJUnit4ClassRunner.class)  
@SpringBootTest(classes=RunApplication.class)// 指定spring-boot的启动类
public class RedisTest {
	
	@Resource
	private RedisClient redisClient;
	
	@Test
	public void set() throws Exception {
		
		Map<String, Object> map =new HashMap<>();
		map.put("web_hits_", 0);
		map.put("ip_nums_", 0);
		
		redisClient.setObject("company", map);
	}
	
	
	@Test
	@Transactional //添加事务，自动回滚
	//@Rollback(false)，不想事务自动回滚，则添加此注解，默认为true
	public void get() throws Exception {
		redisClient.delete("login_192.168.1.101");
	}

}
