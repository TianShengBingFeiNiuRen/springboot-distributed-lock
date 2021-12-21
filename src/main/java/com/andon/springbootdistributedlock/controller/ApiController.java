package com.andon.springbootdistributedlock.controller;

import com.andon.springbootdistributedlock.annotation.DistributedLockApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author Andon
 * 2021/12/21
 */
@Slf4j
@RequestMapping(value = "/api")
@RestController
public class ApiController {

    @DistributedLockApi(timeout = 20, timeUnit = TimeUnit.SECONDS, immediatelyUnLock = false)
    @GetMapping(value = "/test")
    public String test() {
        log.info("test!!");
        return "test!!";
    }
}
