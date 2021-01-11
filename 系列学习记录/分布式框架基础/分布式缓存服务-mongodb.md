### 一、MB 介绍

​			MongoDB 是一个基于分布式文件存储的数据库。由 C++语言编写。旨在为 WEB 应用提供可扩展的高性能数据存储解决方案。 MongoDB 是一个介于关系数据库和非关系数据库之间的产品，是非关系数据库当中功能最丰富，最像关系数据库的。在这里我们有必要先简单介绍一下非关系型数据库（NoSQL）

#### 1.1 什么是NoSql

​			NoSQL，指的是非关系型的数据库。NoSQL 有时也称作 Not Only SQL 的缩写，是对不同于传统的关系型数据库的数据库管理系统的统称。NoSQL 用于超大规模数据的存储。（例如谷歌或 Facebook 每天为他们的用户收集万亿比特的数据）。这些类型的数据存储不需要固定的模式，无需多余操作就可以横向扩展。

#### 1.2 关系型数据库PK非关系型数据库

![1603852083464](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603852083464.png)

#### 1.3 NoSQL 数据库分类

![1603852423646](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603852423646.png)

![1603852442049](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603852442049.png)

#### 1.4 MongoDB 的数据结构与关系型数据库数据结构对比

![1603852507262](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603852507262.png)

#### 1.5 MongoDB 中的数据类型

![1603955624253](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603955624253.png)

![1603955643658](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603955643658.png)

#### 1.6 图解 MongoDB 底层原理

​		MongoDB 的集群部署方案中有三类角色：实际数据存储结点、配置文件存储结点和路由接入结点。

​		连接的客户端直接与路由结点相连，从配置结点上查询数据，根据查询结果到实际的存储结点上查询和存储数据。MongoDB 的部署方案有单机部署、复本集（主备）部署、分片部署、复本集与分片混合部署。混合的部署方式如图：

![1603956005050](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603956005050.png)

混合部署方式下向 MongoDB 写数据的流程如图：

![1603956159826](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603956159826.png)

混合部署方式下读 MongoDB 里的数据流程如图：

![1603956193109](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603956193109.png)

​		对于复本集，又有主和从两种角色，写数据和读数据也是不同，写数据的过程是只写到主结点中，由主结点以异步的方式同步到从结点中：

![1603956258631](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603956258631.png)

而读数据则只要从任一结点中读取，具体到哪个结点读取是可以指定的：

![1603956275045](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603956275045.png)

对于 MongoDB 的分片，假设我们以某一索引键（ID）为片键，ID 的区间[0,50]，划分成 5 个 chunk，分别存储到 3 个片服务器中，如图所示：

![1603956323908](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603956323908.png)

假如数据量很大，需要增加片服务器时可以只要移动 chunk 来均分数据即可。

配置结点：
存储配置文件的服务器其实存储的是片键与 chunk 以及 chunk 与 server 的映射关系，用上面的数据表
示的配置结点存储的数据模型如下表：

![1603956693169](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603956693169.png)

​	路由结点：路由角色的结点在分片的情况下起到负载均衡的作用。



#### 1.7 MongoDB 的应用场景和不适用场景

##### 1.7.1  适用场景

​		对于 MongoDB 实际应用来讲，是否使用 MongoDB 需要根据项目的特定特点进行一一甄别，这就要求我
们对 MongoDB 适用和不适用的场景有一定的了解。

根据 MongoDB 官网的说明，MongoDB 的适用场景如下:

- 网站实时数据:MongoDB 非常适合实时的插入，更新与查询，并具备网站实时数据存储所需的复制及
  高度伸缩性。
- 数据缓存:由于性能很高，MongoDB 也适合作为信息基础设施的缓存层。在系统重启之后，由 MongoDB
  搭建的持久化缓存层可以避免下层的数据源过载。
- 大尺寸、低价值数据存储:使用传统的关系型数据库存储一些数据时可能会比较昂贵，在此之前，很
  多时候程序员往往会选择传统的文件进行存储。
- 高伸缩性场景:MongoDB 非常适合由数十或数百台服务器组成的数据库。MongoDB 的路线图中已经包
  含对 MapReduce 引擎的内置支持。
- 对象或 JSON 数据存储:MongoDB 的 BSON 数据格式非常适合文档化格式的存储及查询。

##### 1.7.2 不适用场景

了解了 MongoDB 适用场景之后，还需要了解哪些场景下不适合使用 MongoDB，具体如下:

- 高度事务性系统:例如银行或会计系统。传统的关系型数据库目前还是更适用于需要大量原子性复杂
  事务的应用程序。
- 传统的商业智能应用:针对特定问题的 BI 数据库会对产生高度优化的查询方式。对于此类应用，
  数据仓库可能是更合适的选择。
- 需要复杂 SQL 查询的问题。



### 二、MB基本配置及常用命令

#### 2.1 安装 MB数据库

打开官网：https://www.mongodb.com/download-center?jmp=nav#community

选择 Community Server 4.0.1 的版本。

![1603958446812](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603958446812.png)

安装与启动
	在 D 盘创建安装目录，D:\MongoDB，将解压后的文件拷入新建的文件。
	在 D 盘创建一个目录，D:\MongoDB\Server\4.0\Data，用于存放 MongoDB 的数据。
	执行安装，使用命令行，进入 MongDB 的安装目录，执行安装命令，并指明存放 MongoDB 的路径。

![1603958467288](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603958467288.png)

安装完成后配置 环境变量

![1603958632437](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603958632437.png)

![1603958641781](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603958641781.png)

启动数据库

![1603958989555](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603958989555.png)

​		注意，如果这是你的目录中有空格，会报 Invalid command 错误，将 dbpath 后面的值加上双引号即可 mongod.exe -dbpath=”D:\MongoDB\Server\4.0\data”。

​		最后一行显示我们的 MongoDB 已经连接到 27017,它是默认的数据库的端口；它建立完数据库之后，会
在我们的 MongoDbData 文件夹下，生成一些文件夹和文件：在 journal 文件夹中会存储相应的数据文件，NoSQL 的 MongoDB，它以文件的形式，也就是说被二进制码转换过的 json 形式来存储所有的数据模型。

​		启动 MongoDB 数据库，也可以根据自己配置 mongodb.bat 文件，在 D:\MongoDB\Server\4.0\bin 中
创建一个 mongodb.bat 文件，然后我们来编写这个可执行文件如下：mongod --dbpath=D:\MongoDB\Server\4.0\data运行 mongodb.bat 文件，MongoDB 便启动成功！

![1603959759214](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1603959759214.png)



#### 2.2  安装 RoboMongo客户端

![1604567248620](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1604567248620.png)



#### 2.3   MongoDB 常用命令

##### 2.3.1 创建数据库

```shell
use testdb
```

##### 2.3.2 创建集合

```java
db.t_member.insert({name:"zhaomin",age:23})
```

##### 2.3.3 查询

```shell
db.t_member.find()
db.t_member.findOne()
```

##### 2.3.4 修改

```shell
＃不会影响其他属性列 ，主键冲突会报错
db.t_member.update({name:"zhaomin"},{$set:{age:18}})
＃第三个参数为 true 则执行 insertOrUpdate 操作，查询出则更新，没查出则插入，
或者
db.t_member.update({name:"zhaomin"},{$set:{age:18}},true)
```

##### 2.3.5 删除

```shell
＃删除满足条件的第一条 只删除数据 不删除索引
db.t_member.remove({age:1})
＃删除集合
db.t_member.drop();
＃删除数据库
db.dropDatabase();
```

##### 2.3.6 查看集合

```shell
show collections
```

##### 2.3.7 查看数据库

```shell
show dbs
```

##### 2.3.8 插入数据

```java
db.t_member.insert() ＃不允许键值重复
db.t_member.save() ＃若键值重复，可改为插入操作
```

##### 2.3.9 批量更新

```java
db.t_member.update({name:"zhaomin"},{$set:{name:"zhanmin11"}},false,t
rue);
```

批量操作需要和选择器同时使用，第一个 false 表示不执行 insertOrUpdate 操作，第二个 true 表示
执行批量

##### 2.3.10 更新器

使用$set : 指定一个键值对，若存在就进行修改，不存在则添加

$inc ：只使用于数字类型，可以为指定键值对的数字类型进行加减操作：

```shell
db.t_member.update({name:"zhangsan"},{$inc:{age:2}})
```

执行结果是名字叫“zhangsan”的年龄加了 2

$unset : 删除指定的键

```shell
db.t_member.update({name:"zhangsan"},{$unset:{age:1}})
```

$push : 数组键操作：1、如果存在指定的数组，则为其添加值；2、如果不存在指定的数组，则创建数组键，并添加值；3、如果指定的键不为数组类型，则报错；
$addToSet : 当指定的数组中有这个值时，不插入，反之插入

```java
＃则不会添加到数组里
db.t_member.update({name:"zhangsan"},{$addToSet:{classes:"English"}});
```

$pop：删除指定数组的值，当 value=1 删除最后一个值，当 value=-1 删除第一个值

```java
＃删除了最后一个值
db.t_member.update({name:"zhangsan"},{$pop:{classes:1}})
```

$pull : 删除指定数组指定的值

```java
＃$pullAll 批量删除指定数组
db.persons.update({name:"zhangsan"},{$pull:{classes:"Chinese"}})
```

```java
＃若数组中有多个 Chinese，则全删除
db.t_member.update({name:"zhangsan"},{$pull:{classes:["Chinese"]}})
```

$ : 修改指定数组时，若数组有多个对象，但只想修改其中一些，则需要定位器：

```java
db.t_member.update({"classes.type":"AA"},{$set:{"classes.$.sex":"male"}})
```

$addToSet 与 $each 结合完成批量数组更新操作

```java
db.t_member.update({name:"zhangsan"},{$set:{classes:{$each:["chinese","art"]}}})
```

runCommand 函数和 findAndModify 函数

```java
runCommand({
	findAndModify:"persons",
    query:{查询器},
	sort:{排序},
	update:{修改器},
	new:true 是否返回修改后的数据
});
```

​	runCommand 函数可执行 mongdb 中的特殊函数
​	findAndModify 就是特殊函数之一，用于返回执行返回 update 或 remove 后的文档
例如：

```shell
db.runCommand({
	findAndModify:"persons",
	query:{name:"zhangsan"},
	update:{$set:{name:"lisi"}},
	new:true
})
```

##### 3.2.11 高级查询详解

```shell
db.t_member.find({},{_id:0,name:1})
```

第一个空括号表示查询全部数据，第二个括号中值为 0 表示不返回，值为 1 表示返回，默认情况下若不
指定主键，主键总是会被返回；

```shell
db.persons.find({条件},{指定键});
```

比较操作符：$lt: < $lte: <= $gt: > $gte: >= $ne: !=

###### 3.2.11.1 查询条件

```shell
＃查询年龄大于等于 25 小于等于 27 的人
db.t_member.find({age:{$gte:25,$lte:27}},{_id:0,name:1,age:1})
＃查询出所有国籍不是韩国的人的数学成绩
db.t_member.find({country:{$ne:"韩国"}},{_id:0,name:1,country:1})
```

###### 3.2.11.2 包含与不包含（仅针对于数组）

$in 或 $nin

```java
＃查询国籍是中国或美国的学生信息
db.t_member.find({country:{$in:["China","USA"]}},{_id:0,name:1:countr
y:1})
```

###### 3.2.11.3  $or 查询

```shell
＃查询语文成绩大于 85 或者英语大于 90 的学生信息
db.t_member.find({$or:[{c:{$gt:85}},{e:{$gt:90}}]},{_id:0,name:1,c:1,e:1})
＃把中国国籍的学生上增加新的键 sex
db.t_member.update({country:"China"},{$set:{sex:"m"}},false,true)
＃查询出 sex 为 null 的人
db.t_member.find({sex:{$in:[null]}},{_id:0,name:1,sex:1})
```

###### 3.2.11.4 正则表达式

```java
＃查询出名字中存在”li”的学生的信息
db.t_member.find({name:/li/i},{_id:0,name:1})
```

###### 3.2.11.5 $not 的使用

$not 和$nin 的区别是$not 可以用在任何地方儿$nin 是用到集合上的

```java
＃查询出名字中不存在”li”的学生的信息
db.t_member.find({name:{$not:/li/i}},{_id:0,name:1})
```

###### 3.2.11.6 $all 与 index 的使用

```java
＃查询喜欢看 MONGOD 和 JS 的学生
db.t_member.find({books:{$all:["JS","MONGODB"]}},{_id:0,name:1})
＃查询第二本书是 JAVA 的学习信息
db.t_member.find({"books.1":"JAVA"},{_id:0,name:1,books:1})
```

###### 3.2.11.7 $size 的使用，不能与比较查询符同时使用

```java
＃查询出喜欢的书籍数量是 4 本的学生
db.t_member.find({books:{$size:4}},{_id:0,name:1})
```

###### 3.2.11.8 查询出喜欢的书籍数量大于 4 本的学生本的学生

1）增加 size 键

```shell
db.t_member.update({},{$set:{size:4}},false,true)
```

2）添加书籍,同时更新 size

```java
db.t_member.update({name:"jim"},{$push:{books:"ORACL"},$inc:{size:1}
})
```

3）查询大于 3 本的

```java
db.t_member.find({size:{$gt:4}},{_id:0,name:1,size:1})
```

###### 3.2.11.9 $slice 操作符返回文档中指定数组的内部值

```java
＃查询出 Jim 书架中第 2~4 本书
db.t_member.find({name:"jim"},{_id:0,name:1,books:{$slice:[1,3]}})
＃查询出最后一本书
db.t_member.find({name:"jim"},{_id:0,name:1,books:{$slice:-1}})
```

###### 3.2.11.10 文档查询

查询出在 K 上过学且成绩为 A 的学生
1）绝对查询，顺序和键个数要完全符合

```java
db.t_member.find({school:{school:"K","score":"A"}},{_id:0,name:1})
```

2）对象方式,但是会出错，多个条件可能会去多个对象查询

```java
db.t_member.find({"school.school":"K","school.score":"A"},{_id:0,nam
e:1})
```

正确做法单条条件组查询$elemMatch

```java
db.t_member.find({school:{$elemMatch:{school:"K",score:"A"}},{_id:0,n
ame:1})
db.t_member.find({age:{$gt:22},books:"C++",school:"K"},{_id:0,name:1,age:1,books:1,school:1})
```

###### 3.2.11.11 分页与排序

1）limit 返回指定条数 查询出 persons 文档中前 5 条数据：

```JAVA
db.t_member.find({},{_id:0,name:1}).limit(5)
```

2）指定数据跨度 查询出 persons 文档中第 3 条数据后的 5 条数据

```java
db.t_member.find({},{_id:0,name:1}).limit(5).skip(3)
```

3）sort 排序 1 为正序，-1 为倒序

```java
db.t_member.find({},{_id:0,name:1,age:1}).limit(5).skip(3).sort({age:1})
```

注意:mongodb 的 key 可以存不同类型的数据排序就也有优先级
最小值->null->数字->字符串->对象/文档->数组->二进制->对象 ID->布尔->日期->时间戳->正则
->最大值

###### 3.2.11.12 游标

利用游标遍历查询数据

```java
var persons = db.persons.find();
	while(persons.hasNext()){
	obj = persons.next();
	print(obj.name)
}
```

游标几个销毁条件
	1).客户端发来信息叫他销毁
	2).游标迭代完毕
	3).默认游标超过 10 分钟没用也会别清除

###### 3.2.11.13 查询快照

快照后就会针对不变的集合进行游标运动了,看看使用方法.

```java
＃用快照则需要用高级查询
db.persons.find({$query:{name:”Jim”},$snapshot:true})
```

##### 3.2.12 高级查询选项

- $query
- $orderby
- $maxsan：integer 最多扫描的文档数
- $min：doc 查询开始
- $max：doc 查询结束
- $hint：doc 使用哪个索引
- $explain:boolean 统计
- $snapshot:boolean 一致快照

###### 3.2.12.1  查询点(70,180)最近的 3 个点

```java
db.map.find({gis:{$near:[70,180]}},{_id:0,gis:1}).limit(3)
```

###### 3.2.12.2 查询以点(50,50)和点(190,190)为对角线的正方形中的所有的点

```java
db.map.find({gis:{$within:{$box:[[50,50],[190,190]]}}},{_id:0,gis:1})
```

###### 3.2.12.3 查询出以圆心为(56,80)半径为 50 规则下的圆心面积中的点

```java
db.map.find({gis:{$with:{$center:[[56,80],50]}}},{_id:0,gis:1})
```

##### 3.2.13 Count+Distinct+Group

###### 3.2.13.1 count 查询结果条数

```java
db.persons.find({country:"USA"}).count()
```

###### 3.2.13.2 Distinct 去重

请查询出 persons 中一共有多少个国家分别是什么

```java
＃key 表示去重的键
db.runCommand({distinct:"persons",key:"country"}).values
```

###### 3.2.13.3 group 分组

```JAVA
db.runCommand({ group:{
	ns:"集合的名字",
	key:"分组键对象",
	initial:"初始化累加器",
	$reduce:"分解器",
	condition:"条件",
	finalize:"组完成器"
}})
```

分组首先会按照 key 进行分组,每组的 每一个文档全要执行$reduce 的方法,他接收 2 个参数一个是组
内本条记录,一个是累加器数据.
请查出 persons 中每个国家学生数学成绩最好的学生信息(必须在 90 以上)

```java
db.runCommand({
	group:{
		ns:"persons",
		key:{"country":true},
		initial:{m:0},
		$reduce:function(doc,prev){
	if(doc.m>prev.m){
		prev.m = doc.m;
		prev.name = doc.m;
		prev.country = doc.country;
	}
},
        condition:{m:{$gt:90}},
			finalize:function(prev){
			prev.m = prev.name+" comes from "+prev.country+" ,Math score is"+prev.m;
		}
	}
})
```

###### 3.2.13.4 函数格式化分组键

如果集合中出现键 Counrty 和 counTry 同时存在

```java
$keyf:function(doc){
	if(doc.country){
		return {country:doc.country}
	}
	return {country:doc.counTry}
}
```

##### 3.2.14 常用命令举例

###### 3.2.14.1 查询服务器版本号和主机操作系统

```java
db.runCommand({buildInfo:1})
```

###### 3.2.14.2 查询执行集合的详细信息,大小,空间,索引等

```java
db.runCommand({collStats:"persons"})
```

###### 3.2.14.3 查看操作本集合最后一次错误信息

```java
db.runCommand({getLastError:"persons"})
```

##### 3.2.15 固定集合

###### 3.2.15.1 特性

​		固定集合默认是没有索引的就算是_id 也是没有索引的由于不需分配新的空间他的插入速度是非常快的固定集合的顺是确定的导致查询速度是非常快的最适合就是日志管理

###### 3.2.15.2 创建固定集合

创建一个新的固定集合要求大小是 100 个字节,可以存储文档 10 个

```java
db.createCollection("mycoll",{size:100,capped:true,max:10})
```

把一个普通集合转换成固定集合

```java
db.runCommand({convertToCapped:"persons",size:1000})
```

###### 3.2.15.3 对固定集合反向排序，默认情况是插入的顺序排序

```java
db.mycoll.find().sort({$natural:-1})
```

