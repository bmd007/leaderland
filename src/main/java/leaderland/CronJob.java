package leaderland;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.JsonValueSerializer;
import it.sauronsoftware.cron4j.Scheduler;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class CronJob {

    @Autowired ZooKeeper zooKeeper;

    @Value("${server.port}") int port;

    @Value("${spring.application.name}") String applicationName;

//    @Autowired RSAPublicKey publicKey;

    @Autowired
    ObjectMapper objectMapper;

    private Runnable becomeCandidateForElection = () -> {
        try {
            var publicKey =  CryptographyUtil.publicKeyOf(CryptographyUtil.generateRSAKeyPair());
            var nodeData = new NodeData(applicationName, new String(CryptographyUtil.publicKeyToPem(publicKey)));
            var jsonNodeData = objectMapper.writeValueAsString(nodeData);

            var random = Math.abs(new Random().nextInt()*100)%1000;
            System.out.println("will wait:"+ random);
            Thread.sleep(random);

            ACL acl = new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE);
            byte[] bytes = jsonNodeData.getBytes();
            var acls = new ArrayList<ACL>();
            acls.add(acl);

            zooKeeper.create("/leader/election/", bytes, acls, CreateMode.EPHEMERAL_SEQUENTIAL, (rc, path, ctx, name) -> {
                System.out.println("Path created:"+ path);
            }, "CONTEXT 4");

        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private Runnable checkCandidates = () -> {
        zooKeeper.getChildren("/leader/election",true, (rc, path, ctx, children) -> {
            var collect = children.stream()
                    .sorted((o1, o2) -> {
                        var int1 = Integer.valueOf(o1);
                        var int2 = Integer.valueOf(o2);
                        return int1.compareTo(int2);
                    })
                    .collect(Collectors.toList());
            System.out.println(collect);
            try {
                byte[] data = zooKeeper.getData(path +"/"+ collect.get(0), false, null);
                var jsonNodeData = new String(data);
                var nodeData = objectMapper.reader().forType(NodeData.class).readValue(jsonNodeData);
                System.out.println(nodeData);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }, "CONTEXT 3");
    };

    private Scheduler registerSelfScheduler = new Scheduler();
    private Scheduler checkCandidatesScheduler = new Scheduler();

    public CronJob(){
        registerSelfScheduler.schedule("52 12 * * *", becomeCandidateForElection);
        registerSelfScheduler.start();
        checkCandidatesScheduler.schedule("53 12 * * *", checkCandidates);
        checkCandidatesScheduler.start();
    }
}
