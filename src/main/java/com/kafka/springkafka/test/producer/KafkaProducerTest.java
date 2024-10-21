package com.kafka.springkafka.test.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class KafkaProducerTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        //创建配置对象
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");

        //序列化
        configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //自訂攔截器 可以加工 發送的數據
//        configMap.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, ValueInterceptorTest.class.getName());

        //自訂分區管理器 可以自訂分區邏輯算法
//        configMap.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, MyKafkaPartitioner.class.getName());

        // 同步時ASKS
//        acks值	     描述	            可靠性	    延遲   	適用場景
//        0	             不等待任何確認	    最低	        最低	    高吞吐量需求、不敏感數據
//        1	             等待領導者副本確認	中等	        中等	    大多數應用、平衡延遲與可靠性
//        all/-1(默認)	 等待所有同步副本確認	最高	        最高     高可靠性需求、關鍵數據處理
//        configMap.put(ProducerConfig.ACKS_CONFIG, "0");


        //重試機制 開啟 幂等性 避免數據重試時數據重複或亂序
        configMap.put(ProducerConfig.ACKS_CONFIG, "-1");
        configMap.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // 启用幂等性 要求開啟重試和 acks all/-1
        configMap.put(ProducerConfig.RETRIES_CONFIG, 5);               // 设置重试次数
        configMap.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);  // 批次大小，设置为16KB
        configMap.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 3000); // 请求超时时间 毫秒

        //创建生产者对象
        //生产这对象需要设定范行 数据类型约束
        KafkaProducer<String,String> producer = new KafkaProducer<String,String>(configMap);



        for(int i = 0; i< 10 ; i++){
            //创建数据
            //topic : 主题明称
            //key
            //value
            ProducerRecord<String,String> record = new ProducerRecord<>(
                    "test",
                    "key"+ i,
                    "calue" + i
            );

            //異步發送
            asynchronousSend(producer,record);

            //同步發送 需要等到收到回調
//            syncSend(producer,record);

            //通过生产者对象将数据发送到Kafka
//            producer.send(record);

        }

        //关闭生产者对象
        producer.close();
    }


    //同步發送 需要等到收到回調
    private static void syncSend(KafkaProducer producer, ProducerRecord record) throws ExecutionException, InterruptedException, TimeoutException {
        Future<RecordMetadata> send = producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                System.out.println("同步數據發送成功:" + metadata);
            }
        });
        send.get(1, TimeUnit.SECONDS);
    }


    //異步發送
    private static void asynchronousSend(KafkaProducer producer, ProducerRecord record){
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                System.out.println("異步數據發送成功:"+metadata);
            }
        });

    }
}

