package top.xlaoer.nowcodercommunity;

import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Xlaoer
 * @date 2022/6/7 17:04
 */
@SpringBootTest
@ContextConfiguration(classes = NowcoderCommunityApplication.class)
public class QuartzTests {

    // 调度器
    @Autowired
    private Scheduler scheduler;

    // 删除任务
    @Test
    public void deleteJob() {
        try {
            boolean result = scheduler.deleteJob(new JobKey("alphaJob", "alphaJobGroup"));
            System.out.println(result);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
