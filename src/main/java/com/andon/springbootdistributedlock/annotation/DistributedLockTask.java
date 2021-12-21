package com.andon.springbootdistributedlock.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Andon
 * 2021/12/21
 * <p>
 * 分布式锁注解(任务)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLockTask {

    /**
     * 锁key前缀
     */
    String key() default "lock_task";

    /**
     * 锁value
     */
    String value() default "lock_value";

    /**
     * 超时时间
     */
    long timeout() default 10;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;

    /**
     * 被加锁方法执行完是否立即释放锁
     */
    boolean immediatelyUnLock() default true;
}
