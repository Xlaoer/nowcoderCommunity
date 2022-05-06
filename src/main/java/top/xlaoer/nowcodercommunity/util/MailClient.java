package top.xlaoer.nowcodercommunity.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author Xlaoer
 * @date 2022/5/6 10:43
 */

@Component
public class MailClient{
    public static final Logger logger = LoggerFactory.getLogger(MailClient.class);
    @Autowired
    private JavaMailSender emailSender;


    @Value("${spring.mail.username}")
    private String from;

    public void sendEmail(
            String to, String subject, String content,boolean isHTML) {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage);

        try {
            message.setText(content,isHTML);
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            emailSender.send(message.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败："+e.getMessage());
        }

    }

}
