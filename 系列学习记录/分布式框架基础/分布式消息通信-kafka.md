### 一、kafka 的安装部署

#### 1.1 下载安装 包

https://www.apache.org/dyn/closer.cgi?path=/kafka/1.1.0/kafka_2.11-1.1.0.tgz

#### 1.2 安装过程

tar -zxvf 解压安装包

##### 1.2.1 kafka 目录介

- /bin 操作 kafka 的可执行脚本

- /config 配置文件

- /libs 依赖库目录
- /logs 日志数据目录

#### 1.3 启动/ / 停止 kafka a

- 需要先启动 zookeeper，如果没有搭建 zookeeper 环境，可以直接运行kafka 内嵌的 zookeeper

**启动命令： bin/zookeeper-server-start.sh config/zookeeper.properties &**

- 进入 kafka 目录，运行 bin/kafka-server-start.sh ｛-daemon 后台启动｝

**config/server.properties &**

- 进入 kafka 目录，运行 bin/kafka-server-stop.sh config/server.properties

### 二、Kafka 的基本操作

#### 2.1 创建topic

./kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test

**注意：**Replication-factor 表示该 topic 需要在不同的 broker 中保存几份，这里设置成 1，表示在两个 broker 中保存两份

​			Partitions 分区数

#### 2.2 查看 topic

./kafka-topics.sh --list --zookeeper localhost:2181

#### 2.3 查看 topic 属性

./kafka-topics.sh --describe --zookeeper localhost:2181 --topic test

#### 2.4 消费消息

./kafka-console-consumer.sh –bootstrap-server localhost:9092 --topic test --from-beginning

#### 2.5 发送消息

./kafka-console-producer.sh --broker-list localhost:9092 --topic test

### 三、安装集群环境

#### 3.1 修改 s server.properties 配置

- 修改 server.properties. broker.id=0 / 1
- 修改 server.properties 修改成本机 IP ，advertised.listeners=PLAINTEXT://192.168.11.153:9092

当 Kafka broker 启动时，它会在 ZK 上注册自己的 IP 和端口号，客户端就通过这个 IP 和端口号来连接

### 四、配置信息分析

#### 4.1 发送端的可选配置信息分析

##### 4.1.1 acks

acks 配置表示 producer 发送消息到 broker 上以后的确认值。有三个可选项

- 0：表示 producer 不需要等待 broker 的消息确认。这个选项时延最小但同时风险最大（因为当 server 宕机时，数据将会丢失）。
- 1：表示 producer 只需要获得 kafka 集群中的 leader 节点确认即可，这个选择时延较小同时确保了 leader 节点确认接收成功。
- all(-1)：需要 ISR 中所有的 Replica 给予接收确认，速度最慢，安全性最高，但是由于 ISR 可能会缩小到仅包含一个 Replica，所以设置参数为 all 并不能一定避免数据丢失，

##### 4.1.2 batch.size

​		生产者发送多个消息到 broker 上的同一个分区时，为了减少网络请求带来的性能开销，通过批量的方式来提交消息，可以通过这个参数来控制批量提交的字节数大小，默认大小是 16384byte,也就是 16kb，意味着当一批消息大小达到指定的 batch.size 的时候会统一发送。

##### 4.1.3 linger.ms

​		Producer 默认会把两次发送时间间隔内收集到的所有 Requests 进行一次聚合然后再发送，以此提高吞吐量，而 linger.ms 就是为每次发送到 broker 的请求增加一些 delay，以此来聚合更多的 Message 请求。 这个有点想 TCP 里面的Nagle 算法，在 TCP 协议的传输中，为了减少大量小数据包的发送，采用了Nagle 算法，也就是基于小包的等-停协议

**注意：**batch.size 和 linger.ms 这两个参数是 kafka 性能优化的关键参数，很多同学会发现 batch.size 和 linger.ms 这两者的作用是一样的，如果两个都配置了，那么怎么工作的呢？实际上，当二者都配置的时候，只要满足其中一个要求，就会发送请求到 broker 上

##### 4.1.3 max.request.size

设置请求的数据的最大字节数，为了防止发生较大的数据包影响到吞吐量，默认值为 1MB。



#### 4.2 消费端的可选配置分析

##### 4.2.1 group.id 

​			consumer group 是 kafka 提供的可扩展且具有容错性的消费者机制。

​			既然是一个组，那么组内必然可以有多个消费者或消费者实例(consumer instance)，它们共享一个公共的 ID，即 group ID。组内的所有消费者协调在一起来消费订阅主题(subscribed topics)的所有分区(partition)。当然，每个分区只能由同一个消费组内的一个 consumer 来消费.

如下图所示，分别有三个消费者，属于两个不同的 group，那么对于 firstTopic 这个 topic 来说，这两个组的消费者都能同时消费这个 topic 中的消息，对于此事的架构来说，这个 firstTopic 就类似于 ActiveMQ 中的 topic 概念。如右图所示，如果 3 个消费者都属于同一个group，那么此事 firstTopic 就是一个 Queue 的概念

![1602486494153](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602486494153.png)

##### 4.2.2 enable.auto.commit

​		消费者消费消息以后自动提交，只有当消息提交以后，该消息才不会被再次接收到，还可以配合 auto.commit.interval.ms 控制自动提交的频率。当然，我们也可以通过 consumer.commitSync()的方式实现手动提交

##### 4.2.3 auto.offset.reset

​		这个参数是针对新的 groupid 中的消费者而言的，当有新 groupid 的消费者来消费指定的 topic 时，对于该参数的配置，会有不同的语义

auto.offset.reset=latest 情况下，新的消费者将会从其他消费者最后消费的offset 处开始消费 Topic 下的消息

auto.offset.reset= earliest 情况下，新的消费者会从该 topic 最早的消息开始消费

auto.offset.reset=none 情况下，新的消费者加入以后，由于之前不存在offset，则会直接抛出异常。

##### 4.2.4 max.poll.records

​		此设置限制每次调用 poll 返回的消息数，这样可以更容易的预测每次 poll 间隔要处理的最大值。通过调整此值，可以减少 poll 间隔

### 五、Topic 和 Partition

#### 5.1 topic

​			在 kafka 中，topic 是一个存储消息的逻辑概念，可以认为是一个消息集合。每条消息发送到 kafka 集群的消息都有一个类别。物理上来说，不同的 topic 的消息是分开存储的

​			每个 topic 可以有多个生产者向它发送消息，也可以有多个消费者去消费其中的消息。

![1602503397116](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602503397116.png)

#### 5.2 Partiton

​			每个 topic 可以划分多个分区（每个 Topic 至少有一个分区），同一 topic 下的不同分区包含的消息是不同的。每个消息在被添加到分区时，都会被分配一个 offset（称之为偏移量），它是消息在此分区中的唯一编号，kafka 通过 offset保证消息在分区内的顺序，offset的顺序不跨分区，即kafka只保证在同一个分区内的消息是有序的。

下图中，对于名字为 test 的 topic，做了 3 个分区，分别是p0、p1、p2.

![1602503662234](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602503662234.png)

- 每一条消息发送到 broker 时，会根据 partition 的规则选择存储到哪一个 partition。如果 partition 规则设置合理，那么所有的消息会均匀的分布在不同的partition中，这样就有点类似数据库的分库分表的概念，把数据做了分片处理。

#### 5.3 Topic&Partition 的存储

​		./kafka-topics.sh --create --zookeeper192.168.11.156:2181 --replication-factor 1 --partitions 3 --topic firstTopic

​		Partition 是以文件的形式存储在文件系统中，比如创建一个名为 firstTopic 的 topic，其中有 3 个 partition，那么在kafka 的数据目录（/tmp/kafka-log）中就有 3 个目录，firstTopic-0~3， 命名规则是<topic_name>-<partition_id>

### 六、关于消息分发

#### 6.1 kafka 消息分发策略

​			消息是 kafka 中最基本的数据单元，在 kafka 中，一条消息由 key、value 两部分构成，在发送一条消息时，我们可以指定这个 key，那么 producer 会根据 key 和 partition 机制来判断当前这条消息应该发送并存储到哪个partition中。我们可以根据需要进行扩展 producer 的 partition 机制。

```java
   @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        //获得分区列表
        List<PartitionInfo> partitionInfos=cluster.partitionsForTopic(topic);
        int partitionNum=0;
        if(key==null){
            partitionNum=random.nextInt(partitionInfos.size()); //随机分区
        }else{
            partitionNum=Math.abs((key.hashCode())%partitionInfos.size());
        }
        System.out.println("key ->"+key+"->value->"+value+"->"+partitionNum);
        return partitionNum;  //指定发送的分区值
    }
```

#### 6.2 消息默认的分发机制

​			默认情况下，kafka 采用的是 hash 取模的分区算法。如果Key 为 null，则会随机分配一个分区。如果Key 

为 null，则会随机分配一个分区。这个随机是在这个参数”metadata.max.age.ms”的时间范围内随机选择一个。

对于这个时间段内，如果 key 为 null，则只会发送到唯一的分区。这个值默认情况下是 10 分钟更新一次。

#### 6.3 Metadata

##### 6.3.1 什么是Metadata？

​		简单理解就是Topic/Partition 和 broker 的映射关系，每一个 topic 的每一个 partition，需要知道对应的 broker 列表是什么，leader是谁、follower 是谁。这些信息都是存储在 Metadata

##### 6.3.2 Metadata的2种更新机制

- 周期性的更新: 每隔一段时间更新一次。这个通过 Metadata的lastRefreshMs, lastSuccessfulRefreshMs 这2个字段来实现。对应的ProducerConfig配置项为：
  - metadata.max.age.ms //缺省300000，即10分钟1次
- 失效检测，强制更新：检查到metadata失效以后，调用`metadata.requestUpdate()`强制更新。 requestUpdate()函数里面其实什么都没做，就是把needUpdate置成了false
  - **消费者组新增消费者**
  - **消费者组中某些消费者异常**

#### 6.4 消费端如何消费指定的分区

通过下面的代码，就可以消费指定该 topic 下的 0 号分区。其他分区的数据就无法接收

```java
// 消费指定分区的时候，不需要再订阅
//kafkaConsumer.subscribe(Collections.singletonList(topic));
// 消费指定的分区
TopicPartition topicPartition=newTopicPartition(topic,0);
kafkaConsumer.assign(Arrays.asList(topicPartition));
```

### 七、消息的消费原理

#### 7.1 kafka 消息消费原理演示

在实际生产过程中，每个 topic 都会有多个 partitions，

好处：

- 一方面能够对 broker 上的数据进行分片有效减少了消息的容量从而提升 io 性能。
- 为了提高消费端的消费能力，一般会通过多个consumer 去消费同一个 topic ，也就是消费端的负载均衡机制



​			多个 partition 以及多个 consumer 的情况下，消费者是如何消费消息的，kafka 存在 consumer group的概念，也就是 group.id 一样的 consumer，这些consumer 属于一个 consumer group，组内的所有消费者协调在一起来消费订阅主题的所有分区。当然每一个分区只能由同一个消费组内的 consumer 来消费，那么同一个consumer group 里面的 consumer 是怎么去分配该消费哪个分区里的数据的呢？

如下图所示，3 个分区，3 个消费者，那么哪个消费者消分哪个分区？

![1602559574985](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602559574985.png)

 如图：3 个消费者会分别消费 test 这个topic 的 3 个分区，也就是每个 consumer 消费一个partition。

#### 7.2 什么是分区分配策略

​		通过前面的案例演示，我们应该能猜到，同一个 group 中的消费者对于一个 topic 中的多个 partition，存在一定的分区分配策略。

​		在 kafka 中，存在两种分区分配策略，一种是 Range(默认)、另 一 种 另 一 种 还 是 RoundRobin （ 轮 询 ）。 通 过partition.assignment.strategy 这个参数来设置。

##### 7.2.1 Range strategy（范围分区）

​			Range 策略是对每个主题而言的，首先对同一个主题里面的分区按照序号进行排序，并对消费者按照字母顺序进行排序。

例如：

​			有 10 个分区，3 个消费者，排完序的分区将会是 0, 1, 2, 3, 4, 5, 6, 7, 8, 9；消费者线程排完序将会是C1-0, C2-0, C3-0。然后将 partitions 的个数除于消费者线程的总数来决定每个消费者线程消费几个分区。如果除不尽，那么前面几个消费者线程将会多消费一个分区。

**例1** ： 10 /3 = 3，而且除不尽，那么消费者线程 C1-0 将会多消费一个分区，所以最后分区分配的结果看起来是这样的：
		C1-0 将消费 0, 1, 2, 3 分区
		C2-0 将消费 4, 5, 6 分区
		C3-0 将消费 7, 8, 9 分区

**例2**: 有 11 个分区，那么最后分区分配的结果看起来是这样的：

​		C1-0 将消费 0, 1, 2, 3 分区
​		C2-0 将消费 4, 5, 6, 7 分区
​		C3-0 将消费 8, 9, 10 分区

**例3**：有 2 个主题(T1 和 T2)，分别有 10 个分区，那么最后分区分配的结果看起来是这样的：

​		C1-0 将消费 T1 主题的 0, 1, 2, 3 分区以及 T2 主题的 0,1, 2, 3 分区
​		C2-0 将消费 T1 主题的 4, 5, 6 分区以及 T2 主题的 4, 5,6 分区
​		C3-0 将消费 T1 主题的 7, 8, 9 分区以及 T2 主题的 7, 8,9 分区

**可以看出，C1-0 消费者线程比其他消费者线程多消费了 2 个分区，这就是 Range strategy 的一个很明显的弊端**

##### 7.2.2 RoundRobin strategy（轮询分区）

​			轮询分区策略是把所有 partition 和所有 consumer 线程都列出来，然后按照 hashcode 进行排序。最后通过轮询算法分配 partition 给消费线程。如果所有 consumer 实例的订阅是相同的，那么 partition 会均匀分布

​			例如：按照 hashCode 排序完的 topic-partitions 组依次为 T1-5, T1-3, T1-0, T1-8, T1-2, T1-1, T1-4,T1-7, T1-6, T1-9，我们的消费者线程排序为 C1-0, C1-1, C2-0, C2-1，最后分区分配的结果为：

​			C1-0 将消费 T1-5, T1-2, T1-6 分区；
​			C1-1 将消费 T1-3, T1-1, T1-9 分区；
​			C2-0 将消费 T1-0, T1-4 分区；
​			C2-1 将消费 T1-8, T1-7 分区；

使用轮询分区策略必须满足两个条件
- 每个主题的消费者实例具有相同数量的流

- 每个消费者订阅的主题必须是相同的

##### 7.2.3 什么时候会触发这个策略呢？

​		当出现以下几种情况时，kafka 会进行一次分区分配操作，也就是 kafka consumer 的 rebalance

- 同一个 consumer group 内新增了消费者
- 消费者离开当前所属的 consumer group，比如主动停机或者宕机
- topic 新增了分区（也就是分区数量发生了变化）

​        kafka consuemr 的 rebalance 机制规定了一个 consumergroup下的所有consumer如何达成一致来分配订阅topic的每个分区。而具体如何执行分区策略，就是前面提到过的两种内置的分区策略。而 kafka 对于分配策略这块，提供了可插拔的实现方式， 也就是说，除了这两种之外，我们还可以创建自己的分配机制。

#### 7.3 谁来执行 Rebalance 以及管理consumer的group 呢？

​		Kafka 提供了一个角色：coordinator 来执行对于 consumer group 的管理当 consumer group 的第一个 consumer 启动的时候，它会去和 kafka server 确定谁是它们组的coordinator。之后该 group 内的所有成员都会和该 coordinator 进行协调通信

##### 7.3.1 如何确定 coordinator

​		consumer group 如何确定自己的 coordinator 是谁呢, 消费者向 kafka 集群中的任意一个 broker 发送一个
GroupCoordinatorRequest 请求，服务端会返回一个负载最 小的 broker 节 点的 id，并 将该 broker 设 置为
coordinator

##### 7.3.2 JoinGroup 的过程

![1602571294764](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602571294764.png)

​		在 rebalance 之前，需要保证 coordinator 是已经确定好了的，整个 rebalance 的过程分为两个步骤，Join 和 Sync

**join:**  表示加入到 consumer group 中，在这一步中，所有的成员都会向 coordinator 发送 joinGroup 的请求。一旦所有成员都发送了 joinGroup 请求，那么 coordinator 会选择一个 consumer 担任 leader 角色，并把组成员信息和订阅信息发送消费者。

```java
protocol_metadata: 序列化后的消费者的订阅信息
leader_id： 消费组中的消费者，coordinator 会选择一个作为leader，对应的就是 member_id
member_metadata 对应消费者的订阅信息
members：consumer group 中全部的消费者的订阅信息
generation_id： 年代信息，类似于之前讲解 zookeeper 的时候的 epoch 是一样的，对于每一轮 rebalance，
generation_id 都会递增。主要用来保护 consumer group。隔离无效的 offset 提交。也就是上一轮的 consumer 成员无法提交 offset 到新的 consumer group 中
```

##### 7.3.3 Synchronizing Group State 阶段

![1602571658674](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602571658674.png)

​		完成分区分配之后，就进入了 Synchronizing Group State阶段 主 要 逻 辑 是 向 GroupCoordinator 发 送
SyncGroupRequest 请求，并且处理 SyncGroupResponse响应，简单来说，就是 leader 将消费者对应的 partition 分配方案同步给 consumer group 中的所有 consumer

​		每个消费者都会向 coordinator 发送 syncgroup 请求，不过只有 leader 节点会发送分配方案，当 leader 把方案发给 coordinator 以后，coordinator 会把结果设置到 SyncGroupResponse 中。这样所有成员都知道自己应该消费哪个分区。

-  consumer group 的分区分配方案是在客户端执行的！Kafka 将这个权利下放给客户端主要是因为这样做可以有更好的灵活性

#### 7.4 如何保存消费端的消费位置

##### 7.4.1 什么是 offset

​		每个 topic可以划分多个分区（每个 Topic 至少有一个分区），同一topic 下的不同分区包含的消息是不同的。

​		每个消息在被添加到分区时，都会被分配一个 offset（称之为偏移量），它是消息在此分区中的唯一编号，kafka 通过 offset 保证消息在分区内的顺序，offset 的顺序不跨分区，即 kafka 只保证在同一个分区内的消息是有序的；

​		对于应用层的消费来说，每次消费一个消息并且提交以后，会保存当前消费到的最近的一个 offset。那么 offset 保存在哪里？

![1602573836501](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602573836501.png)

##### 7.4.2 offset 在哪里维护？

​		在 kafka 中，提供了一个__consumer_offsets_* 的一个topic ， 把 offset 信 息 写 入 到 这 个 topic 中 。__consumer_offsets——按保存了每个 consumer group某一时刻提交的 offset 信息。 __consumer_offsets 默认有50 个分区。

​		设 置 了 一 个KafkaConsumerDemo 的 groupid。首先我们需要找到这个 consumer_group 保存在哪个分区中

```java
properties.put(ConsumerConfig.GROUP_ID_CONFIG,"KafkaConsumerDemo");
```

计算公式

- Math.abs(“groupid”.hashCode())%groupMetadataTopicPartitionCount ; 由 于 默 认 情 况 下groupMetadataTopicPartitionCount 有 50 个分区，计算得到的结果为:35, 意味着当前的consumer_group的位移信息保存在__consumer_offsets 的第 35 个分区
- 执行如下命令，可以查看当前 consumer_goup 中的offset 位移信息

sh kafka-simple-consumer-shell.sh --topic__consumer_offsets --partition 35 --broker-list
192.168.11.153:9092,192.168.11.154:9092,192.168.11.157:9092 --formatter
"kafka.coordinator.group.GroupMetadataManager\$OffsetsMessageFormatter"



### 八、消息的存储

#### 8.1 消息的保存路径

消息发送端发送消息到 broker 上以后，消息是如何持久化的呢？那么接下来去分析下消息的存储

- kafka 是使用日志文件的方式来保存生产者和发送者的消息，每条消息都有一个 offset 值来表示它在分区中的偏移量
- Kafka 中存储的一般都是海量的消息数据，为了避免日志文件过大，Log 并不是直接对应在一个磁盘上的日志文件，而是对应磁盘上的一个目录

例如：这个目录的明明规则是<topic_name>_<partition_id>比如创建一个名为firstTopic的topic，其中有3个partition，那么在 kafka 的数据目录（/tmp/kafka-log）中就有 3 个目录，firstTopic-0~3

#### 8.2 多个分区在集群中的分配

![1602574481726](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602574481726.png)

如果我们对于一个 topic，在集群中创建多个 partition，那么 partition 是如何分布的呢？

- 将所有 N Broker 和待分配的 i 个 Partition 排序

- 将第 i 个 Partition 分配到第(i mod n)个 Broker 上

#### 8.3 消息写入的性能

![1602574560662](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602574560662.png)

​		消息从发送到落地保存，broker 维护的消息日志本身就是文件目录，每个文件都是二进制保存，生产者和消费者使用相同的格式来处理。在消费者获取消息时，服务器先从硬盘读取数据到内存，然后把内存中的数据原封不动的通过 socket 发送给消费者。虽然这个操作描述起来很简单，但实际上经历了很多步骤。

#### 8.4 消息的文件存储机制

​		一个 topic 的多个 partition 在物理磁盘上的保存路径，那么我们再来分析日志的存储方式。通过如
下命令找到对应 partition 下的日志内容

```shell
[root@localhost ~]# ls /tmp/kafka-logs/firstTopic-1/
00000000000000000000.index
00000000000000000000.log
00000000000000000000.timeindex
Leader-epoch-checkpoint (保证ISR副本同步)
```

​		kafka 是通过分段的方式将 Log 分为多个 LogSegment，LogSegment 是一个逻辑上的概念，一个 LogSegment 对应磁盘上的一个日志文件和一个索引文件，其中日志文件是用来记录消息的。索引文件是用来保存消息的索引。那么这个 LogSegment 是什么呢？

##### 8.4.1 LogSegment

​		假设 kafka 以 partition 为最小存储单位，那么我们可以想象当 kafka producer 不断发送消息，必然会引起 partition文件的无线扩张，这样对于消息文件的维护以及被消费的消息的清理带来非常大的挑战，所以 kafka 以 segment 为单位又把 partition 进行细分。

​		每个 partition 相当于一个巨型文件被平均分配到多个大小相等的segment数据文件中（每个 segment 文件中的消息不一定相等），这种特性方便已经被消费的消息的清理，提高磁盘的利用率。

- log.segment.bytes=107370 (设置分段大小),默认是1gb，我们把这个值调小以后，可以看到日志分段的效果

例：**抽取其中 3 个分段来进行分析**

![1602644237447](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602644237447.png)

​		segment file 由 2 大部分组成，分别为 index file 和 datafile，此 2 个文件一一对应，成对出现，后缀".index"和“.log”分别表示为 segment 索引文件、数据文件.

​		segment 文件命名规则：partion 全局的第一个 segment从 0 开始，后续每个 segment 文件名为上一个 segment文件最后一条消息的 offset 值进行递增。数值最大为 64 位long 大小，20 位数字字符长度，没有数字用 0 填充

##### 8.4.2 查看 segment 文件命名规则

- 通过下面这条命令可以看到 kafka 消息日志的内容

```shell
sh kafka-run-class.sh kafka.tools.DumpLogSegments --files /tmp/kafka-logs/test-0/00000000000000000000.log --print-data-log
```

输出结果为:

```shell
offset: 5376 position: 102124 CreateTime: 1531477349287 isvalid: true keysize: -1 valuesize: 12 magic: 2compresscodec: NONE producerId: -1 producerEpoch: -1 sequence: -1 isTransactional: false headerKeys: []payload: message_5376
```

第一个 log 文件的最后一个 offset 为:5376,所以下一个

segment 的文件命名为: 00000000000000005376.log。

对应的 index 为 00000000000000005376.index

##### 8.4.3 segment 中 index 和 log 的对应关系

从所有分段中，找一个分段进行分析为了提高查找消息的性能，为每一个日志文件添加 2 个索引

索引文件：OffsetIndex 和 TimeIndex，分别对应*.index以及*.timeindex, 

TimeIndex 索引文件格式：它是映射时间戳和相对 offset

**查看索引内容**

```shell
sh kafka-run-class.sh kafka.tools.DumpLogSegments --files /tmp/kafka-logs/test-0/00000000000000000000.index --print-data-log
```

![1602645238163](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602645238163.png)

​		如图所示，index 中存储了索引以及物理偏移量。 log 存储了消息的内容。索引文件的元数据执行对应数据文件中message 的物理偏移地址。

**查找案例：**

​		以[4053,80899]为例，在 log 文件中，对应的是第 4053 条记录，物理偏移量（position）为 80899. position 是ByteBuffer 的指针位置

##### 8.4.4 在 partition 中如何通过 offset 查找 message

查找的算法是

- 根据 offset 的值，查找 segment 段中的 index 索引文件。由于索引文件命名是以上一个文件的最后一个offset 进行命名的，所以，使用二分查找算法能够根据offset 快速定位到指定的索引文件。
- 找到索引文件后，根据 offset 进行定位，找到索引文件中的符合范围的索引。（kafka 采用稀疏索引的方式来提高查找性能）
- 得到 position 以后，再到对应的 log 文件中，从 position出开始查找 offset 对应的消息，将每条消息的 offset 与目标 offset 进行比较，直到找到消息

​       比如说，我们要查找 offset=2490 这条消息，那么先找到00000000000000000000.index, 然后找到[2487,49111]这个索引，再到 log 文件中，根据 49111 这个 position 开始查找，比较每条消息的 offset 是否大于等于 2490。最后查找到对应的消息以后返回

##### 8.4.5 Log 文件的消息内容分析

​		前面我们通过 kafka 提供的命令，可以查看二进制的日志文件信息，一条消息，会包含很多的字段。

```shell
offset: 5371 position: 102124 CreateTime: 1531477349286 isvalid: true keysize: -1 valuesize: 12 magic: 2 compresscodec: NONE producerId: -1 producerEpoch: -1 sequence: -1 isTransactional: false headerKeys: []payload: message_5371
```

offset 和 position

 createTime 表示创建时间、

keysize 和 valuesize 表示 key 和 value 的大小、

compresscodec 表示压缩编码

payload:表示消息的具体内容

#### 8.5 日志的清除策略以及压缩策略

##### 8.5.1 日志清除策略

​		日志的分段存储，一方面能够减少单个文件内容的大小，另一方面，方便 kafka 进行日志清理。日志的
清理策略有两个

- 根据消息的保留时间，当消息在 kafka 中保存的时间超过了指定的时间，就会触发清理过程
- 根据 topic 存储的数据大小，当 topic 所占的日志文件大小大于一定的阀值，则可以开始删除最旧的消息。kafka会启动一个后台线程，定期检查是否存在可以删除的消息

​		通过 **log.retention.bytes** 和 **log.retention.hours** 这两个参数来设置，当其中任意一个达到要求，都会执行删除。默认的保留时间是：7 天

##### 8.5.2 日志压缩策略

​			Kafka 还提供了“日志压缩（Log Compaction）”功能，通过这个功能可以有效的减少日志文件的大小，缓解磁盘紧张的情况，在很多实际场景中，消息的 key 和 value 的值之间的对应关系是不断变化的，就像数据库中的数据会不断被修改一样，消费者只关心 key 对应的最新的 value。因此，我们可以开启 kafka 的日志压缩功能，服务端会在后台启动启动Cleaner线程池，定期将相同的key进行合并，只保留最新的 value 值。日志的压缩原理是

![1602646445141](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602646445141.png)

### 九、partition高可用副本机制

​			我们已经知道Kafka的每个topic都可以分为多个Partition，并且多个 partition 会均匀分布在集群的各个节点下。虽然这种方式能够有效的对数据进行分片，但是对于每个partition 来说，都是单点的，当其中一个 partition 不可用的时候，那么这部分消息就没办法消费。所以 kafka 为了提高 partition 的可靠性而提供了副本的概念（Replica）,通过副本机制来实现冗余备份。

​			每个分区可以有多个副本，并且在副本集合中会存在一个leader 的副本，所有的读写请求都是由 leader 副本来进行处理。剩余的其他副本都做为 follower 副本，follower 副本会从 leader 副本同步消息日志。。这个有点类似zookeeper 中 leader 和 follower 的概念，但是具体的时间方式还是有比较大的差异。所以我们可以认为，副本集会存在一主多从的关系。

​			一般情况下，同一个分区的多个副本会被均匀分配到集群中的不同 broker 上，当 leader 副本所在的 broker 出现故障后，可以重新选举新的 leader 副本继续对外提供服务。通过这样的副本机制来提高 kafka 集群的可用性。

#### 9.1 副本分配算法

​	将所有 N Broker 和待分配的 i 个 Partition 排序.
​	将第 i 个 Partition 分配到第(i mod n)个 Broker 上.
​	将第 i 个 Partition 的第 j 个副本分配到第((i + j) mod n)个Broker 上

#### 9.2 创建一个带副本机制的 topic

通过下面的命令去创建带 2 个副本的 topic

```shell
./kafka-topics.sh --create --zookeeper 192.168.11.156:2181 --replication-factor 2 --partitions 3 --topic secondTopic
```

然后我们可以在/tmp/kafka-log 路径下看到对应 topic 的副本信息了。我们通过一个图形的方式来表达。

- 针对 secondTopic 这个 topic 的 3 个分区对应的 3 个副本

![1602647468399](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602647468399.png)

- 如何知道那个各个分区中对应的 leader 是谁呢？

  ​	在 zookeeper 服务器上，通过如下命令去获取对应分区的信息, 比如下面这个是获取 secondTopic 第 1 个分区的状态信息。

```shell
get /brokers/topics/secondTopic/partitions/1/state
{"controller_epoch":12,"leader":0,"version":1,"leader_epoch":0,"isr":[0,1]}
```

leader 表示当前分区的 leader 是那个 broker-id

下图中。绿色线条的表示该分区中的 leader 节点。其他节点就为follower

![1602655897744](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602655897744.png)

​			Kafka 提供了数据复制算法保证，如果 leader 发生故障或挂掉，一个新 leader 被选举并被接受客户端的消息成功写入。Kafka 确保从同步副本列表中选举一个副本为 leader；

​			leader 负责维护和跟踪 ISR(in-Sync replicas ， 副本同步队列)中所有 follower 滞后的状态。当 producer 发送一条消息到 broker 后，leader 写入消息并复制到所有 follower。消息提交之后才被成功复制到所有的同步副本。

**既然有副本机制，就一定涉及到数据同步的概念，那接下来分析下数据是如何同步的？**

#### 9.3 kafka 副本机制中的几个概念

Kafka 分区下有可能有很多个副本(replica)用于实现冗余，从而进一步实现高可用。副本根据角色的不同可分为 3 类：

leader 副本：响应 clients 端读写请求的副本

follower 副本：被动地备份 leader 副本中的数据，不能响应 clients 端读写请求。

**ISR** 副本：包含了 leader 副本和所有与 leader 副本保持同步的 follower 副本

​		如何判定是否与 leader 同步后面会提到每个 Kafka 副本对象都有两个重要的属性：LEO 和HW。注意是所有的副本，而不只是 leader 副本。

**LEO**：即日志末端位移(log end offset)，记录了该副本底层日志(log)中下一条消息的位移值。如果 LEO=10，那么表示该副本保存了 10 条消息，位移值范围是[0, 9]。

**HW：**即上面提到的水位值。对于同一个副本对象而言，其HW 值不会大于 LEO 值。小于等于 HW 值的所有消息都被认为是“已备份”的（replicated）。

#### 9.4 副本协同机制

​		消息的读写操作都只会由 leader 节点来接收和处理。follower 副本只负责同步数据以及当 leader 副本所在的 broker 挂了以后，会从 follower 副本中选取新的leader。

![1602666463486](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602666463486.png)

​		写请求首先由 Leader 副本处理，之后 follower 副本会从leader 上拉取写入的消息，这个过程会有一定的延迟，导致 follower 副本中保存的消息略少于 leader 副本，但是只要没有超出阈值都可以容忍。但是如果一个 follower 副本出现异常，比如宕机、网络断开等原因长时间没有同步到消息，那这个时候，leader 就会把它踢出去。kafka 通过 ISR集合来维护一个分区副本信息

#### 9.5 ISR

​		SR 表示目前“可用且消息量与 leader 相差不多的副本集合，这是整个副本集合的一个子集”。

ISR 集合中的副本必须满足两个条件：

- 副本所在节点必须维持着与 zookeeper 的连接
- 副本最后一条消息的 offset 与 leader 副本的最后一条消息的 offset 之 间 的 差 值 不 能 超 过 指 定 的 阈 值(replica.lag.time.max.ms) replica.lag.time.max.ms： 如果该 follower 在此时间间隔内一直没有追上过leader 的所有消息，则该 follower 就会被剔除 isr 列表
- ISR 数 据 保 存 在 Zookeeper 的/brokers/topics/<topic>/partitions/<partitionId>/state 节点中



#### 9.6 HW&LEO

​			关于 follower 副本同步的过程中，还有两个关键的概念，HW(HighWatermark)和 LEO(Log End Offset). 这两个参数跟 ISR 集合紧密关联。

​			HW 标记了一个特殊的 offset，当消费者处理消息的时候，只能拉去到 HW 之前的息，HW之后的消息对消费者来说是不可见的。也就是说，取partition 对应 ISR 中最小的 LEO 作为 HW，consumer 最多只能消费到 HW 所在的位置。每个 replica 都有 HW，leader 和 follower 各自维护更新自己的 HW 的状态。

​			一条消息只有被 ISR 里的所有 Follower 都从 Leader 复制过去才会被认为已提交。这样就避免了部分数据被写进了Leader，还没来得及被任何 Follower 复制就宕机了，而造成数据丢失（Consumer 无法消费这些数据）。而对于Producer 而言它可以选择是否等待消息 commit，这可以通过 acks 来设置。这种机制确保了只要 ISR 有一个或以上的Follower，一条被 commit 的消息就不会丢失。

#### 9.7 数据的同步过程

了解了副本的协同过程以后，还有一个最重要的机制，就是数据的同步过程。它需要解决

- 怎么传播消息
- 在向消息发送端返回 ack 之前需要保证多少个 Replica已经接收到这个消息

#### 9.8 数据的处理过程是

Producer 在发布 消 息 到 某个 Partition 时， 先通过ZooKeeper找到该Partition的Leader

```shell
【 get /brokers/topics/<topic>/partitions/2/state】
```

​			然后无论该Topic 的 Replication Factor 为多少（也即该 Partition 有多少个 Replica），Producer 只将该消息发送到该 Partition 的Leader。Leader会将该消息写入其本地Log。每个Follower都从 Leader pull 数据。

​			这种方式上，Follower 存储的数据顺序与 Leader 保持一致。Follower 在收到该消息并写入其Log 后，向 Leader 发送 ACK。一旦 Leader 收到了 ISR 中的所有 Replica 的 ACK，该消息就被认为已经 commit 了，Leader 将增加 HW( HighWatermark )并且向 Producer 发送ACK。

##### 9.8.1 初始状态

​			初始状态下，leader 和 follower 的 HW 和 LEO 都是 0，leader 副本会保存 remote LEO，表示所有 follower LEO，也会被初始化为 0，这个时候，producer 没有发送消息.

​			follower 会不断地个 leader 发送 FETCH 请求，但是因为没有数据，这个请求会被 leader 寄存，当在指定的时间之后会强制完成请求 ， 这个时间配置是(replica.fetch.wait.max.ms)，如果在指定时间内 producer有消息发送过来，那么 kafka 会唤醒 fetch 请求，让 leader继续处理

![1602669163594](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602669163594.png)

​			这里会分两种情况，第一种是 leader 处理完 producer 请求之后，follower 发送一个 fetch 请求过来、第二种是follower 阻塞在 leader 指定时间之内，leader 副本收到producer 的请求。

#### 9.9 follower 的 fetch 请求是当 leader 处理消息以后执行的

##### 9.9.1 生产者发送一条消息

![1602669408866](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602669408866.png)

- leader 处理完 producer 请求之后，follower 发送一个fetch 请求过来

leader 副本收到请求以后，会做几件事情

- 把消息追加到 log 文件，同时更新 leader 副本的 LEO
- 尝试更新 leader HW 值。这个时候由于 follower 副本还没有发送 fetch 请求，那么 leader 的 remote LEO 仍然是 0。leader 会比较自己的 LEO 以及 remote LEO 的值发现最小值是 0，与 HW 的值相同，所以不会更新 HW

##### 9.9.2 follower fetch 消息

![1602669501574](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602669501574.png)

follower 发送 fetch 请求，leader 副本的处理逻辑是:

- 读取 log 数据、更新 remote LEO=0(follower 还没有写入这条消息，这个值是根据 follower 的 fetch 请求中的offset 来确定的)
- 尝试更新 HW，因为这个时候 LEO 和 remoteLEO 还是不一致，所以仍然是 HW=0
- 消息内容和当前分区的 HW 值发送给 follower 副本

follower 副本收到 response 以后

- 将消息写入到本地 log，同时更新 follower 的 LEO
- 更新 follower HW，本地的 LEO 和 leader 返回的 HW进行比较取小的值，所以仍然是 0



第一次交互结束以后，HW 仍然还是 0，这个值会在下一次follower 发起 fetch 请求时被更新

![1602669932112](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602669932112.png)

**follower 发第二次 fetch 请求，leader 收到请求以后**

- 读取 log 数据
- 更新 remote LEO=1， 因为这次 fetch 携带的 offset 是1.
- 更新当前分区的 HW，这个时候 leader LEO 和 remoteLEO 都是 1，所以 HW 的值也更新为 1
- 把数据和当前分区的 HW 值返回给 follower 副本，这个时候如果没有数据，则返回为空

follower 副本收到 response 以后：

- 如果有数据则写本地日志，并且更新 LEO
- 更新 follower 的 HW 值

到目前为止，数据的同步就完成了，意味着消费端能够消费 offset=0 这条消息。

##### 9.9.3 follower 的fetch 请求是直接从 阻塞过程中触发

​		前面说过，由于 leader 副本暂时没有数据过来，所以follower 的 fetch 会被阻塞，直到等待超时或者 leader 接收到新的数据。当 leader 收到请求以后会唤醒处于阻塞的fetch 请求。处理过程基本上和前面说的一致



##### 9.9.4 数据丢失的问题

​			前提：min.insync.replicas=1 的时候。->设定 ISR 中的最小副本数是多少，默认值为 1, 当且仅当 acks 参数设置为-1（表示需要所有副本确认）时，此参数才生效. 表达的含义是，至少需要多少个副本同步才能表示消息是提交的

​			所以，当 min.insync.replicas=1 的时候一旦消息被写入 leader 端 log 即被认为是“已提交”，而延迟一轮 FETCH RPC 更新 HW 值的设计使得 follower HW值是异步延迟更新的，倘若在这个过程中leader发生变更，那么成为新 leader 的 follower 的 HW 值就有可能是过期的，使得 clients 端认为是成功提交的消息被删除。![1602671031934](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1602671031934.png)

##### 9.9.5 数据丢失的解决方案

​			在 kafka0.11.0.0 版本以后，提供了一个新的解决方案，使用 leader epoch 来解决这个问题，leader epoch 实际上是一对之(epoch,offset), epoch 表示 leader 的版本号，从 0开始，当 leader 变更过 1 次时 epoch 就会+1，而 offset 则对应于该 epoch 版本的 leader 写入第一条消息的位移。

(0,0) ; (1,50); 表示第一个 leader 从 offset=0 开始写消息，一共写了 50 条，第二个 leader 版本号是 1，从 50 条处开始写消息。

##### 9.9.6 ISR 的设计原理

​		在所有的分布式存储中，冗余备份是一种常见的设计方式，而常用的模式有同步复制和异步复制，按照 kafka 这个副本模型来说

​		如果采用**同步复制**，那么需要要求所有能工作的 Follower 副本都复制完，这条消息才会被认为提交成功，一旦有一个follower 副本出现故障，就会导致 HW 无法完成递增，消息就无法提交，消费者就获取不到消息。这种情况下，故障的Follower 副本会拖慢整个系统的性能，设置导致系统不可用

​		如果采用**异步复制**，leader 副本收到生产者推送的消息后，就认为次消息提交成功。follower 副本则异步从 leader 副本同步。这种设计虽然避免了同步复制的问题，但是假设所有follower 副本的同步速度都比较慢他们保存的消息量远远落后于 leader 副本。而此时 leader 副本所在的 broker 突然宕机，则会重新选举新的 leader 副本，而新的 leader 副本中没有原来 leader 副本的消息。这就出现了消息的丢失

​		kafka 权衡了同步和异步的两种策略，采用 ISR 集合，巧妙解决了两种方案的缺陷：当 follower 副本延迟过高，leader 副本则会把该 follower 副本提出 ISR 集合，消息依然可以快速提交。当 leader 副本所在的 broker 突然宕机，会优先将 ISR 集合中follower 副本选举为 leader，新 leader 副本包含了 HW 之前的全部消息，这样就避免了消息的丢失。

