package com.andon.springbootdistributedlock;

import com.andon.springbootdistributedlock.task.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Andon
 * 2021/12/21
 */
@Slf4j
@Component
public class Main implements ApplicationRunner {

    @Resource
    private ThreadPoolExecutor globalExecutorService;
    @Resource
    private TaskService taskService;

    @Override
    public void run(ApplicationArguments args) {
        for (int i = 0; i < 6; i++) {
            System.out.println("Run!!");
        }

        globalExecutorService.execute(() -> taskService.task01());
    }
}
