package com.kafka.springkafka.test.producer;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;


/*
* 字定義攔截際
* 1.實現 接口
* 2.定義泛行
* 3.重寫方法
* */
public class ValueInterceptorTest implements ProducerInterceptor<String,String> {
    @Override
    //發送數據的時候 會調用此方法
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
        String value = record.value() + "fix";

        return new ProducerRecord<String,String>(record.topic(),record.key(),value);
    }

    @Override
    //發送數據完畢 服務器返回的響應 會調用此方法
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {

    }

    @Override
    //生產者對象關閉的時候會調用此方法
    public void close() {

    }

    @Override
    //創建生產者對象的時候調用
    public void configure(Map<String, ?> configs) {

    }
}
