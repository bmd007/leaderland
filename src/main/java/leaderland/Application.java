package leaderland;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    ZooKeeper zooKeeper;

    @Override
    public void run(String... args) throws Exception {
        ACL acl = new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE);
        byte[] bytes = "PUBLIC KEY 0".getBytes();
        var acls = new ArrayList<ACL>();
        acls.add(acl);

        if (zooKeeper.exists("/leader", false)==null) {
            zooKeeper.create("/leader", bytes, acls, CreateMode.PERSISTENT, (rc, path, ctx, name) -> {
                System.out.println("Path created:" + path);
            }, "CONTEXT 4");
        }
        if (zooKeeper.exists("/leader/election", false)==null) {
            zooKeeper.create("/leader/election", bytes, acls, CreateMode.PERSISTENT, (rc, path, ctx, name) -> {
                System.out.println("Path created:" + path);
            }, "CONTEXT 4");
        }
    }
}
