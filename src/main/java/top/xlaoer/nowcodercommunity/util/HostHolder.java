package top.xlaoer.nowcodercommunity.util;

import org.springframework.stereotype.Component;
import top.xlaoer.nowcodercommunity.entity.User;

/**
 * @author Xlaoer
 * @date 2022/5/10 23:47
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }
}
