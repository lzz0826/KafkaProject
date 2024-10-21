package com.kafka.springkafka.test.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class KafkaProducerTransactionTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        //创建配置对象
        Map<String, Object> configMap = new HashMap<>();
        configMap.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");

        //序列化
        configMap.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configMap.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());


        //重試機制 開啟 幂等性 避免數據重試時數據重複或亂序
        configMap.put(ProducerConfig.ACKS_CONFIG, "-1");
        configMap.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // 启用幂等性 要求開啟重試和 acks all/-1
        configMap.put(ProducerConfig.RETRIES_CONFIG, 5);               // 设置重试次数
        configMap.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);  // 批次大小，设置为16KB
        configMap.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 3000); // 请求超时时间 毫秒
        // 事務ID 解決跨繪畫幂等性問題 (**無法解決跨分區) 基於 性操作
        configMap.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my-tx-id");

        //创建生产者对象
        //生产这对象需要设定范行 数据类型约束
        KafkaProducer<String,String> producer = new KafkaProducer<String,String>(configMap);

        //初始化事務
        producer.initTransactions();
        try {

            //開啟事務
            producer.beginTransaction();


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
                // 异步发送消息
                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        // 处理发送失败
                        exception.printStackTrace();
                    } else {
                        System.out.println("消息发送成功: " + metadata);
                    }
                });
//                //同步發送 需要等到收到回調
//                Future<RecordMetadata> send = producer.send(record, new Callback() {
//                    @Override
//                    public void onCompletion(RecordMetadata metadata, Exception exception) {
//                        System.out.println("同步數據發送成功:" + metadata);
//                    }
//                });
//                send.get(1, TimeUnit.SECONDS);
            }

            //提交事務
            producer.commitTransaction();

        }catch (Exception e){
            e.printStackTrace();
            //終止事務
            producer.abortTransaction();

        }finally {
            //关闭生产者对象
            producer.close();
        }



    }

}

