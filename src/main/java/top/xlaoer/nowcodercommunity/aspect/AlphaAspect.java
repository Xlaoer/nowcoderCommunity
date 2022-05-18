package top.xlaoer.nowcodercommunity.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author Xlaoer
 * @date 2022/5/18 23:18
 */
//@Component
//@Aspect
public class AlphaAspect {

    // 切点
    // *代表方法的返回值
    // top.xlaoer.nowcodercommunity.service.*.*(..)) 表示service包下面的所有类的（任意参数）所有方法
    @Pointcut("execution(* top.xlaoer.nowcodercommunity.service.*.*(..))")
    public void pointcut() {

    }

    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }

    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }

    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }

    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{

        System.out.println("aroundBefore");

        // 调用目标组件的方法
        // obj为方法的返回值
        Object obj = joinPoint.proceed();

        System.out.println("aroundAfter");

        return obj;
    }

}