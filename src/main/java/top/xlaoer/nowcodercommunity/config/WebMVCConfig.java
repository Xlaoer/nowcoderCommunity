package top.xlaoer.nowcodercommunity.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.xlaoer.nowcodercommunity.dao.LoginTicketMapper;
import top.xlaoer.nowcodercommunity.interceptor.DataInterceptor;
import top.xlaoer.nowcodercommunity.interceptor.LoginRequiredInterceptor;
import top.xlaoer.nowcodercommunity.interceptor.LoginTicketInterceptor;
import top.xlaoer.nowcodercommunity.interceptor.MessageInterceptor;

/**
 * @author Xlaoer
 * @date 2022/5/10 23:23
 */
@Configuration
public class WebMVCConfig implements WebMvcConfigurer {

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

//    废弃登录拦截器
//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Autowired
    private DataInterceptor dataInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/static/css/*.css", "/static/js/*.js", "/static/img/*.png", "/static/img/*.jpg", "/static/img/*.jpeg");

//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/static/css/*.css", "/static/js/*.js", "/static/img/*.png", "/static/img/*.jpg", "/static/img/*.jpeg");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/static/css/*.css", "/static/js/*.js", "/static/img/*.png", "/static/img/*.jpg", "/static/img/*.jpeg");

        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/static/css/*.css", "/static/js/*.js", "/static/img/*.png", "/static/img/*.jpg", "/static/img/*.jpeg");
    }
}
