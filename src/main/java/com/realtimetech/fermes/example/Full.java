package com.realtimetech.fermes.example;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Full {
    public static void executeMain(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
        Method method = clazz.getMethod("main", (String[].class));

        System.out.println("===============================");
        System.out.println("\t " + clazz.getSimpleName());
        System.out.println("===============================");
        method.invoke(clazz, new Object[]{new String[0]});

        System.out.println();
        System.out.println();
        System.out.println();
        Thread.sleep(3000);
    }

    public static void main(String[] args) throws InterruptedException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        executeMain(Example.class);

        executeMain(TestForNormal.class);
        executeMain(TestForRemoval.class);

        executeMain(TestForMemory.class);

        // It's fucking not stopping
        // executeMain(TestForMemoryLock.class);
        executeMain(TestForSaveLock.class);

        executeMain(TestForSaveIncreaseException.class);
        executeMain(TestForCloseException.class);
        executeMain(TestForRemoveAccessException.class);

        executeMain(TestForPerformanceInsert.class);
        executeMain(TestForPerformanceMixed.class);
        executeMain(TestForPerformanceSelect.class);

    }
}
