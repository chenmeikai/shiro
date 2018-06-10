/**   
 * Copyright © 2018 
 * @Package: MyShiroRealm.java 
 * @author: Administrator   
 * @date: 2018年6月10日 下午10:34:36 
 */
package com.kenhome.config.shiro;

import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meikai.shop.entity.TSystemAcl;
import com.meikai.shop.entity.TSystemManager;
import com.meikai.shop.entity.TSystemRole;

/**
 * @Description:授权来源
 * @author: cmk
 * @date: 2018年6月10日 下午10:34:36
 */
public class MyShiroRealm extends AuthorizingRealm {

	private static final Logger logger = LoggerFactory.getLogger(MyShiroRealm.class);

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

		TSystemManager testManager = (TSystemManager) principals.fromRealm(getName()).iterator().next();

		TSystemManager manager = managerService.getRolesAndAclsByName(testManager.getManagerName());
		
		System.out.println("昵称：" + manager.getNickname());

		HashSet<String> permissions = new HashSet<String>();

		if (manager != null) {

			System.out.println("已获得管理员");
			for (TSystemRole role : manager.getRoles()) {

				System.out.println("管理员角色是" + role.getRoleName());

				for (TSystemAcl acl : role.getAcls()) {
					System.out.println("权限是：" + acl.getPermission());
					permissions.add(acl.getPermission());
				}
			}
		}
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		info.addStringPermissions(permissions);
		return info;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

		String managerName = (String) token.getPrincipal();

		System.out.println("获得管理员名是" + managerName);

		TSystemManager manager = managerService.getRolesAndAclsByName(managerName);

		if (manager != null) {

			String password = manager.getPassword();

			AuthenticationInfo aInfo = new SimpleAuthenticationInfo(manager, password, getName());
			return aInfo;
		}

		return null;
	}

}
