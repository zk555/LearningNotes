package com.forezp.gc;

/**
 * 模拟： 年轻代内存不够直接存入老年代，老年代内存不足发生full gc
 *
 * jvm 参数如下：-XX:NewSize=5242880 -XX:MaxNewSize=5242880 -XX:InitialHeapSize=10485760 -
 * XX:MaxHeapSize=10485760 -XX:SurvivorRatio=8 -XX:PretenureSizeThreshold=10485760 -
 * XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -
 * Xloggc:gc.log
 *
 * -XX:+PrintGCDetils：打印详细的gc日志
 * -XX:+PrintGCTimeStamps：这个参数可以打印出来每次GC发生的时间
 * -Xloggc:gc.log：这个参数可以设置将gc日志写入一个磁盘文件
 */
public class YoungGc {

    public static void main(String[] args) {
        byte[] arry1 = new byte[1024*1024];
        arry1 = new byte[1024*1024];
        arry1 = new byte[1024*1024];
        arry1 = null;

        byte[] arry2 = new byte[2*1024*1024];
        System.out.println("end");
    }
}
