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

    public static void main(String[] args) throws InterruptedException {
        ReentrantLockExampleTwo reentrantLockExampleTwo = new ReentrantLockExampleTwo();

        logHeader("test normal lock way");
        Thread thread1 = new Thread(reentrantLockExampleTwo::lockOne, "t1");
        Thread thread2 = new Thread(reentrantLockExampleTwo::lockOne, "t2");
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        logHeader("test re in lock");
        reentrantLockExampleTwo.reentrantFeature();

        logHeader("test try lock");
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(reentrantLockExampleTwo::tryGetLock, "thread" + i);
            thread.start();
        }

        Thread.sleep(4000);

        logHeader("test try lock with time");
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(() -> {
                try {
                    reentrantLockExampleTwo.tryGetLockWithTime();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "thread " + i);
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