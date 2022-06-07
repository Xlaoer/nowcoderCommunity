package top.xlaoer.nowcodercommunity;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Xlaoer
 * @date 2022/6/7 15:14
 */

@SpringBootTest
@ContextConfiguration(classes = NowcoderCommunityApplication.class)
public class ThreadPoolTests {

    public static final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);

    //JDK普通线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    //JDK可执行定时任务的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    //Spring普通线程池
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    //Spring可执行定时任务的线程池
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    //main方法里启动线程，JVM的运行机制中，所有非守护线程都执行完毕后，JVM就会自动退出
    //junit方法启动线程，test方法运行在主线程中，外层函数执行完test等操作后执行System.exit来退出虚拟机，这个时候thread1和thread2可能还没执行完，就被销毁了（只要主线程结束完成，单元测试就会关闭
    //封装一下sleep方法，Junit测试方法
    private void sleep(long m){
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //1.JDK普通线程池
    @Test
    public void testExecutorService(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ExecutorService");
            }
        };

        for(int i=0;i<10;i++){
            executorService.submit(task);
        }

        sleep(10000);

    }

    //2.JDK定时任务线程池
    @Test
    public void testScheduledExecutorService(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ScheduledExecutorService");
            }
        };

        scheduledExecutorService.scheduleAtFixedRate(task,10,1, TimeUnit.SECONDS);
        sleep(30000);
    }

    //3.Spring普通线程池
    //Spring提供@Async注解修饰方法 从而调用该方法时会被异步调用
    @Test
    public void testThreadPoolTaskExecutor(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ThreadPoolTaskExecutor");
            }
        };

        for(int i=0;i<35;i++){
            taskExecutor.submit(task);
        }

        sleep(10000);

    }

    //4.Spring定时任务线程池
    //Spring提供@Scheduled注解修饰方法 来简化定时任务的使用
    @Test
    public void testThreadPoolTaskScheduler(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ThreadPoolTaskScheduler");
            }
        };

        Date startTime = new Date(System.currentTimeMillis()+10000);
        taskScheduler.scheduleAtFixedRate(task,startTime,1000);

        sleep(30000);
    }



}
