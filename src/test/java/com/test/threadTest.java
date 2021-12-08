package com.test;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 陈浩
 * @slogan: Talk is cheap. Show me the code.
 * @Date: created in 6:59 下午 2021/12/6
 * @Modified By:
 */
public class threadTest {


    public static void main(String[] args) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(1000);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1000, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                new SynchronousQueue<>(),

                r -> new Thread(Thread.currentThread().getThreadGroup(), r, "test"));
        AtomicInteger errIndex = new AtomicInteger(0);
        AtomicInteger sucIndex = new AtomicInteger(0);
        CloseableHttpClient build = HttpClients.createDefault();
        AtomicLong start = new AtomicLong();
        AtomicLong end = new AtomicLong();
        for (int i = 0; i < 1000; i++) {
            int finalI = 81 + i;
            executor.execute(() -> {
                System.out.println(Thread.currentThread().getId() + "准备就绪");
                try {
                    cyclicBarrier.await();
                    HttpGet get = new HttpGet("http://127.0.0.1/api?id=true");
                    CloseableHttpResponse
                            response = build.execute(get);
                    System.out.println(EntityUtils.toString(response.getEntity()));
                } catch (Exception e) {
                    errIndex.getAndIncrement();
                    e.printStackTrace();
                    return;
                }
                if ((finalI - 81) == 999) {
                    end.set(System.nanoTime());
                }
                sucIndex.getAndIncrement();
            });
            if (i == 999) {
                start.set(System.nanoTime());
            }
        }
        executor.shutdown();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println((end.get() - start.get()));
        System.out.println(errIndex.get());
        System.out.println();
        System.out.println(sucIndex.get());
    }
}
