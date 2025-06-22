package ReadWriteLock;


import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockDemo {

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock readerLock = lock.readLock();
    private static final ReentrantReadWriteLock.WriteLock writerLock = lock.writeLock();


    private static int shareSource = 0;

    public static void main(String[] args) {
        ReadWriteLockDemo readWriteLockDemo = new ReadWriteLockDemo();
        for(int i = 0; i < 3; i ++){
            Thread thread = new Thread(() -> {
                readWriteLockDemo.write();
                readWriteLockDemo.read();
                readWriteLockDemo.write();
                readWriteLockDemo.read();

            }, "thead - " + i);
            thread.start();
        }
    }

    public void read(){
        readerLock.lock();
        System.out.println(Util.getThreadName() + " get the lock, and source -> " + shareSource);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            readerLock.unlock();
            System.out.println(Util.getThreadName() + " released the lock");
        }
    }

    public void write(){
        writerLock.lock();
        System.out.println(Util.getThreadName() + " get the lock, and source -> " + shareSource);
        try {
            Thread.sleep(1500);
            shareSource ++;
            System.out.println(Util.getThreadName() + " plus the share");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            writerLock.unlock();
            System.out.println(Util.getThreadName() + " released the lock");
        }
    }



}
