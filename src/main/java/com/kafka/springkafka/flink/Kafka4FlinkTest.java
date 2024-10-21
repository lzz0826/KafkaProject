package com.kafka.springkafka.flink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class Kafka4FlinkTest {
    public static void main(String[] args) throws Exception {

        // 創建執行環境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 配置 Kafka 消費者屬性
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092") // 設置 Kafka 服務器地址
                .setTopics("FlinkTest")                // 設置要消費的 Kafka 主題
                .setGroupId("flink")                   // 設置消費者組
                .setStartingOffsets(OffsetsInitializer.latest())    // 從最新的偏移量開始消費
                .setValueOnlyDeserializer(new SimpleStringSchema()) // 設置反序列化器
                .build();


        // 從 Kafka 中讀取數據源
        // 使用 KafkaSource 來設置 Kafka 的屬性，並指定從 Kafka 主題 "FlinkTest" 讀取數據
        // WatermarkStrategy.noWatermarks() 表示不使用水印策略，因為這裡不需要基於事件時間的處理
        // "kafka-source" 是該數據源的名稱，方便在 Flink 的 Web UI 中查看和追蹤
        DataStreamSource<String> stream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "kafka-source");
//        stream.print("Kafka");

        // 詞頻統計（將每一行數據按空格切割，統計每個詞出現的次數）
        DataStream<String> wordCounts = stream
                .flatMap(new Tokenizer())
                .keyBy(value -> value.f0)
                .sum(1)
                .map(value -> value.f0 + ": " + value.f1);

        // 將結果打印到控制台
        wordCounts.print("Kafka");

        // 執行 Flink 作業
        env.execute();
    }
}