package com.forezp.gc;
/*
“-XX:NewSize=10485760 -XX:MaxNewSize=10485760 -XX:InitialHeapSize=20971520 -XX:MaxHeapSize=20971520 -
XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=15 -XX:PretenureSizeThreshold=10485760 -XX:+UseParNewGC -
XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:gc.log
*/

public class young2old {
    public static void main(String[] args) {
        byte[] arry1 = new byte[2*1024*1024];
        arry1 = new byte[1024*1024*2];
        arry1 = new byte[1024*1024*2];
        arry1 = null;

        byte[] arry2 = new byte[128*1024];
        byte[] arry3 = new byte[2*1024*1024];
         arry3 = new byte[2*1024*1024];
         arry3 = new byte[2*1024*1024];
         arry3 = null;
// 第一次回收S1 --> S2这个时候 年龄+1
        byte[] arry4 = new byte[2*1024*1024];
        System.out.println("end");
    }
}
