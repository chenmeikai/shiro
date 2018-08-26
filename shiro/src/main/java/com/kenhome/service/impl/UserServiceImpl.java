
package com.kenhome.service.impl;


import com.kenhome.mapper.UserMapper;
import com.kenhome.model.User;
import com.kenhome.service.UserService;
import com.kenhome.utils.MD5;
import com.kenhome.utils.OrderUtils;
import com.kenhome.utils.response.BaseResponse;
import com.kenhome.utils.response.RspUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service("userService")
public class UserServiceImpl extends BaseServiceImpl<User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    public void setMapper() {
        super.setMapper(userMapper);
    }

    @Override
    public BaseResponse<Object> editBase(Long id, String password) {

        User user = userMapper.selectById(id);
        password = MD5.getIntance().start(password);
        user.setPassword(password);
        int updateCode = userMapper.update(user);
        if (updateCode != 1) {
            return RspUtil.error("更新失败");
        }
        return RspUtil.success("更新成功");
    }

    @Override
    public BaseResponse<Long> deleteById(Long id) {
        User user = userMapper.selectById(id);
        if (user.getState() == 1) {
            return RspUtil.error("抱歉，该用户不可删除！");
        }
        int deleteCode = userMapper.delById(id);
        if (deleteCode == 1) {
            return RspUtil.success("删除成功");
        }
        return RspUtil.error("删除失败");
    }


    @Override
    public BaseResponse<Long> saveByName(String userName, String password) {

        User user = new User();
        user.setUserName(userName);
        password = MD5.getIntance().start(password);
        user.setPassword(password);
        user.setCreateTime(new Date());
        user.setUserNo(OrderUtils.makeOrderNo());
        int insertCode = userMapper.save(user);
        if (insertCode != 1) {
            return RspUtil.error("新增用户失败");
        }
        return RspUtil.success("新增用户成功");
    }


    @Override
    public User getUserByName(String userName) {
        return userMapper.getUserByName(userName);
    }

    @Override
    public User getRolesAndAclsByName(String userName) {
        return userMapper.getRolesAndAclsByName(userName);
    }
}
