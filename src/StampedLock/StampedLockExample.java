package StampedLock;

import util.Util;

import java.util.concurrent.locks.StampedLock;

public class StampedLockExample {

    public static void main(String[] args) {
        Point point = new Point(3, 4);
        new Thread(() -> {
            for(int i = 0; i < 3; i ++){
                point.move(1, 1);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Writer").start();


        new Thread(() -> {
            for(int i = 0; i < 5; i ++){
                point.tryDistance();
                point.getDistance();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Reader").start();
    }
}

class Point {
    private double x, y;
    private final StampedLock stampedLock = new StampedLock();

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void move(double x, double y) {
        long stamp = stampedLock.writeLock();
        try {
            System.out.println(Util.getCurrentThreadName() + " get the lock, and start move the location");
            this.x += x;
            this.y += y;
            System.out.println(Util.getCurrentThreadName() + " moved the location, and x -> " + x + ", y -> " + y);
        } finally {
            //use stamp to check lock status.
            stampedLock.unlock(stamp);
            System.out.println(Util.getCurrentThreadName() + " released the lock.");
        }
    }

    public double getDistance(){
        long stamp = stampedLock.readLock();
        try {
            System.out.println(Util.getCurrentThreadName() + " get the read lock !");
            return Math.sqrt(x * x + y * y);
        } finally {
            stampedLock.unlock(stamp);
            System.out.println(Util.getCurrentThreadName() + " released the read lock !");
        }
    }


    public double tryDistance(){
        long stamp = stampedLock.tryOptimisticRead();

        double currentX = x;
        double currentY = y;
        if(! stampedLock.validate(stamp)){
            System.out.println(Util.getCurrentThreadName() + " optimistic lock failed, upgrade to negative lock !");
            stamp = stampedLock.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                stampedLock.unlock(stamp);
                System.out.println(Util.getCurrentThreadName() + " released the negative lock!");
            }
        }else {
            System.out.println(Util.getCurrentThreadName() + " optimistic valid, just count !");
        }

        return Math.sqrt(currentX * currentY + currentY * currentY);
    }


    @Override
    public String toString() {
        return "x " + x + ", y " + y;
    }
}