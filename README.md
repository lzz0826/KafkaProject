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
##### Follower 副本:
- 剩下的副本稱為 Follower，它們會從 Leader 副本中複製資料，保持資料同步。<br />
- Follower 副本不參與讀寫操作，只負責同步資料。<br />


## Kafka 生產數據 事務處理 確保原子性
![image](https://github.com/lzz0826/KafkaProject/blob/main/img/3.png)
### 事務性寫入
Kafka 的 Producer 可以啟動一個事務，在這個事務中，Producer 可以向多個 Topic 或 Partition 發送訊息，所有訊息要麼全部成功提交（commit），要麼全部回滾（abort）。這樣可以保證資料的一致性。<br />
消費者可以讀取來自多個 Partition 的資料，然後將處理後的結果再次寫入 Kafka。使用事務時，這些操作可以作為一個原子單元進行，保證「一次且僅一次（exactly-once）」的處理語義，避免重複或丟失資料。<br />
### 事務隔離級別
- Read Uncommitted：消費者可以讀取尚未提交的事務數據。<br />
- Read Committed：消費者只能讀取已提交的事務數據，未提交的資料對消費者是不可見的。<br />


## ACK（Acknowledgment 確認機制 確保可靠性
保證訊息可靠傳遞的機制，Producer 在向 Broker 發送訊息時，會根據設定的 ACK 值決定如何確認訊息是否成功寫入 Kafka 集群。這直接影響資料的可靠性和性能。<br />
### ACK 機制對應的可靠性與效能：
- acks=0：最高效能，最低可靠性。<br />
- acks=1：中等效能，較高可靠性。<br />
- acks=-1/all：最低效能，最高可靠性。<br />
#### acks = 0（無需確認）
- Producer 發送訊息後，不等待任何來自 Broker 的確認，無論訊息是否成功接收或儲存。<br />
- 特點：這種模式具有最低的延遲和最高的吞吐量，因為 Producer 不等待回應就繼續發送下一條訊息。<br />
- 風險：無法保證資料的可靠性。如果 Broker 發生故障，Producer 不會知道訊息是否丟失。<br />

#### acks = 1（Leader 確認）
- Producer 在收到 Leader 副本 的確認後，認為訊息已成功發送，不等待 Follower 副本的同步。<br />
- 特點：這種模式提供了平衡的可靠性和性能，Leader 副本確認訊息後就能告知 Producer 成功，延遲相對較低。<br />
- 風險：如果在 Leader 副本確認訊息後但在 Follower 副本同步之前，Leader 發生故障，可能會導致資料丟失。<br />

#### acks = -1 或 all（全副本確認）
- Producer 會等待所有的 ISR（In-Sync Replicas） 副本都確認收到訊息後，才認為訊息已成功發送。<br />
- 特點：這是最安全的模式，因為 Producer 會等待所有副本完成同步，確保資料已分散到多個節點，從而避免單一節點故障導致資料丟失。<br />
- 風險：延遲較高，因為要等到所有副本都完成確認才會返回成功訊息。<br />


## 幂等性（Idempotence）確保消息不重複
確保同一條訊息多次發送時，最終結果只會有一次成功的寫入。這在分散式系統中非常重要，特別是當系統發生錯誤或網絡中斷時，Producer 可能會重試發送訊息。幂等性確保即使訊息被多次重發，Kafka 也只會處理並儲存一次，避免重複數據。<br />
### 幂等 Producer：
- 在 Producer 端實現的。當 Producer 啟用幂等性時，它會自動處理重複訊息的問題，無需手動干預。<br />
- 通過追蹤每個 Producer 的每條訊息來實現幂等性。每個 Producer 都有一個唯一的 PID（Producer ID） 和每個 Partition 的 序列號（Sequence Number），這兩者結合來防止同一條訊息被多次處理。<br />
### 序列號（Sequence Number）：
- Kafka 為每一條訊息在每個 Partition 上分配一個遞增的序列號。Broker 會記錄收到的最新序列號，從而識別出是否有重複的訊息。當 Producer 發送一條訊息時，Kafka 會檢查該訊息的序列號是否大<br />
### 與ACK 機制的結合：
- 幂等性與 acks=all（或 acks=-1）結合使用效果最佳，因為這保證了所有同步副本都收到訊息。搭配幂等性，Kafka 可以保證「至少一次」的訊息傳遞語義提升到「恰好一次」（Exactly Once）的語義。<br />
### 與事務的關聯：
-  幂等性是實現 Kafka 事務性操作的一個基礎。當幂等 Producer 與 Kafka 的 事務性操作 結合時，Kafka 可以實現「一次且僅一次」（Exactly Once）的語義，不僅確保訊息不會重複，還能確保跨多個 Topic 和 Partition 的操作要麼全部成功，要麼全部失敗。<br />



## 消費數據
![image](https://github.com/lzz0826/KafkaProject/blob/main/img/4.png)
![image](https://github.com/lzz0826/KafkaProject/blob/main/img/5.png)
![image](https://github.com/lzz0826/KafkaProject/blob/main/img/6.png)
### 流程
![image](https://github.com/lzz0826/KafkaProject/blob/main/img/7.png)

## 選舉
![image](https://github.com/lzz0826/KafkaProject/blob/main/img/8.png)
