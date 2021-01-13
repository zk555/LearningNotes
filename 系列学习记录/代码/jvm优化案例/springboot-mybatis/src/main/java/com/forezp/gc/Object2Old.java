package com.forezp.gc;

/**
 * @author zhaokai108
 * @version 0.1.0
 * @description: ，对象是如何在Young GC过后因为放入下Survivor区域，就直接进入老年代了,
 * 有部分对象会留在Survivor中，有部分对象会进入老年代的
 * @create 2021-01-13 09:08
 * @since 0.1.0
 **/
public class Object2Old {
    public static void main(String[] args) {
        byte[] arry1 = new byte[2*1024*1024];
         arry1 = new byte[2*1024*1024*2];
         arry1 = new byte[2*1024*1024*2];


        byte[] arry2 = new byte[128*1024];
        arry2 = null;

        byte[] arry3 = new byte[2*1024*1024];
    }

}
