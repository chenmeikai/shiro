
package com.kenhome.controller;

import com.kenhome.config.redis.RedisClient;
import com.kenhome.model.User;
import com.kenhome.service.UserService;
import com.kenhome.utils.CreateImageCode;
import com.kenhome.utils.HttpClientUtil;
import com.kenhome.utils.MD5;
import com.kenhome.utils.response.BaseResponse;
import com.kenhome.utils.response.RspUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("manager/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisClient redisClient;

    private static String LOGIN = "login_";

    // 登录页
    @RequestMapping("loginPage")
    public String list(Map<String, Object> map) {

        return "back/user/loginPage";
    }

    /**
     * 登录
     *
     * @param request
     * @param password
     * @return
     * @throws IOException
     * @throws ServletException
     */
    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse<String> login(HttpServletRequest request, String userName, String password, String imgCode, Boolean remember)
            throws ServletException, IOException {

        String ip = HttpClientUtil.getCliectIp(request);

        try {
            Integer loginNum = (Integer) redisClient.getObject(LOGIN + ip);
            if (loginNum != null && loginNum > 4) {
                return RspUtil.error("抱歉，登陆失败次数超过5次，请1小时后重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 验证参数不可为空
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(password) || StringUtils.isBlank(imgCode)) {
            return RspUtil.error("必填参数不可为空");
        }

        String valiCode = (String) request.getSession().getAttribute("imgCode");
        if (!imgCode.equalsIgnoreCase(valiCode)) {
            return RspUtil.error("抱歉，验证码错误");
        }

        String mdPassword = MD5.getIntance().start(password);

        /*shiro校验*/
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(userName, mdPassword);
        Subject subject = SecurityUtils.getSubject();

        //记住我
        remember = remember == null ? false : remember;
        usernamePasswordToken.setRememberMe(remember);

        try {
            //完成登录
            subject.login(usernamePasswordToken);
            User user = (User) subject.getPrincipal();
            //TODO 更新登录时间
            request.getSession().setAttribute("user", user);
            request.getSession().removeAttribute(imgCode);

            String redirectUrl = "/manager/index";

            return RspUtil.success("登录成功，即将跳转到后台首页", redirectUrl);

        } catch (LockedAccountException e) {
            //登录校验失败，redis存储错误次数
            countWrong(ip);
            request.getSession().removeAttribute(imgCode);
            return RspUtil.error("抱歉，该用户已经被禁用");
        } catch (AuthenticationException se) {
            //登录校验失败，redis存储错误次数
            countWrong(ip);
            request.getSession().removeAttribute(imgCode);
            return RspUtil.error("抱歉，输入的用户名或者密码不正确");
        }

    }


    /**
     * 退出登录
     *
     * @return
     */
    @RequestMapping(value = "logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response) {

        //清空user
        request.getSession().removeAttribute("user");

        Subject subject = SecurityUtils.getSubject();

        subject.logout();

        try {
            response.sendRedirect("/manager/user/loginPage");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "back/user/loginPage";
    }


    @RequestMapping("imgCode")
    public void getCode(HttpServletRequest req, HttpServletResponse response, HttpSession session) throws IOException {
        // 设置响应的类型格式为图片格式
        response.setContentType("image/jpeg");
        //禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        CreateImageCode vCode = new CreateImageCode(100, 30, 5, 10);
        session.setAttribute("imgCode", vCode.getCode());
        vCode.write(response.getOutputStream());
    }


    /*累加登录失败次数*/
    void countWrong(String ip) {
        try {
            redisClient.increment(LOGIN + ip, 1);
            redisClient.expire(LOGIN + ip, 1L, TimeUnit.HOURS);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                redisClient.setObject(LOGIN + ip, 1, 1, TimeUnit.HOURS);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }


}
