package top.xlaoer.nowcodercommunity.controller;

import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import top.xlaoer.nowcodercommunity.entity.User;
import top.xlaoer.nowcodercommunity.service.UserService;
import top.xlaoer.nowcodercommunity.util.CommunityConstant;
import top.xlaoer.nowcodercommunity.util.CommunityUtil;
import top.xlaoer.nowcodercommunity.util.RedisKeyUtil;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Xlaoer
 * @date 2022/5/6 11:46
 */
@Controller
public class LoginController implements CommunityConstant {

    private Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

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
        int result= userService.activation(id,code);
        //注册成功
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "帐号激活成功！");
            model.addAttribute("target", "/login");
        }else if(result == ACTIVATION_FAILURE){
            model.addAttribute("msg", "激活失败，您提供的激活码不正确！");
            model.addAttribute("target", "/index");
        }else{
            model.addAttribute("msg","您的账号已激活，请勿重复激活！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";


    }

//    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
//    public void getKaptcha(HttpServletResponse response, HttpSession session){
//        response.setContentType("image/png");
//        String text = kaptchaProducer.createText();
//        BufferedImage image = kaptchaProducer.createImage(text);
//        session.setAttribute("kaptcha",text);
//        try {
//            ServletOutputStream outputStream = response.getOutputStream();
//            ImageIO.write(image,"png",outputStream);
//        } catch (IOException e) {
//            logger.error("获取验证码出错"+e.getMessage());
//        }
//    }
    //redis重构验证码功能
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 因为还没有登陆，所以需要设置验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        // 将验证码存入Redis,并设置过期时间
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image,"png",outputStream);
        } catch (IOException e) {
            logger.error("获取验证码出错"+e.getMessage());
        }
    }

//    @RequestMapping(path = "/login",method = RequestMethod.POST)
//    public String login(String username, String password, String code, boolean rememberme,
//                      Model model,HttpSession session, HttpServletResponse response
//    ) {
//        // 检查验证码
//        String kaptcha = (String) session.getAttribute("kaptcha");
//        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
//            model.addAttribute("codeMsg", "验证码不正确！");
//            return "/site/login";
//        }
//
//        // 检查账号，密码
//        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
//        Map<String, Object> map = userService.login(username, password, expiredSeconds);
//        if (map.containsKey("ticket")) {
//            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
//            cookie.setPath(contextPath);
//            cookie.setMaxAge(expiredSeconds);
//            response.addCookie(cookie);
//            return "redirect:/index";
//        } else {
//            model.addAttribute("usernameMsg", map.get("usernameMsg"));
//            model.addAttribute("passwordMsg", map.get("passwordMsg"));
//            return "/site/login";
//        }
//    }
    //redis重构login，不再使用session，解决分布式session的问题
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,
                        Model model,HttpServletResponse response, @CookieValue("kaptchaOwner") String kaptchaOwner) {
        // 检查验证码
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }

        // 检查账号,密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket,HttpServletResponse response) {
        userService.logout(ticket);
        Cookie cookie = new Cookie("ticket","");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        // 清除数据
        SecurityContextHolder.clearContext();

        return "redirect:/login";
    }

    @RequestMapping(value = "/forget", method = RequestMethod.GET)
    public String getForgetPage() {
        return "/site/forget";
    }



    @RequestMapping(value = "/forgetMail", method = RequestMethod.POST)
    public String setForgetCode(String email,HttpSession session,Model model) {
        String verCode = CommunityUtil.generateUUID().substring(0,6);
        session.setAttribute(email,verCode);
        userService.sendForgetMail(email,verCode);
        return "/site/forget";
    }

    @RequestMapping(value = "/forget", method = RequestMethod.POST)
    public String forget(String email,String verifycode,String password,HttpSession session,Model model) {
        if(StringUtils.isBlank(email)){
            model.addAttribute("emailMsg","邮箱不能为空");
            return "/site/forget";
        }
        String code = (String) session.getAttribute(email);
        if(StringUtils.isBlank(verifycode)||!verifycode.equals(code)){
            model.addAttribute("codeMsg","验证码错误或为空");
            return "/site/forget";
        }

        User user = userService.selectByEmail(email);
        if(user==null){
            model.addAttribute("emailMsg","邮箱未注册");
            return "/site/forget";
        }
        userService.updatePassword(user.getId(),CommunityUtil.md5(password+user.getSalt()));
        return "/site/login";
    }
}
