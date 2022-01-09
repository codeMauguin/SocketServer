package com.test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 7:43 PM 2022/1/8
 * @Modified By:
 */
public class T2 {
    public static void main(String[] args) throws InterruptedException {
        AtomicInteger sun = new AtomicInteger();
        Thread A = new Thread(() -> {
            System.out.println("1" + Thread.currentThread());
            for (int i = 0; i < 10; i++) {
                sun.addAndGet(i);
            }
            LockSupport.park();
            System.out.println(sun.get());
        });
        A.start();
        System.out.println(A);
        Thread.sleep(1000);
        LockSupport.unpark(A);
    }
}
