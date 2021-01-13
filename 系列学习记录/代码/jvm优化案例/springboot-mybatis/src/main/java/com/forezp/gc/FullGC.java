package com.forezp.gc;

/**
 * @author zhaokai108
 * @version 0.1.0
 * @description: “-XX:NewSize=10485760 -XX:MaxNewSize=10485760 -XX:InitialHeapSize=20971520 -XX:MaxHeapSize=20971520 -
 * XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=15 -XX:PretenureSizeThreshold=3145728 -XX:+UseParNewGC -
 * XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:gc.log
 * @create 2021-01-13 09:30
 * @since 0.1.0
 **/
public class FullGC {
    public static void main(String[] args) {
        byte[] arry1 = new byte[4*1024*1024];
        arry1 =null; //直接进入老年代
        byte[] arry2 = new byte[2*1024*1024*2];
        byte[] arry3 = new byte[2*1024*1024*2];
        byte[] arry4 = new byte[2*1024*1024*2];
        byte[] arry5 = new byte[128*1024];
        //年轻代分配6.128M


        byte[] arry6 = new byte[2*1024*1024]; //放不下，进行young GC
        //6M直接会进入老年代
        // young GC 过后老年代空间只剩4M不足存下6M， old GC ，8M-6M

    }
}
