package top.xlaoer.nowcodercommunity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = NowcoderCommunityApplication.class)
class NowcoderCommunityApplicationTests implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    void testApplicationContext(){
        System.out.println(applicationContext);
    }
}
