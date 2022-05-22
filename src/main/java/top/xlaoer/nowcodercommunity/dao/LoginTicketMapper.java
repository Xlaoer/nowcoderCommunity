package top.xlaoer.nowcodercommunity.dao;

import org.apache.ibatis.annotations.*;
import top.xlaoer.nowcodercommunity.entity.LoginTicket;

/**
 * @author Xlaoer
 * @date 2022/5/9 16:40
 */
@Mapper
@Deprecated //废弃注解
public interface LoginTicketMapper {
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id") // 声明Id为自动生成
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    // 如果要在注解里面写动态SQL的话，例如说写if标签，需要在里面加上script标签
    @Update({
            "<script>",
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "<if test=\"ticket!=null\">",
            "and 1=1 ",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket, int status);
}
