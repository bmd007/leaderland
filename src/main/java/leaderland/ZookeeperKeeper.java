package leaderland;

import org.apache.zookeeper.ZooKeeper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ZookeeperKeeper {

    @Bean
    public ZooKeeper zooKeeper() throws IOException {
        var connectionString = "localhost:2181";
        return new ZooKeeper(connectionString, 9999999, event -> System.out.println(event));
    }

}
