package top.xlaoer.nowcodercommunity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import top.xlaoer.nowcodercommunity.util.MailClient;

/**
 * @author Xlaoer
 * @date 2022/5/6 10:55
 */
@SpringBootTest
@ContextConfiguration(classes = NowcoderCommunityApplication.class)
public class EmailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${activation.domain}")
    private String domain;

    @Test
    public void testSendEmail(){
//        mailClient.sendEmail("1229230718@qq.com","123","<h1>test</h1>",true);
        System.out.println(domain);
    }

    @Test
    public void testProcess(){
        Context context = new Context();
        context.setVariable("email", "makjys@gmail.com");
        context.setVariable("url", domain + "/" + "123" + "/" + "123");

        String process = templateEngine.process("/mail/activation", context);
        System.out.println(process);
    }
}
