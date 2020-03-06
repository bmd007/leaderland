package leaderland;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;

@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        var connectionString = "localhost:2181";
        ZooKeeper zooKeeper = new ZooKeeper(connectionString, 9999999, event -> System.out.println(event));
        Thread.sleep(1000);

        ACL acl = new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE);
        byte[] bytes = "PUBLIC KEY 0".getBytes();
        var acls = new ArrayList<ACL>();
        acls.add(acl);
        zooKeeper.create("/ff", bytes, acls, CreateMode.PERSISTENT);
        zooKeeper.create("/ff/vv", bytes, acls, CreateMode.PERSISTENT);

        Thread.sleep(10000);
        try {
            zooKeeper.getChildren("/ff", System.out::println);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread.sleep(30000);
    }
}
