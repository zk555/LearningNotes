mysql:
1. 掌握了为什么选择B+tree作为索引
2. B+tree索引在innoDb 和MyIsam中的不同实现
3. 索引类型，聚簇索引、最左原则等
4. 事务隔离级别 + 事务并发带来的问题
5. 如何解决事务并发带来的问题
6. 锁，排他锁+共享锁，意向排它锁，临建锁+间隙锁+记录锁
7. innoDb 怎么利用锁来解决（脏读、不可重复读、幻读）
8. mvcc 的 插入、删除、更新、读取对应的版本事务ID和删除ID的变化
9. undu log 是干什么的？快照读和回滚日志？
10.redo log 做什么的？ 为什么会出现？

jvm:
1. jvm内存模型的分代
2.  class文件怎么加载到JVM中运行的
3. 类加载机制，双亲委派模型
4. 堆内存分为什么？为什么出现两个S区?
5. 年轻代、老年代和永久代都指定是什么？存的是什么对象
6. 年轻代对象什么时候会到老年代
7. 年轻代的垃圾回收算法和垃圾回收器?
8. cms垃圾回收器的特点？
9. G1垃圾回收器的特点？
10. 什么时候触发yuong Gc ？ old GC？混合GC？
11. JVM常用参数
12. 怎么估算一个系统的每秒产生对象? 如何调整内存的比例？

截至2021-01-12 掌握内容