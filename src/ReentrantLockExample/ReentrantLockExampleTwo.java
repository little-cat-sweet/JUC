package ReentrantLockExample;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockExampleTwo {

    Lock lock = new ReentrantLock();

    Condition isEmpty = lock.newCondition();
    Condition isFull = lock.newCondition();


    int in = 0;
    int out = 0;
    int count = 0;
    private final String[] buffer = new String[3];


    public void put(String item) throws InterruptedException {
        lock.lock();

        try {
            while (count == buffer.length - 1) {
                System.out.println(getCurrentThreadName() + " buffer is full, provider wait");
                isFull.await();
            }

            buffer[in] = item;
            in = (in + 1) % buffer.length;
            count++;

            System.out.println(getCurrentThreadName() + " put item " + item + ", count " + count);
            isEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    ReentrantLock reentrantLock = new ReentrantLock(true);

    public void fairLock() {
        reentrantLock.lock();
        try {
            System.out.println(getCurrentThreadName() + " got the fair lock");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            System.out.println(getCurrentThreadName() + " released the fair lock");
            reentrantLock.unlock();
        }
    }

    public  void lockState(){
        lock.lock();
        System.out.println(((ReentrantLock) lock).isLocked());
        System.out.println("current thread hold the lock ?  " + ((ReentrantLock) lock).isHeldByCurrentThread());
        System.out.println("is the fair lock " + ((ReentrantLock) lock).isFair());
    }



    public String take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0) {
                System.out.println(getCurrentThreadName() + " buffer is empty, consumer wait");
                isEmpty.await();
            }

            String item = buffer[out];
            out = (out + 1) % buffer.length;
            count--;
            System.out.println(getCurrentThreadName() + "get item" + item + ", count : " + count);

            isFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }


    public void lockOne() {
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " get the lock, and running");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
            System.out.println(Thread.currentThread().getName() + " release the lock");
        }
    }

    public void reentrantFeature() {
        lock.lock();
        System.out.println("first get the lock");
        try {
            lock.lock();
            System.out.println("second get the lock");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
                System.out.println("release second lock");
            }
        } finally {
            lock.unlock();
            System.out.println("release first lock");
        }
    }

    public void tryGetLock() {
        if (lock.tryLock()) {
            try {
                System.out.println(Thread.currentThread().getName() + " try get lock successfully");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
                System.out.println(Thread.currentThread().getName() + " release lock from try get");
            }
        } else {
            System.out.println(Thread.currentThread().getName() + " try get lock failed");
        }
    }

    public void tryGetLockWithTime() throws InterruptedException {
        if (lock.tryLock(1, TimeUnit.SECONDS)) {
            try {
                System.out.println(Thread.currentThread().getName() + " get the lock, before time is over");
                Thread.sleep(5000);
            } finally {
                lock.unlock();
                System.out.println(getCurrentThreadName() + " release the lock");
            }
        } else {
            System.out.println(getCurrentThreadName() + " try get the lock failed, time is over");
        }
    }

    public void lockInterruptibly() {
        try {
            lock.lockInterruptibly();

            try {
                System.out.println(getCurrentThreadName() + " get the interruptibly lock");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println(getCurrentThreadName() + " was interrupted, when it hold the interruptibly lock");
                Thread.currentThread().interrupt();
            } finally {
                if (((ReentrantLock) lock).isHeldByCurrentThread()) {
                    lock.unlock();
                    System.out.println(getCurrentThreadName() + " released the interruptibly lock !");
                }
            }
        } catch (InterruptedException e) {
            System.out.println(getCurrentThreadName() + " was interrupted, when it gets the lock !");
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
//        testPC();
        testFairLock();

    }

    public static void testTryLockWithTime() {
        logHeader("test try lock with time");
        ReentrantLockExampleTwo reentrantLockExampleTwo = new ReentrantLockExampleTwo();
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(() -> {
                try {
                    reentrantLockExampleTwo.tryGetLockWithTime();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "t-" + i);
            thread.start();
        }
    }

    public static void testNormalLock() throws InterruptedException {
        logHeader("test normal lock way");
        ReentrantLockExampleTwo reentrantLockExampleTwo = new ReentrantLockExampleTwo();

        Thread thread1 = new Thread(reentrantLockExampleTwo::lockOne, "t1");
        Thread thread2 = new Thread(reentrantLockExampleTwo::lockOne, "t2");
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
    }

    public static void testReInLock() {
        logHeader("test re in lock");
        ReentrantLockExampleTwo reentrantLockExampleTwo = new ReentrantLockExampleTwo();
        reentrantLockExampleTwo.reentrantFeature();
    }

    public static void testTryLock() {
        logHeader("test try lock");
        ReentrantLockExampleTwo reentrantLockExampleTwo = new ReentrantLockExampleTwo();
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(reentrantLockExampleTwo::tryGetLock, "t-" + i);
            thread.start();
        }
    }


    public static void testInterruptible() throws InterruptedException {

        ReentrantLockExampleTwo reentrantLockExampleTwo = new ReentrantLockExampleTwo();
        logHeader("test interruptibly lock");

        Thread thread1 = new Thread(reentrantLockExampleTwo::lockInterruptibly, "Thread-1");
        thread1.start();
        Thread.sleep(100); // 确保thread1先获取到锁

        // 启动线程2
        Thread thread2 = new Thread(reentrantLockExampleTwo::lockInterruptibly, "Thread-2");
        thread2.start();
        Thread.sleep(100); // 让thread2进入等待锁的状态

        // 中断线程2
        System.out.println("主线程中断 Thread-2");
        thread2.interrupt();
    }

    public static void testPC() throws InterruptedException {

        logHeader("test provider and consumer");
        ReentrantLockExampleTwo reentrantLockExampleTwo = new ReentrantLockExampleTwo();
        Thread provider = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    reentrantLockExampleTwo.put("item-" + i);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Provider");

        Thread consumer = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    reentrantLockExampleTwo.take();
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Consumer");

        provider.start();
        consumer.start();
        provider.join();
        consumer.join();
    }

    public static void testFairLock() {
        ReentrantLockExampleTwo reentrantLockExampleTwo = new ReentrantLockExampleTwo();
        logHeader("test fair lock");
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(reentrantLockExampleTwo::fairLock, "fair - lock " + i);
            thread.start();
        }
    }

    private static void logHeader(String msg) {
        System.out.println("--------------------" + msg + "----------------------");
    }

    private static String getCurrentThreadName() {
        return Thread.currentThread().getName();
    }


}