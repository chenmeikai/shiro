
package com.kenhome.mapper;


import com.kenhome.model.User;
import org.springframework.stereotype.Repository;


@Repository
public interface UserMapper extends BaseMapper<User> {


    public User getUserByName(String userName);


    public User getRolesAndAclsByName(String userName);

}
