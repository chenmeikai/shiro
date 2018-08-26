
package com.kenhome.service;


import com.kenhome.model.User;
import com.kenhome.utils.response.BaseResponse;

public interface UserService extends BaseService<User> {


    /**
     * 新增
     *
     * @param userName
     * @param password
     * @return
     */
    public BaseResponse<Long> saveByName(String userName, String password);


    /**
     * 编辑基本信息
     *
     * @param id
     * @param password
     * @return
     */
    public BaseResponse<Object> editBase(Long id, String password);


    /**
     * 删除
     *
     * @param id
     * @return
     */
    public BaseResponse<Long> deleteById(Long id);


    public User getUserByName(String userName);


    public User getRolesAndAclsByName(String userName);


}
