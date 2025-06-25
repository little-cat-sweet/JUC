package Semaphore;

import util.Util;

import java.util.concurrent.Semaphore;

public class SemaphoreDemo {

    public static void main(String[] args) {
//        demoForLimitSource();
//        demoForProducerAndConsumer();
        try {
            mutexDemo();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //this is show how semaphore limit the threads source access
    public static void demoForLimitSource() {
        Semaphore semaphore = new Semaphore(3);
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                try {
                    semaphore.acquire();
                    System.out.println(Util.getCurrentThreadName() + " get the right");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    System.out.println(Util.getCurrentThreadName() + " release the right");
                    semaphore.release();
                }
            }, "Thread - " + i);
            thread.start();
        }
    }

    //implement producer and consumer model

    private static final int MAX_BUFFER_SIZE = 4;
    private static final Semaphore fullSlots = new Semaphore(0);
    private static final Semaphore emptySlots = new Semaphore(MAX_BUFFER_SIZE);
    private static int source = 0;

    public static void demoForProducerAndConsumer() {
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    emptySlots.acquire();
                    synchronized (SemaphoreDemo.class) {
                        source++;
                        System.out.println(Util.getCurrentThreadName() + " source : " + source);
                    }
                    fullSlots.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Producer").start();

        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    fullSlots.acquire();
                    synchronized (SemaphoreDemo.class) {
                        source--;
                        System.out.println(Util.getCurrentThreadName() + " source : " + source);
                    }
                    emptySlots.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Consumer").start();

    }

    private static final Semaphore mutex = new Semaphore(1);
    static int count = 0;

    //simulate to
    public static void mutexDemo() throws InterruptedException {
        Thread thread1 = new Thread(() -> increment(), "t1");
        Thread thread2 = new Thread(() -> increment(), "t2");
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        System.out.println("count : " + count);
    }

    public static void increment() {
        for (int i = 0; i < 10; i++) {
            try {
                mutex.acquire();
                count++;
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                mutex.release();
            }
        }
    }
}
