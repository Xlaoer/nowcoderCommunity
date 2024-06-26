package top.xlaoer.nowcodercommunity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class NowcoderCommunityApplication {

    @PostConstruct
    public void init(){
        //解决Netty启动冲突问题
        //看 Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public static void main(String[] args) {
        SpringApplication.run(NowcoderCommunityApplication.class, args);
    }

}
