package com.kenhome.config.shiro;

import com.kenhome.config.shiro.cache.MyCacheManager;
import com.kenhome.mapper.AclMapper;
import com.kenhome.model.Acl;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.LinkedHashMap;
import java.util.List;


@Configuration
public class ShiroConfig {

    @Autowired
    private AclMapper aclMapper;

    private static final Logger log = LoggerFactory.getLogger(ShiroConfig.class);

    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(@Qualifier("securityManager") SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 必须设置 SecurityManager,Shiro的核心安全接口
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 配置登录的url，如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"
        shiroFilterFactoryBean.setLoginUrl("/manager/user/loginPage");
        // 未授权界面;配置不会被拦截的链接 顺序判断
        shiroFilterFactoryBean.setUnauthorizedUrl("/unauthorized"); ////这里设置403并不会起作用
        // 配置访问权限,权限控制map.Shiro连接约束配置,即过滤链的定义，
        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();

        //静态资源允许访问//登录页允许访问,一个URL可以配置多个Filter,使用逗号分隔,当设置多个过滤器时，全部验证通过，才视为通过,部分过滤器可指定参数，如perms，roles
        filterChainDefinitionMap.put("manager/user/login", "anon");
        filterChainDefinitionMap.put("/unauthorized", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/fonts/**", "anon");
        filterChainDefinitionMap.put("/img/**", "anon");
        filterChainDefinitionMap.put("/manager/user/**", "anon");
        //logout是shiro提供的过滤器
        filterChainDefinitionMap.put("manager/user/logout", "logout");

        /*从库中获取权限配置*/
        List<Acl> list = aclMapper.selectList(null);
        for (Acl acl : list) {
            if (!"javascript:;".equals(acl.getAclUrl())) {
                String perms = "perms[" + acl.getPermission() + "]";
                log.info("访问{}需要的权限:{}", acl.getAclUrl(), perms);
                filterChainDefinitionMap.put(acl.getAclUrl(), perms);
            }
        }

        filterChainDefinitionMap.put("/druid/**", "user");
        //其他资源都需要登录认证
        filterChainDefinitionMap.put("/**", "user");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        //shiro拦截器工厂类注入成功
        return shiroFilterFactoryBean;
    }


    @Bean
    public MyShiroRealm myShiroRealm() {
        MyShiroRealm myShiroRealm = new MyShiroRealm();
        return myShiroRealm;
    }

    //缓存管理器
    @Bean
    public MyCacheManager getRedisCacheManager() {
        MyCacheManager shiroCacheManager = new MyCacheManager();
        return shiroCacheManager;
    }

    /*cookie对象;会话Cookie模板 ,默认为: JSESSIONID 问题: 与SERVLET容器名冲突,重新定义为sid或rememberMe，自定义*/
    public SimpleCookie rememberMeCookie() {
        //这个参数是cookie的名称
        SimpleCookie cookie = new SimpleCookie("rememberMe");
        //<!-- 记住我cookie生效时间30天 ,单位秒;-->
        cookie.setHttpOnly(true);
        cookie.setMaxAge(86400);
        return cookie;
    }

    /*cookie管理对象;记住我功能,rememberMe管理器*/
    public CookieRememberMeManager rememberMeManager() {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie());
        //rememberMe cookie加密的密钥 建议每个项目都不一样 默认AES算法 密钥长度(128 256 512 位) //3AvVhmFLUs0KTA3Kprsdag==
        cookieRememberMeManager.setCipherKey(Base64.decode("8BvVhmFLUs0KTA3Kprsdag=="));
        return cookieRememberMeManager;
    }


    @Bean
    @DependsOn({"lifecycleBeanPostProcessor"})
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

    /*开启注解*/
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor
                = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }


    @Bean(name = "securityManager")
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // realm
        securityManager.setRealm(myShiroRealm());
        //记住我管理器
        securityManager.setRememberMeManager(rememberMeManager());
        // 自定义缓存管理器
        securityManager.setCacheManager(getRedisCacheManager());
        // 自定义session管理
//        securityManager.setSessionManager(sessionManager());
        return securityManager;
    }
}