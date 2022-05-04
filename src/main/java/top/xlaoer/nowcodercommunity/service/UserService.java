package top.xlaoer.nowcodercommunity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.xlaoer.nowcodercommunity.dao.UserMapper;
import top.xlaoer.nowcodercommunity.entity.User;

/**
 * @author Xlaoer
 * @date 2022/5/3 20:25
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

}
