package com.kafka.springkafka.test.admin;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminTopicTest {

    public static void main(String[] args) {

        Map<String,Object> config = new HashMap<>();

        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");

        //管理員對象
        Admin admin = Admin.create(config);

        //構建主題需要三個參數 kafka 默認算法分配
        String name = "k1";        //主題名
        int numPartitions = 1; //主題分區數量
        short replicationFactor = 1; //主題分區副本的 (因子) 數量
        NewTopic newTopic = new NewTopic(name,numPartitions,replicationFactor);

        String name2 = "k2";
        int numPartitions2 = 2;
        short replicationFactor2 = 2;
        NewTopic newTopic2 = new NewTopic(name2,numPartitions2,replicationFactor2);

        //自定分區策略 *須要對Kafka資原分配有一定了解 影想吞吐量
        String name3 = "k3";
        Map<Integer, List<Integer>> map = new HashMap<>();
        map.put(0,Arrays.asList(3,1)); // 分區0 兩個副本 放在BrokerId 3(master) , 1
        map.put(1,Arrays.asList(2,3)); // 分區1 兩個副本 放在BrokerId 2(master) , 3
        map.put(2,Arrays.asList(1,2)); // 分區2 兩個副本 放在BrokerId 1(master) , 2

        NewTopic newTopic3 = new NewTopic(name3,map);


        CreateTopicsResult topics = admin.createTopics(Arrays.asList(newTopic,newTopic2,newTopic3));


        // In - Sync -Replicas : 同步副本咧表(ISR)

        //關閉管理對象
        admin.close();


    }







}
