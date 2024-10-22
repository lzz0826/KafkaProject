# Kafka
Spring boot 集成 <br />
Fllink <br />
Spark <br />

## 核心概念包括：
- Producer：產生並發送訊息到 Kafka topic。 <br />
- Consumer：從 Kafka topic 中讀取訊息。 <br />
- Broker：Kafka 的伺服器，負責儲存和分發訊息。 <br />
- Topic：訊息分類的邏輯單位。 <br />



## 架構:
![image](https://github.com/lzz0826/KafkaProject/blob/main/img/1.png)
![image](https://github.com/lzz0826/KafkaProject/blob/main/img/2.png)
#### Partition（分區）:  每個 Topic 的邏輯子單位，負責提高併發性和擴展性。每個 Topic 可以被劃分成多個 Partition，資料會分散儲存在這些 Partition 中，並平行處理訊息流
- 提高併發性：Producer 和 Consumer 可以同時對不同的 Partition 進行讀寫操作，提升效能。<br />
- 資料分散：資料可以根據某個 key（如用戶 ID）被分配到特定的 Partition，確保同一 key 的訊息會順序儲存在同一個 Partition 中。<br />
- 水平擴展：通過增加 Partition 數量，可以輕鬆增加 Topic 的容量和處理能力。<br />
- 容錯性：每個 Partition 都可以有多個副本（replica），如果某個 Broker 發生故障，其他 Broker 上的副本會接管。<br />

#### Replica（副本）:  每個 Partition 可以有多個副本，每個副本儲存在不同的 Broker 上，確保即使某個 Broker 發生故障，資料依然可用。
- 當 Leader 副本發生故障時，Kafka 的控制器會從 Follower 副本中選出一個新的 Leader，確保服務不中斷。<br />
- Kafka 的 ISR（In-Sync Replicas）機制確保只有與 Leader 同步的副本才有資格成為新 Leader，這保證了資料一致性。<br />
- 副本的數量可以根據需求設定，一般設定為 3（1 個 Leader + 2 個 Follower），這樣可以提供冗餘和容錯。<br />
##### Leader 副本:
- 每個 Partition 有一個主要副本稱為 Leader，負責處理所有的讀寫請求。<br />
- Producer 和 Consumer 只與這個 Leader 副本進行交互。<br />
##### ollower 副本:
- 剩下的副本稱為 Follower，它們會從 Leader 副本中複製資料，保持資料同步。<br />
- Follower 副本不參與讀寫操作，只負責同步資料。<br />


## Kafka 生產數據 事務處理
![image](https://github.com/lzz0826/KafkaProject/blob/main/img/3.png)

## 消費數據
![image](https://github.com/lzz0826/KafkaProject/blob/main/img/4.png)
![image](https://github.com/lzz0826/KafkaProject/blob/main/img/5.png)
![image](https://github.com/lzz0826/KafkaProject/blob/main/img/6.png)
### 流程
![image](https://github.com/lzz0826/KafkaProject/blob/main/img/7.png)

## 選舉
![image](https://github.com/lzz0826/KafkaProject/blob/main/img/8.png)
