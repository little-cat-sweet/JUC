package CountDownLatch;

import util.Util;

import java.awt.*;
import java.util.concurrent.CountDownLatch;

public class CountDownLatchDemo {

    public static void main(String[] args) {
//        multiTaskRun();
        simulatePressureTest();
    }

    public static void multiTaskRun() {
        CountDownLatch downLatch = new CountDownLatch(3);
        for (int i = 0; i < 3; i++) {
            final int taskId = i;
            new Thread(() -> {
                try {
                    Thread.sleep((long) (Math.random() * 1000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    downLatch.countDown();
                    System.out.println("task : " + taskId + ", has been over, and count -> " + downLatch.getCount());
                }
            }).start();
        }
        System.out.println("main thread needs wait for other child thread to be completed");
        try {
            downLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("all the tasks, which child run has been completed, continue main function");
    }


    //simulate pressure test
    public static void simulatePressureTest() {
        CountDownLatch endLatch = new CountDownLatch(5);
        CountDownLatch startLatch = new CountDownLatch(1);
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    System.out.println(Util.getCurrentThreadName() + " is waiting !");

                    startLatch.await();

                    System.out.println(Util.getCurrentThreadName() + " running !");

                    Thread.sleep(3000);
                    System.out.println(Util.getCurrentThreadName() + " run completed !");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }


            }, String.valueOf(i)).start();
        }

        try {
            //simulating the preparation works.
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        startLatch.countDown();

        try {
            endLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("all works done, start run main way !!!!!");

    }
}
