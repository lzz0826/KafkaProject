package com.kafka.springkafka.test.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KafkaConsumerTest {


    public static void main(String[] args) {


        //创建配置对象
        Map<String,Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());

        //消費者組 同個組視為一個消費者去消費 同一條消息只有組裡的一個消費者會消費到
        config.put(ConsumerConfig.GROUP_ID_CONFIG,"tony01");

        //分配策略 由Leader決定
        config.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG,"aaa"); //組裡的會員ID
        //輪詢策略
//        config.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());
        //範圍分配策略
//        config.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RangeAssignor.class.getName());
        //黏性分配策略(推薦)
        config.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, StickyAssignor.class.getName());


        //啟用默認提交 默認true 如果改成false會沒有偏移量 需要手動保存
//        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);

        //偏移量 Kafka默認從最新如果沒設置只會在開啟消費者後接收最新數據 從最早earliest  從最新latest
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest");

        //事務隔離級別 默認read_uncommitted
//        特性	    read_uncommitted	                         read_committed
//        读取消息	读取所有消息，包括未提交和已回滚的事务消息	         仅读取已提交的事务消息，忽略未提交或回滚的消息
//        性能	    更高，适用于高吞吐量的场景	                     较低，可能引入延迟，但保证数据的一致性
//        数据一致性	不保证，可能读取到不一致的数据	                 保证数据一致性，确保读取到的是最终确认的数据
//        适用场景	- 性能优先的系统                               - 金融交易系统
//                  - 内部监控和调试                               - 订单处理系统
        config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG,"read_committed");


        //创建消费者对象
        KafkaConsumer<String,String> consumer = new KafkaConsumer<>(config);
        //订阅主题
        consumer.subscribe(Collections.singletonList("test"));

        //自訂偏移量
//        setOffset(consumer, "test",2);

        //从Kafka的处提中获取数据  消费者 从中(拉取 *依照消费者能力)数据
        while (true){
            ConsumerRecords<String, String> poll = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> sr : poll) {
                String key = sr.key();
                String value = sr.value();
                System.out.println("消费者接收k:"+key+"v:"+value);
            }
            //手動保存偏移量 配合 config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);
//            consumer.commitAsync(); //異步提交
//            consumer.commitSync();  //同步提交

        }

        // 关闭消费者对象
//        consumer.close();
    }

    // 設置偏移量
    private static void setOffset(KafkaConsumer<String, String> consumer, String topicName,int offset) {
        long startTime = System.currentTimeMillis();
        long timeout = 5000;  // 5 秒超時
        boolean found = false;
        while (!found && (System.currentTimeMillis() - startTime) < timeout) {
            consumer.poll(Duration.ofMillis(100));
            // 獲取分區信息
            Set<TopicPartition> assignment = consumer.assignment();
            if (assignment != null && !assignment.isEmpty()) {
                for (TopicPartition topicPartition : assignment) {
                    // 如果找到了指定的主題
                    if (topicName.equals(topicPartition.topic())) {
                        consumer.seek(topicPartition,offset);
                        found = true;
                    }
                }
            }
        }
    }


}
