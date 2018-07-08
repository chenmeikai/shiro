/**
 * Copyright © 2018
 *
 * @Package: MyShiroRealm.java
 * @author: Administrator
 * @date: 2018年6月10日 下午10:34:36
 */
package com.kenhome.config.shiro;

import com.kenhome.mapper.UserMapper;
import com.kenhome.model.Acl;
import com.kenhome.model.Role;
import com.kenhome.model.User;
import com.kenhome.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;

/**
 * @Description:授权来源
 * @author: cmk
 * @date: 2018年6月10日 下午10:34:36
 */

public class MyShiroRealm extends AuthorizingRealm {

    private static final Logger logger = LoggerFactory.getLogger(MyShiroRealm.class);

    @Autowired
    UserMapper userMapper;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        User validateManager = (User) SecurityUtils.getSubject().getPrincipal();

        /*TODO 缓存权限*/
        User manager = userMapper.getRolesAndAclsByName(validateManager.getUserName());

        HashSet<String> permissions = new HashSet<String>();

        if (manager != null) {

            for (Role role : manager.getRoles()) {

                logger.info("{}拥有的角色是{}",manager.getUserName(),role.getRoleName());

                for (Acl acl : role.getAcls()) {
                    logger.info("{}拥有的权限是：{}",manager.getUserName(),acl.getPermission());
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

        String password = new String((char[]) token.getCredentials());

        logger.info("校验的身份是{}",managerName);

        /*TODO 缓存用户*/
        User manager = userMapper.getUserByName(managerName);

        if (manager == null) {
            throw new UnknownAccountException("用户名或密码错误！");
        }
        if (!password.equals(manager.getPassword())) {
            throw new IncorrectCredentialsException("用户名或密码错误！");
        }
        if ("1".equals(manager.getState())) {
            throw new LockedAccountException("账号已被禁用！");
        }

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(manager, password, getName());

        return info;
    }

}
