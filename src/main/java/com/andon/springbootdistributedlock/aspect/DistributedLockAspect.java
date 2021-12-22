package com.andon.springbootdistributedlock.aspect;

import com.alibaba.fastjson.JSONObject;
import com.andon.springbootdistributedlock.annotation.DistributedLockApi;
import com.andon.springbootdistributedlock.annotation.DistributedLockTask;
import com.andon.springbootdistributedlock.exception.DistributedLockException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

/**
 * @author Andon
 * 2021/12/21
 * <p>
 * 分布式锁切面
 */
@SuppressWarnings("DuplicatedCode")
@Slf4j
@Aspect
@Component
public class DistributedLockAspect {

    /**
     * StringRedisTemplate
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 任务切点
     */
    @Pointcut("@annotation(com.andon.springbootdistributedlock.annotation.DistributedLockTask)")
    public void taskPointCut() {
    }

    /**
     * API切点
     */
    @Pointcut("@annotation(com.andon.springbootdistributedlock.annotation.DistributedLockApi)")
    public void apiPointCut() {
    }

    /**
     * 任务加分布式锁
     */
    @Around(value = "taskPointCut() && @annotation(distributedLockTask)")
    public Object taskAround(ProceedingJoinPoint pjp, DistributedLockTask distributedLockTask) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        // 获取目标类名方法名
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        // 类名方法名作为分布式锁的key
        String key = distributedLockTask.key() + "_" + className + "_" + methodName;
        // 获取锁
        Boolean status = getLock(key, distributedLockTask.value(), distributedLockTask.timeout(), distributedLockTask.timeUnit(), distributedLockTask.waitLockSecondTime());
        if (!ObjectUtils.isEmpty(status) && status.equals(Boolean.TRUE)) {
            try {
                Object proceed = pjp.proceed();
                // 释放锁
                if (distributedLockTask.immediatelyUnLock()) { //是否立即释放锁
                    unLock(key);
                }
                return proceed;
            } catch (Throwable throwable) {
                log.error("key failure!! key{} error:{}", key, throwable.getMessage());
            }
        }
        log.warn("getLock failure!! key{} 已执行!!", key);
        return null;
    }

    /**
     * API加分布式锁
     */
    @Around(value = "apiPointCut() && @annotation(distributedLockApi)")
    public Object apiAround(ProceedingJoinPoint pjp, DistributedLockApi distributedLockApi) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String remoteHost = request.getRemoteHost(); //访问者ip
        String method = request.getMethod(); //请求方式
        String uri = request.getRequestURI(); //请求路径
        Enumeration<String> headerNames = request.getHeaderNames();
        JSONObject headers = new JSONObject();
        while (headerNames.hasMoreElements()) {
            String s = headerNames.nextElement();
            headers.put(s, request.getHeader(s)); //请求头
        }
        log.info("headers:{}", headers.toJSONString());
        Object[] args = pjp.getArgs(); //获取入参
        // 类名方法名作为分布式锁的key
        String key = distributedLockApi.key() + "_" + method + "_" + uri + "_" + JSONObject.toJSONString(args);
        // 获取锁
        Boolean status = getLock(key, distributedLockApi.value(), distributedLockApi.timeout(), distributedLockApi.timeUnit(), distributedLockApi.waitLockSecondTime());
        if (!ObjectUtils.isEmpty(status) && status.equals(Boolean.TRUE)) {
            try {
                Object proceed = pjp.proceed();
                // 释放锁
                if (distributedLockApi.immediatelyUnLock()) { //是否立即释放锁
                    unLock(key);
                }
                return proceed;
            } catch (Throwable throwable) {
                log.error("key failure!! key{} error:{}", key, throwable.getMessage());
            }
        }
        log.warn("getLock failure!! key{} 已执行!!", key);
        throw new DistributedLockException("请勿重复操作!!");
    }

    /**
     * 获取锁
     */
    private Boolean getLock(String key, String value, long timeout, TimeUnit unit, long waitLockSecondTime) {
        Boolean status = null;
        try {
            long endTime = System.currentTimeMillis() + waitLockSecondTime * 1000;
            do {
                status = stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
                Thread.sleep(50);
            } while (System.currentTimeMillis() - endTime < 0 && (ObjectUtils.isEmpty(status) || !status.equals(Boolean.TRUE)));
        } catch (Exception e) {
            log.error("getLock failure!! error:{}", e.getMessage());
        }
        return status;
    }

    /**
     * 释放锁
     */
    private void unLock(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.error("unLock failure!! error:{}", e.getMessage());
        }
    }
}
