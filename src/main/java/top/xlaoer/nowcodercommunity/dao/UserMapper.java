package top.xlaoer.nowcodercommunity.dao;

import org.apache.ibatis.annotations.Mapper;
import top.xlaoer.nowcodercommunity.entity.User;

/**
 * @author Xlaoer
 * @date 2022/5/3 19:54
 */
@Mapper
public interface UserMapper {
    User selectById(int id);

    User selectByUserName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id, int status);

    int updateHeader(int id, String headerUrl);

    int updatePassword(int id, String password);
}
