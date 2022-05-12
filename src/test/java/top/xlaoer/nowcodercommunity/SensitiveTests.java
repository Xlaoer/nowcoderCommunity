package top.xlaoer.nowcodercommunity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import top.xlaoer.nowcodercommunity.util.SensitiveFilter;

/**
 * @author Xlaoer
 * @date 2022/5/12 15:23
 */
@SpringBootTest
@ContextConfiguration(classes = NowcoderCommunityApplication.class)
public class SensitiveTests {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void test(){
        String origin = "不要❤嫖❤娼❤，不要❤赌❤博❤，不要❤吸❤毒❤";
        System.out.println(sensitiveFilter.filter(origin));
    }
}
