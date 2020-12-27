### 一、从架构的发展过程说起

![1600739793326](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600739793326.png)

​			单一的应用架构，后端的架构通过垂直伸缩的方式很难达到我们期望的性能要求，同时投入产出比也非常大，同时普通 PC 的性能也越来越高，所以通过水平伸缩的方式来提升性能成为了主流。

​			**单一的应用架构升级为分布式架构**

​			服务越来越多，规模越来越大时，对应的机器数量也越来越大，**单靠人工来管理和维护服务及地址的配置地址信息会越来越困难**，单点故障的问题也开始凸显出来，一旦服务路由或者负载均衡服务器宕机，依赖他的所有服务均将失效。		

​			**此时，需要一个能够动态注册和获取服务信息的地方。来统一管理服务名称和其对应的服务器列表信息，称之为服务配置中心，服务提供者在启动时，将其提供的服务名称、服务器地址注册到服务配置中心，服务消费者通过服务配置中心来获得需要调用的服务的机器列表。**

​			这个服务配置中心需要的功能：

​				1. 通过相应的负载均衡算法，选取其中一台服务器进行调用。

​				2. 服务器宕机或者下线时，相应的机器需要能够动态地从服务配置中心里面移除，并通知相应的服务消费者

​				3. 服务消费者只有在第一次调用服务时需要查询服务配置中心，然后将查询到的信息缓存到本地，后面的调用直接使用本地缓存的服务地址列表信息，而不需要重新发起请求道服务配置中心去获取相应的服务地址列表，直到服务的地址列表有变更

### 二 、什么是Zookeeper

#### 			2.1 zookeeper 安装部署

##### 				2.1.1 单机版安装		

​				zookeeper 有两种运行模式：集群模式和单击模式。下 载 zookeeper 安 装 包 ：
http://apache.fayea.com/zookeeper/

​			   下载完成，通过tar -zxvf解压

常用命令：

```shell
1. 启动 ZK 服务:
bin/zkServer.sh start
2. 查看 ZK 服务状态:
bin/zkServer.sh status
3. 停止 ZK 服务:
bin/zkServer.sh stop
4. 重启 ZK 服务:
bin/zkServer.sh restart
5. 连接服务器
zkCli.sh -timeout 0 -r -server ip:port
```

##### 			2.1.2 单机环境安装

​				一般情况下，在开发测试环境，没有这么多资源的情况下，而且也不需要特别好的稳定性的前提下，我们可以使用单机部署；初 次 使 用 zookeeper ， 需 要 将 conf 目 录 下 的zoo_sample.cfg 文件 copy 一份重命名为 zoo.cfg修改 dataDir 目录，dataDir 表示日志文件存放的路径

##### 			2.1.3 集群环境安装

​				在zookeeper集群中，各个节点总共有三种角色，分别是：leader，follower，observer			

​				集群模式我们采用模拟 3 台机器来搭建 zookeeper 集群。分别复制安装包到三台机器上并解压，同时 copy 一份zoo.cfg。

###### 			2.1.3.1 修改配置文件

修改端口：

```
server.1=IP1:2888:3888 【2888：访问 zookeeper 的端口；3888：重新选举 leader 的端口】
server.2=IP2.2888:3888
server.3=IP3.2888:2888
```

server.A=B：C：D：其 中
			A 是一个数字，表示这个是第几号服务器；
			B 是这个服务器的 ip 地址；
			C 表示的是这个服务器与集群中的 Leader服务器交换信息的端口；
			D 表示的是万一集群中的 Leader 服务器挂了，需要一个端口来重新进行选举，选出一个新的 Leader，而这个端口就是用来执行选举时服务器相互通信的端口。

​		在集群模式下，集群中每台机器都需要感知到整个集群是由 哪 几 台 机 器 组 成 的 ， 在 配 置 文 件 中 ， 按 照 格 式
​		server.id=host:port:port，每一行代表一个机器配置id: 指的是 server ID,用来标识该机器在集群中的机器序号

###### 			2.1.3.2 新建 datadir 目录，设置 myid

​			在每台zookeeper机器上，我们都需要在数据目录(dataDir)下创建一个 myid 文件，该文件只有一行内容，对应每台机器的 Server ID 数字；比如 server.1 的 myid 文件内容就是1。【必须确保每个服务器的 myid 文件中的数字不同，并且和自己所在机器的 zoo.cfg 中 server.id 的 id 值一致，id 的范围是 1~255】

###### 			2.1.3.3  启动 zookeeper

带 Observer 角色的集群

​			Observer：**在不影响写性能的情况下扩展 zookeeper**本身 zookeeper 的集群性能已经很好了，但是如果超大量的客户端访问，就势必需要增加 zookeeper 集群的服务器数量，而**随着服务器的增加，zookeeper 集群的写性能就会下降**；zookeeper 中 znode 的变更需要半数及以上服务器投票通过，而随着机器的增加，由于网络消耗等原因必定会导致投票成本增加。也就导致性能下降的结果

###### 			2.1.3.4 结构

![1600742282954](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600742282954.png)

###### 			2.1.3.5 节点特性

​					持久性

​					临时节点 

​					有序节点

​					同一节点必须唯一

​					临时节点不能存在子节点



### 三 、zookeeper的由来

![1600843334150](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600843334150.png)

![1600843351643](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600843351643.png)

#### 3.1 Zookeeper 的前世今生

​		从上面的案例可以看出，分布式系统的很多难题，都是由于缺少协调机制造成的。在分布式协调这块做得比较好的，有 Google 的 Chubby 以及 Apache 的 Zookeeper。Google Chubby 是一个分布式锁服务，通过 GoogleChubby 来解决分布式协作、Master 选举等与分布式锁服务相关的问题。

​		在上面这个架构下 zookeeper 以后，可以用来解决 task 执行问题，各个服务先去 zookeeper 上去注册节点，然后获得权限以后再来访问 task

#### 3.2 zookeeper 的设计猜想

​		如果设计 zookeeper的分布式管理的中间件，那么它需要满足什么条件？

##### 	3.2.1 防止单点故障	

​		如果要防止 zookeeper 这个中间件的单点故障，那就势必要做集群。而且这个集群如果要满足高性能要求的话，还得是一个高性能高可用的集群。高性能意味着这个集群能够分担客户端的请求流量，高可用意味着集群中的某一个节点宕机以后，不影响整个集群的数据和继续提供服务的可能性。

​		***结论： 所以这个中间件需要考虑到集群,而且这个集群还需要分摊客户端的请求流量***

#####   3.2.2 leader节点的维护

​		如果要满足这样的一个高性能集群，我们最直观的想法应该是，每个节点都能接收到请求，并且每个节点的数据都必须要保持一致。要实现各个节点的数据一致性，就势必要一个 leader 节点负责协调和数据同步操作	

​		如果在这样一个集群中没有 leader 节点，每个节点都可以接收所有请求，那么这个集群的数据同步的复杂度是非常大

​		**结论：所以这个集群中涉及到数据同步以及会存在leader 节点**

#####   3.2.3 数据恢复

​		如何在这些节点中选举出 leader 节点，以及leader 挂了以后，如何恢复呢？

#####  3.2.4 分布式数据的一致性

​		leader 节点如何和其他节点保证数据一致性，并且要求是强一致的。在分布式系统中，每一个机器节点虽然都能够明确知道自己进行的事务操作过程是成功和失败，但是却无法直接获取其他分布式节点的操作结果。所以当一个事务操作涉及到跨节点的时候，就需要用到分布式事务，分布式事务的数据一致性协议有 2PC 协议和3PC 协议。

### 四、关于 2PC 提交

#### 	4.1 什么是2PC提交？	

​		当一个事务操作需要跨越多个分布式节点的时候，为了保持事务处理的 ACID特性，就需要引入一个“协调者”（TM）来统一调度所有分布式节点的执行逻辑，这些被调度的分布式节点被称为 AP。TM 负责调度 AP 的行为，并最终决定这些 AP 是否要把事务真正进行提交；因为整个事务是分为两个阶段提交，所以叫 2pc

#### 	 4.2 图示

![1600844912626](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600844912626.png)

![1600844920744](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600844920744.png)

#### 4.3 阶段一：提交事务请求（投票）

##### 	4.3.1 事务询问	

​		协调者向所有的参与者发送事务内容，询问是否可以执行事务提交操作，并开始等待各参与者的响应

##### 	4.3.2 执行事务	

​		各个参与者节点执行事务操作，并将 Undo 和 Redo 信息记录到事务日志中，尽量把提交过程中所有消耗时间的操作和准备都提前完成确保后面 100%成功提交事务

##### 	4.3.3 各个参与者向协调者反馈事务询问的响应

​	  如果各个参与者成功执行了事务操作，那么就反馈给参与者yes 的响应，表示事务可以执行；如果参与者没有成功执行事务，就反馈给协调者 no 的响应，表示事务不可以执行，上面这个阶段有点类似协调者组织各个参与者对一次事务操作的投票表态过程，因此 2pc 协议的第一个阶段称为“投票阶段”，即各参与者投票表名是否需要继续执行接下去的事务提交操作。

#### 4.4 阶段二：执行事务提交

​	 在这个阶段，协调者会根据各参与者的反馈情况来决定最终是否可以进行事务提交操作，正常情况下包含两种可能:执行事务、中断事务

### 五、 zookeeper 的集群

#### 	5.1 示意图

![1600845913451](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600845913451.png)

​			在 zookeeper 中，客户端会随机连接到 zookeeper 集群中的一个节点，如果是读请求，就直接从当前节点中读取数据，如果是写请求，那么请求会被转发给leader提交事务，然后 leader 会广播事务，只要有超过半数节点写入成功，那么写请求就会被提交（类 2PC 事务）

#### 		5.2 请求流程概述		

​				 所有事务请求必须由一个全局唯一的服务器来协调处理，这个服务器就是 Leader 服务器，其他的服务器就是follower。leader 服务器把客户端的失去请求转化成一个事务 Proposal（提议），并把这个 Proposal 分发给集群中的所有 Follower 服务器。之后 Leader 服务器需要等待所有Follower 服务器的反馈，一旦超过半数的 Follower 服务器进行了正确的反馈，那么 Leader 就会再次向所有的Follower 服务器发送 Commit 消息，要求各个 follower 节点对前面的一个 Proposal 进行提交;

#### 		5.3 集群角色

##### 			5.3.1 Leader 角色

​		Leader 服务器是整个 zookeeper 集群的核心，主要的工作任务有两项

​			1. 事物请求的唯一调度和处理者，保证集群事物处理的顺序性

​			2. 集群内部各服务器的调度者

##### 			5.3.2 Follower 角色

​		Follower 角色的主要职责是

​			1. 处理客户端非事物请求、转发事物请求给 leader 服务器

​			2. 参与事物请求 Proposal 的投票（需要半数以上服务器通过才能通知 leader commit 数据; Leader 发起的提案，要求 Follower 投票）

​			3. 参与 Leader 选举的投票

##### 			5.3.3 Observer 角色

​		Observer 的工作原理与follower 角色基本一致，而它和 follower 角色唯一的不同在于 observer 不参与任何形式的投票，包括事物请求Proposal的投票和leader选举的投票。简单来说，observer服务器只提供非事物请求服务，通常在于不影响集群事物处理能力的前提下提升集群非事物处理的能力

#### 		5.4 集群组成	

​		通常 zookeeper 是由 2n+1 台 server 组成，每个 server 都知道彼此的存在。对于 2n+1 台 server，只要有 n+1 台（大多数）server 可用，整个系统保持可用。

​	  之所以要满足这样一个等式，是因为一个节点要成为集群中的 leader，需要有超过及群众过半数的节点支持，这个涉及到 leader 选举算法。同时也涉及到事务请求的提交投票

### 六 、ZAB 协议

​			ZAB（Zookeeper Atomic Broadcast） 协议是为分布式协调服务 ZooKeeper 专门设计的一种支持崩溃恢复的原子广播协议。在 ZooKeeper 中，主要依赖 ZAB 协议来实现分布式数据一致性，基于该协议，ZooKeeper 实现了一种主备模式的系统架构来保持集群中各个副本之间的数据一致性。

#### 	6.1 zab 协议介绍

​			ZAB 协议包含两种基本模式，分别是

​					1. 崩溃恢复

​					2. 原子广播		

​			当整个集群在启动时，或者当 leader 节点出现网络中断、崩溃等情况时，ZAB 协议就会进入恢复模式并选举产生新的 Leader，当 leader 服务器选举出来后，并且集群中有过半的机器和该 leader 节点完成数据同步后（同步指的是数据同步，用来保证集群中过半的机器能够和 leader 服务器的数据状态保持一致），ZAB 协议就会退出恢复模式。		

​			当集群中已经有过半的 Follower 节点完成了和 Leader 状态同步以后，那么整个集群就进入了消息广播模式。这个时候，在 Leader 节点正常工作时，启动一台新的服务器加入到集群，那这个服务器会直接进入数据恢复模式，和leader 节点进行数据同步。同步完成后即可正常对外提供非事务请求的处理。

#### 	6.2 消息广播的实现原理		

​		消息广播的过程实际上是一个简化版本的二阶段提交过程

​			1.  leader 接收到消息请求后，将消息赋予一个全局唯一的64 位自增 id，叫：zxid，通过 zxid 的大小比较既可以实现因果有序这个特征

​			2. leader 为每个 follower 准备了一个 FIFO 队列（通过 TCP协议来实现，以实现了全局有序这一个特点）将带有 zxid的消息作为一个提案（proposal）分发给所有的 follower

​			3. 当 follower 接收到 proposal，先把 proposal 写到磁盘，写入成功以后再向 leader 回复一个 ack

​	   	 4. 当 leader 接收到合法数量（超过半数节点）的 ACK 后，leader 就会向这些 follower 发送 commit 命令，同时会在本地执行该消息

​			5. 当 follower 收到消息的 commit 命令以后，会提交该消息

![1600848173791](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600848173791.png)

​			**leader 的投票过程，不需要 Observer 的 ack，也就是Observer 不需要参与投票过程，但是 Observer 必须要同步 Leader 的数据从而在处理请求的时候保证数据的一致性**

#### 		6.3 崩溃恢复(数据恢复)

​				ZAB 协议的这个基于原子广播协议的消息广播过程，在正常情况下是没有任何问题的，但是一旦 Leader 节点崩溃，或者由于网络问题导致 Leader 服务器失去了过半的Follower 节点的联系（leader 失去与过半 follower 节点联系，可能是 leader 节点和 follower 节点之间产生了网络分区，那么此时的 leader 不再是合法的 leader 了），那么就会进入到崩溃恢复模式。在 ZAB 协议中，为了保证程序的正确运行，整个恢复过程结束后需要选举出一个新的Leader

​			为了使 leader 挂了后系统能正常工作，需要解决以下两个问题

1. 已经被处理的消息不能丢失

![1600848474600](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600848474600.png)

​				当 leader 收到合法数量 follower 的 ACKs 后，就向各个 follower 广播 COMMIT 命令，同时也会在本地执行 COMMIT 并向连接的客户端返回「成功」。但是如果在各个 follower 在收到 COMMIT 命令前 leader就挂了，导致剩下的服务器并没有执行都这条消息。

​			  leader 对事务消息发起 commit 操作，但是该消息在follower1 上执行了，但是 follower2 还没有收到 commit，就已经挂了，而实际上客户端已经收到该事务消息处理成功的回执了。所以在 zab 协议下需要保证所有机器都要执行这个事务消息

 2. 被丢弃的消息不能再次出现

    ​		当 leader 接收到消息请求生成 proposal 后就挂了，其他 follower 并没有收到此 proposal，因此经过恢复模式重新选了 leader 后，这条消息是被跳过的。 此时，之前挂了的 leader 重新启动并注册成了 follower，他保留了被跳过消息的 proposal 状态，与整个系统的状态是不一致的，需要将其删除。

**ZAB 协议需要满足上面两种情况，**就必须要设计一个leader 选举算法：能够确保已经被 leader 提交的事务
Proposal能够提交、同时丢弃已经被跳过的事务Proposal。

#### 	6.4 关于 ZXID

​			为了保证事务的顺序一致性，zookeeper 采用了递增的事	务 id 号（zxid）来标识事务。所有的提议（proposal）都在被提出的时候加上了 zxid。实现中 zxid 是一个 64 位的数字，它高 32 位是 epoch（ZAB 协议通过 epoch 编号来区分 Leader 周期变化的策略）用来标识 leader 关系是否改变，每次一个 leader 被选出来，它都会有一个新的epoch=（原来的 epoch+1），标识当前属于那个 leader 的统治时期。低 32 位用于递增计数。

```java
epoch：可以理解为当前集群所处的年代或者周期，每个
leader 就像皇帝，都有自己的年号，所以每次改朝换代，
leader 变更之后，都会在前一个年代的基础上加 1。这样就算旧的 leader 崩溃恢复之后，也没有人听他的了，因为
follower 只听从当前年代的 leader 的命令。*
```

### 	七 、 leader 选举

#### 		7.1 启动的时候的 leader 选举

​			每个节点启动的时候状态都是 LOOKING，处于观望状态，接下来就开始进行选主流程

​			1 . 每个 Server 发出一个投票。由于是初始情况，Server1和 Server2 都会将自己作为 Leader 服务器来进行投票，每次投票会包含所推举的服务器的myid和ZXID、epoch，使用(myid, ZXID,epoch)来表示，此时 Server1的投票为(1, 0)，Server2 的投票为(2, 0)，然后各自将这个投票发给集群中其他机器

​			2 . 接受来自各个服务器的投票。集群的每个服务器收到投票后，首先判断该投票的有效性，如检查是否是本轮投票（epoch）、是否来自LOOKING状态的服务器。

​			3 . 处理投票。针对每一个投票，服务器都需要将别人的投票和自己的投票进行 PK，PK 规则如下

​					i. 优先检查ZXID。ZXID 比较大的服务器优先作为Leader

​					ii. 如果 ZXID 相同，那么就比较 myid。myid 较大的服务器作为 Leader 服务器。对于 Server1 而言，它的投票是(1, 0)，接收 Server2的投票为(2, 0)，首先会比较两者的 ZXID，均为 0，再比较 myid，此时 Server2 的 myid 最大，于是更新自己的投票为(2, 0)，然后重新投票，对于Server2而言，它不需要更新自己的投票，只是再次向集群中所有机器发出上一次投票信息即可。  

​			4 . 统计投票。每次投票后，服务器都会统计投票信息，判断是否已经有过半机器接受到相同的投票信息，对于 Server1、Server2 而言，都统计出集群中已经有两台机器接受了(2, 0)的投票信息，此时便认为已经选出了 Leader。 

​			5 . 改变服务器状态。一旦确定了 Leader，每个服务器就会更新自己的状态，如果是 Follower，那么就变更为FOLLOWING，如果是 Leader，就变更为 LEADING。

#### 		7.2 leader 崩溃的时候的的选举

​			当集群中的 leader 服务器出现宕机或者不可用的情况时，		那么整个集群将无法对外提供服务，而是进入新一轮的Leader 选举，服务器运行期间的 Leader 选举和启动时期的 Leader 选举基本过程是一致的。	

​		1 . 变更状态。Leader 挂后，余下的非 Observer 服务器都会将自己的服务器状态变更为 LOOKING，然后开
始进入 Leader 选举过程。 

​		2 .  每个 Server 会发出一个投票。在运行期间，每个服务器上的 ZXID 可能不同，此时假定 Server1 的 ZXID 为123，Server3的ZXID为122；在第一轮投票中，Server1和 Server3 都会投自己，产生投票(1, 123)，(3, 122)，然后各自将投票发送给集群中所有机器。接收来自各个服务器的投票。与启动时过程相同。 

​		3 . 处理投票。与启动时过程相同，此时，Server1 将会成为 Leader。

​		4 . 统计投票。与启动时过程相同。

​		5 . 改变服务器的状态。与启动时过程相同

#### 7.3  Leader 选举源码分析

##### 	7.3.1  从入口函数 QUORUMPEERMAIN 开始

![1600849710175](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849710175.png)

![1600849725962](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849725962.png)

![1600849750048](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849750048.png)

![1600849765959](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849765959.png)

启动主线程，QuorumPeer 重写了 Thread.start 方法

![1600849785153](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849785153.png)

##### 7.3.2 调用 QUORUMPEER 的 START 方法

![1600849802238](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849802238.png)

7.3.3 loaddatabase， 主要是从本地文件中恢复数据，以及获取最新的 zxid

![1600849817982](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849817982.png)

##### 7.3.3 初始化 LEADERELECTION

![1600849840780](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849840780.png)

![1600849850823](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849850823.png)

配置选举算法，选举算法有 3 种，可以通过在 zoo.cfg 里面进行配置，默认是 fast 选举

![1600849871585](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849871585.png)

继续看 FastLeaderElection 的初始化动作，主要初始化了业务层的发送队列和接收队列

![1600849893548](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849893548.png)

![1600849905051](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849905051.png)

​			接下来调用 fle.start() , 也就是会调用 FastLeaderElectionstart()方法，该方法主要是对发送线程和接收线程的初始化 ， 左 边 是 FastLeaderElection 的 start ， 右 边 是messager.start()

![1600849933256](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849933256.png)

​			wsThread 和 wrThread 的 初 始 化 动 作 在FastLeaderElection 的 starter 方法里面进行，这里面有两个内部类，一个是 WorkerSender，一个是 WorkerReceiver，负责发送投票信息和接收投票信息

![1600849962387](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849962387.png)

![1600849969947](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849969947.png)

​		然后再回到 QuorumPeer.java。 FastLeaderElection 初始化完成以后，调用 super.start()，最终运行 QuorumPeer 的run 方法

![1600849991161](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600849991161.png)

前面部分主要是做 JMX 监控注册

![1600850006614](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850006614.png)

重要的代码在这个 while 循环里

![1600850025274](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850025274.png)

​		调用 setCurrentVote(makeLEStrategy().lookForLeader());，最终根据策略应该运行 FastLeaderElection 中的选举算法

![1600850045060](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850045060.png)

##### 7.3.4 LOOKFORLEADER 开始选举

![1600850120408](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850120408.png)

![1600850135567](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850135567.png)

![1600850150581](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850150581.png)

![1600850162727](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850162727.png)

![1600850179162](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850179162.png)

![1600850188177](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850188177.png)

![1600850199099](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850199099.png)

![1600850210627](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850210627.png)

![1600850223724](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850223724.png)

![1600850233561](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850233561.png)

![1600850243611](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850243611.png)

##### 7.3.5 消息如何广播，看 SENDNOTIFICATIONS

![1600850263162](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850263162.png)

WORKERSENDER

![1600850279435](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850279435.png)

![1600850288717](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850288717.png)

![1600850298815](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600850298815.png)

##### 7.3.6 FastLeaderElection 选举过程

​				其实在这个投票过程中就涉及到几个类，

FastLeaderElection：FastLeaderElection实现了Election接口，实现各服务器之间基于 TCP 协议进行选举

Notification：内部类，Notification 表示收到的选举投票信息（其他服务器发来的选举投票信息），其包含了被选举者的 id、zxid、选举周期等信息

ToSend：ToSend表示发送给其他服务器的选举投票信息，也包含了被选举者的 id、zxid、选举周期等信息Messenger ： Messenger 包 含 了WorkerReceiver 和WorkerSender 两个内部类；

WorkerReceiver实现了 Runnable 接口，是选票接收器。其会不断地从 QuorumCnxManager 中获取其他服务器发来的选举消息，并将其转换成一个选票，然后保存到recvqueue 中WorkerSender 也实现了 Runnable 接口，为选票发送器，其会不断地从 sendqueue 中获取待发送的选票，并将其传递到底层 QuorumCnxManager 中

### 八 、zookeeper实现分布式锁

#### 		8.1 惊群效应

![1600852149130](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600852149130.png)

​			**不建议使用：**大量客户端监听同一个节点 ，当这个节点状态改变，所有客户端状态都变更，

#### 			8.2 利用有序节点

![1600852203640](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1600852203640.png)

​				特点：每个客户端都会在/locks下写入有序节点，同时**监听比自己小的节点**

