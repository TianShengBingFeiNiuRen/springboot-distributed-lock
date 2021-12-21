package com.andon.springbootdistributedlock.task;

import com.andon.springbootdistributedlock.annotation.DistributedLockTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author Andon
 * 2021/12/21
 */
@Slf4j
@Service
public class TaskService {

    @DistributedLockTask(timeout = 1, timeUnit = TimeUnit.DAYS, immediatelyUnLock = false)
    public void task01() {
        log.info("task01 run!!");
    }
}
