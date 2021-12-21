package com.andon.springbootdistributedlock.exception;

/**
 * @author Andon
 * 2021/12/21
 * <p>
 * 分布式锁异常
 */
public class DistributedLockException extends RuntimeException {

    public DistributedLockException(String message) {
        super(message);
    }
}
