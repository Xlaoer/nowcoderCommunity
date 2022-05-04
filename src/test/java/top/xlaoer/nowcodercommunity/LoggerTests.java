package top.xlaoer.nowcodercommunity;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Xlaoer
 * @date 2022/5/4 13:45
 */
@SpringBootTest
@ContextConfiguration(classes = NowcoderCommunityApplication.class)
public class LoggerTests {

    public static final Logger logger = LoggerFactory.getLogger(LoggerTests.class);

    @Test
    public void test(){
        logger.debug("debug log");
        logger.info("info log");
        logger.error("error log");
        logger.warn("warn log");
    }
}
