package ReentrantLockExample;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockExampleOne {
    // 创建一个可重入锁实例
    private final Lock lock = new ReentrantLock();

    // 创建条件变量，用于线程间通信
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    // 共享资源和状态变量
    private final String[] buffer = new String[5];
    private int count = 0;
    private int in = 0;
    private int out = 0;

    // 1. 基本的锁获取和释放
    public void basicLockExample() {
        lock.lock();
        try {
            // 临界区代码
            System.out.println("线程 " + Thread.currentThread().getName() + " 获取了锁");
            // 模拟一些工作w
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // 确保锁在任何情况下都能被释放
            lock.unlock();
            System.out.println("线程 " + Thread.currentThread().getName() + " 释放了锁");
        }
    }

    // 2. 可重入特性
    public void reentrantFeature() {
        lock.lock();
        try {
            System.out.println("第一次获取锁");
            // 再次获取同一个锁
            lock.lock();
            try {
                System.out.println("重入锁成功，锁计数为2");
                // 模拟一些工作
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // 释放第二次获取的锁
                lock.unlock();
                System.out.println("释放第二次获取的锁，锁计数为1");
            }
        } finally {
            // 释放第一次获取的锁
            lock.unlock();
            System.out.println("释放第一次获取的锁，锁计数为0");
        }
    }

    // 3. 尝试获取锁
    public void tryLockExample() {
        if (lock.tryLock()) {
            try {
                System.out.println("线程 " + Thread.currentThread().getName() + " 成功获取锁");
                // 执行临界区代码
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
                System.out.println("线程 " + Thread.currentThread().getName() + " 释放锁");
            }
        } else {
            System.out.println("线程 " + Thread.currentThread().getName() + " 未能获取锁，执行其他操作");
            // 执行其他非临界区代码
        }
    }

    // 4. 带超时的尝试获取锁
    public void tryLockWithTimeoutExample() throws InterruptedException {
        if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
            try {
                System.out.println("线程 " + Thread.currentThread().getName() + " 在超时前获取了锁");
                // 执行临界区代码
            } finally {
                lock.unlock();
                System.out.println("线程 " + Thread.currentThread().getName() + " 释放了锁");
            }
        } else {
            System.out.println("线程 " + Thread.currentThread().getName() + " 超时，未能获取锁");
        }
    }

    // 5. 可中断的锁获取
    public void lockInterruptiblyExample() {
        try {
            lock.lockInterruptibly();
            try {
                System.out.println("线程 " + Thread.currentThread().getName() + " 获取了可中断锁");
                // 执行临界区代码
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("线程 " + Thread.currentThread().getName() + " 在持有锁期间被中断");
                Thread.currentThread().interrupt();
            } finally {
                if (((ReentrantLock) lock).isHeldByCurrentThread()) {
                    lock.unlock();
                    System.out.println("线程 " + Thread.currentThread().getName() + " 释放了可中断锁");
                }
            }
        } catch (InterruptedException e) {
            System.out.println("线程 " + Thread.currentThread().getName() + " 在获取锁时被中断");
            Thread.currentThread().interrupt();
        }
    }

    // 6. 使用条件变量实现生产者-消费者模式
    public void put(String item) throws InterruptedException {
        lock.lock();
        try {
            // 等待缓冲区不满
            while (count == buffer.length) {
                System.out.println("缓冲区已满，生产者等待...");
                notFull.await();
            }

            // 添加元素到缓冲区
            buffer[in] = item;
            in = (in + 1) % buffer.length;
            count++;

            System.out.println("生产者 " + Thread.currentThread().getName() + " 放入: " + item + ", 当前数量: " + count);

            // 通知消费者
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public String take() throws InterruptedException {
        lock.lock();
        try {
            // 等待缓冲区不为空
            while (count == 0) {
                System.out.println("缓冲区为空，消费者等待...");
                notEmpty.await();
            }

            // 从缓冲区取出元素
            String item = buffer[out];
            out = (out + 1) % buffer.length;
            count--;

            System.out.println("消费者 " + Thread.currentThread().getName() + " 取出: " + item + ", 当前数量: " + count);

            // 通知生产者
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }

    // 7. 公平锁模式
    private final Lock fairLock = new ReentrantLock(true); // 创建公平锁

    public void fairLockExample() {
        fairLock.lock();
        try {
            System.out.println("公平锁由线程 " + Thread.currentThread().getName() + " 获取");
            // 执行临界区代码
        } finally {
            fairLock.unlock();
        }
    }

    // 8. 锁状态查询
    // 8. 锁状态查询
    public void lockStateExample() {
        lock.lock();
        try {
            System.out.println("锁是否被持有: " + ((ReentrantLock) lock).isLocked());
            System.out.println("是否当前线程持有锁: " + ((ReentrantLock) lock).isHeldByCurrentThread());
            System.out.println("是否是公平锁: " + ((ReentrantLock) lock).isFair());
            System.out.println("等待获取锁的线程数: " + ((ReentrantLock) lock).getQueueLength());
            System.out.println("等待特定条件的线程数: " + ((ReentrantLock) lock).getWaitQueueLength(notEmpty));
        } finally {
            lock.unlock();
        }
    }

    // 主方法用于测试
    public static void main(String[] args) throws InterruptedException {
        ReentrantLockExampleOne example = new ReentrantLockExampleOne();

        // 测试基本锁功能
        Thread t1 = new Thread(example::basicLockExample, "Thread-1");
        Thread t2 = new Thread(example::basicLockExample, "Thread-2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("\n--- 测试可重入特性 ---");
        example.reentrantFeature();

        System.out.println("\n--- 测试尝试获取锁 ---");
        t1 = new Thread(example::tryLockExample, "Thread-1");
        t2 = new Thread(example::tryLockExample, "Thread-2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("\n--- 测试带超时的尝试获取锁 ---");
        t1 = new Thread(() -> {
            try {
                example.tryLockWithTimeoutExample();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Thread-1");
        t2 = new Thread(() -> {
            try {
                example.tryLockWithTimeoutExample();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Thread-2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("\n--- 测试可中断的锁获取 ---");
        t1 = new Thread(example::lockInterruptiblyExample, "Thread-1");
        t2 = new Thread(example::lockInterruptiblyExample, "Thread-2");
        t1.start();
        t2.start();

        // 中断第二个线程
        Thread.sleep(200);
        t2.interrupt();
        t1.join();
        t2.join();

        System.out.println("\n--- 测试生产者-消费者模式 ---");
        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    example.put("Item-" + i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    example.take();
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();

        System.out.println("\n--- 测试公平锁 ---");
        for (int i = 0; i < 5; i++) {
            new Thread(example::fairLockExample, "Fair-Thread-" + i).start();
        }

        System.out.println("\n--- 测试锁状态查询 ---");
        example.lockStateExample();
    }

}