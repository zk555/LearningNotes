#### 1.Mysql的体系结构

##### 	1.1 索引是谁实现的？

​		  存储引擎实现，InnoDb、MyIsam

####    1.2 正确的创建合适的索引，有利于加快查询速度

​		索引是为了加速对表中数据行的检索而创建的一种分散存储的数据结构

![](D:\java\idea\workspace\views\images\索引数据结构.png)

#### 	1.3 为什么要使用索引？

​		加快存储引起搜索的时间

​		索引可以把随机IO变成顺序IO （子节点引用的作用）

​		索引可以帮助我们进行分组、排序等操作时，避免使用临时表

####     1.4 为什么时B+tree

​			二叉树不足：

​							（数据分布）可能形成一个链表（每次插入节点都比上个节点大），检索时相当于全表

![](D:\java\idea\workspace\views\images\二叉树结构.png)

​			平衡二叉树不足：

​							1、 数据的高度决定着IO的次数，IO消耗很大

​							2、每一个磁盘块保存的数据量太小了（满足不了页的大小4K）

![](D:\java\idea\workspace\views\images\平衡二叉树.png)

​			B-tree ：

​						一个磁盘块相当于一个页的大小，16k * 1024byte 每页大小

​						减少了搜索的深度

![](D:\java\idea\workspace\views\images\B+TREE.png)

B+tree:

![](D:\java\idea\workspace\views\images\B+TREE1.png)

#### 2.理解Mysql底层B+tree索引机制

​	1.查询效率高，可以充分利用页空间，搜索深度减少

​	2.数据的高度决定着IO的次数，IO消耗很大

3. 解决数据分布问题
4. 数据分布有序



#### 3.聚集索引

​	数据库表行中数据的物理顺序与键的逻辑顺序相同



#### 4.InnoDb和MyIsam索引对比

![](D:\java\idea\workspace\views\images\InnoDb和myIsam比对.png)



#### 5.列的离散性Count(distinct col) ：count(col)

​	离散性越高，性能越好

#### 6.最左匹配原则

​			联合索引：节点中关键字：【name,phoneNum】

​			经常用的列优先，

​			覆盖索引：减少叶子节点IO ，将随机IO变为顺序IO

​								SELECT  index1 index2 from table

#### 7.索引不到情况

![](D:\java\idea\workspace\views\images\索引不到情况.png)

​			like ； 离散性的原因

#### 8.Mysql的结构体系

![](D:\java\idea\workspace\views\images\Mysql体系结构.png)

​		Connection Pool ：链接，认证等

​		Sql Interface :  sq请求的接受，返回结果

​		Parser : 解析器，sql解析

​		Optimizer : 执行计划优化，选出最优的执行计划

​		Caches : 缓存器，sql的缓存

#### 9.存储引擎的介绍

##### 		9.1 CSV存储引擎

​			数据存储以CSV文件，

​				特点：不能定义没有索引，列定义必须为not  null ，不能设置自增列

​							不适应于大表或者数据的在线处理

​		应用场景：数据的快速导入导出，

​							表格的直接转换CSV



##### 	  9.2 Archive存储引擎

​				压缩协议进行数据的存储

​			   数据存储为ARZ文件格式

​				特点：

​						 只支持insert合select两种格式

​						 只允许自增ID列建立索引

​						 行级锁

​						不支持事务

​						 数据磁盘空间占用小

​				应用场景：

​						日志系统

​						大量的设备数据采集

##### 	9.3 MyIsam 存储引擎

​				特点：

​						表级锁，

​						不支持事务

​						

#####  9.4 InnoDb

​				支持事务

​			    行级锁

​			     剧集索引方式存储数据

​				支持外键关系保证数据完整性

### 10. Mysql 查询优化器

![](D:\java\idea\workspace\views\images\查询优化流程.png)

​		mysql客户端/服务端通信 --> 查询缓存 -- >查询优化处理 -- > 查询执行引擎 -- >返回客户端

1. 通信方式 ：半双工   A --- B 一次只能一个方向
2. 半双工、全双工、单工

##### 10.1 查看一个线程连接在做什么？

​		show full processlist/show processlist

 				sleep ：线程正在等待客服端发送数据

​				Query :  连接线程正在执行查询

​				locked :  线程正在等待表锁释放

​				Sorting result : 线程正在对结果排序

​				sendng Data : 向请求端返回数据

 对于出现问题的连接,可以通过kill (id)杀掉

###### 	10.1.1查询缓存：

###### ![](D:\java\idea\workspace\views\images\mysql缓存.png)	

​		工作原理：

​					缓存SELECT的查询结果

​					新的SQL会查询缓存，判断是否有    ![](D:\java\idea\workspace\views\images\缓存命中相关信息.png)

###### 	10.1.2 刷新缓存时机：

​		全表某个数据更新都会刷新缓存

###### 	10.1.3 查询优化处理

​			解析Sql: 

​			预处理阶段：查询表是否存在，等等

​			查询优化器： 制定最优的查询计划

   1 . 使用等价优化器

   2. 将可转换的外连接查询变成内连接查询

   3. 优化count()\mix等函数，MySQL会有额外的存储

   4. 覆盖索引扫描

   5. 子查询优化

   6. in的优化 ，mySql中如果写or的时候要用in替换

      ​					in先去排序再利用二分法查找是否在条件中，如果命中则跳过

      ​					or全部都需要比对  

      

      ###### 10.1.4 执行计划-id

      ​		查询的序列号：标识执行的顺序

      ​	ID相同，执行顺序由上倒下

      ​	ID不同，如果是子查询，ID的序列号会递增，id值越大优先级越高，越先被执行

      ###### 10.1.5 执行计划 -select-type

      ![](D:\java\idea\workspace\views\images\SelectType查询计划.png)

      ###### 10.1.6 执行计划 -type

![](D:\java\idea\workspace\views\images\type执行计划.png)

285957505