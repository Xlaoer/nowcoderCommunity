package top.xlaoer.nowcodercommunity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import top.xlaoer.nowcodercommunity.entity.User;
import top.xlaoer.nowcodercommunity.service.UserService;

import java.util.Map;

/**
 * @author Xlaoer
 * @date 2022/5/6 11:46
 */
@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        //注册成功
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，清尽快激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }
        //注册失败，携带参数返回
        else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    @RequestMapping(path = "/activation/{uid}/{code}", method = RequestMethod.GET)
    public String activation(Model model,@PathVariable("uid")int id,@PathVariable("code")String code) {
        Map<String, Object> map = userService.activation(id,code);
        //注册成功
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "参数错误");
        }else{
            model.addAttribute("msg", map.get("msg"));
        }
        model.addAttribute("target", "/index");
        return "/site/operate-result";


    }





}
