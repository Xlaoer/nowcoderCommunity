package top.xlaoer.nowcodercommunity.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import top.xlaoer.nowcodercommunity.dao.UserMapper;
import top.xlaoer.nowcodercommunity.entity.User;
import top.xlaoer.nowcodercommunity.util.CommunityUtil;
import top.xlaoer.nowcodercommunity.util.MailClient;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Xlaoer
 * @date 2022/5/3 20:25
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Value("${activation.domain}")
    private String domain;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        HashMap<String, Object> map = new HashMap<>();
        //验证有效性
        if (user == null) {
            throw new IllegalArgumentException("user参数为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }
        if (userMapper.selectByUserName(user.getUsername()) != null) {
            map.put("usernameMsg", "该账号已存在");
            return map;
        }
        if (userMapper.selectByEmail(user.getEmail()) != null) {
            map.put("emailMsg", "该邮箱已注册");
            return map;
        }


        //注册
        user.setCreateTime(new Date());
        user.setSalt(CommunityUtil.generateUUID().substring(0, 6));
        user.setType(0);
        user.setStatus(0);
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setActivationCode(CommunityUtil.generateUUID());
        userMapper.insertUser(user);


        Context context = new Context();
        context.setVariable("email", user.getEmail());
        context.setVariable("url", domain + "/" + user.getId() + "/" + user.getActivationCode());

        String process = templateEngine.process("/mail/activation", context);
        mailClient.sendEmail(user.getEmail(), "激活帐号", process, true);

        return map;
    }

    public Map<String, Object> activation(int id, String code) {
        if(id<0||StringUtils.isBlank(code))return null;
        User user = userMapper.selectById(id);
        HashMap<String, Object> map = new HashMap<>();
        if (code.equals(user.getActivationCode())) {
            if (user.getStatus() == 0) {
                userMapper.updateStatus(id, 1);
                map.put("msg", "帐号激活成功！");
            } else {
                map.put("msg", "您的账号已激活，请勿重复激活！");
            }
        } else {
            map.put("msg", "激活码有误，请重新注册");
        }
        return map;
    }
}
