package leaderland;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.sauronsoftware.cron4j.Scheduler;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
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
            var random = Math.abs(new Random().nextInt()*100)%10000;

            var publicKey =  CryptographyUtil.publicKeyOf(CryptographyUtil.generateRSAKeyPair());
            var b64pem = Base64.getEncoder().encodeToString(CryptographyUtil.publicKeyToPem(publicKey));
            var nodeData = new NodeData(applicationName+random, b64pem);
            var jsonNodeData = objectMapper.writeValueAsString(nodeData);

            Thread.sleep(random);
            System.out.println("waited "+ random + " mss and now time is: "+ LocalTime.now());

            ACL acl = new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE);
            byte[] bytes = jsonNodeData.getBytes();
            var acls = new ArrayList<ACL>();
            acls.add(acl);

            zooKeeper.create("/leader/election/", bytes, acls, CreateMode.EPHEMERAL_SEQUENTIAL, (rc, path, ctx, name) -> {
                System.out.println("Path created:"+ path +" with name "+ name +" for application"+random);
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
                        return int2.compareTo(int1);
                    })
                    .collect(Collectors.toList());
            System.out.println(collect);
            try {
                byte[] data = zooKeeper.getData(path +"/"+ collect.get(0), false, null);
                var jsonNodeData = new String(data);
                var nodeData = objectMapper.reader().forType(NodeData.class).readValue(jsonNodeData);
                System.out.println(collect.get(0)+ " as [0] resulted in "+ nodeData);

                byte[] data2 = zooKeeper.getData(path +"/"+ collect.get(collect.size()-1), false, null);
                var jsonNodeData2 = new String(data2);
                var nodeData2 = objectMapper.reader().forType(NodeData.class).readValue(jsonNodeData2);
                System.out.println(collect.get(collect.size()-1)+ " as [last] resulted in "+ nodeData2);
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
        registerSelfScheduler.schedule("55 21 * * *", becomeCandidateForElection);
        registerSelfScheduler.start();
        checkCandidatesScheduler.schedule("57 21 * * *", checkCandidates);
        checkCandidatesScheduler.start();
    }
}
