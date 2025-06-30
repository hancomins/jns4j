package com.hancomins.jsn4j.tool;

import com.hancomins.jsn4j.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class JsonStringWriterPerformanceTest {
    
    private static final int ITERATIONS = 100000;
    private static final int WARMUP_ITERATIONS = 10000;
    
    @Test
    public void performanceComparison() {
        System.out.println("=== JSN4J Performance Test ===");
        System.out.println("Iterations: " + ITERATIONS);
        System.out.println();
        
        // Warmup
        System.out.println("Warming up...");
        warmup();
        
        // 여러 번 실행하여 평균 구하기
        int runs = 5;
        List<TestResult> avgResults = new ArrayList<>();
        
        // 각 테스트를 5번씩 실행
        long[] jsonWriterTimes = new long[runs];
        long[] simpleContainerTimes = new long[runs];
        long[] noCacheTimes = new long[runs];
        
        for (int i = 0; i < runs; i++) {
            System.out.println("Run " + (i + 1) + " of " + runs);
            
            TestResult jw = testJsonStringWriter();
            jsonWriterTimes[i] = jw.timeMs;
            
            TestResult sc = testSimpleContainer();
            simpleContainerTimes[i] = sc.timeMs;
            
            TestResult nc = testSimpleContainerNoCache();
            noCacheTimes[i] = nc.timeMs;
            
            // GC 실행
            System.gc();
            try { Thread.sleep(100); } catch (InterruptedException e) {}
        }
        
        // 평균 계산
        avgResults.add(new TestResult("JsonStringWriter", average(jsonWriterTimes), 0));
        avgResults.add(new TestResult("Simple Container", average(simpleContainerTimes), 0));
        avgResults.add(new TestResult("JsonStringWriter (No Cache)", average(noCacheTimes), 0));
        
        // 결과 출력
        printResults(avgResults);
        
        // Markdown 표 생성
        printMarkdownTable(avgResults);
    }
    
    private long average(long[] times) {
        long sum = 0;
        for (long time : times) {
            sum += time;
        }
        return sum / times.length;
    }
    
    private void warmup() {
        // JsonStringWriter warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            String json = new JsonObjectStringWriter()
                .put("id", i)
                .put("name", "User " + i)
                .put("active", i % 2 == 0)
                .build();
        }
        
        // Container warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            ObjectContainer obj = Jsn4j.newObject()
                .put("id", i)
                .put("name", "User " + i)
                .put("active", i % 2 == 0);
            String json = obj.getWriter().write();
        }
    }
    
    private TestResult testJsonStringWriter() {
        System.gc();
        long startMemory = getUsedMemory();
        long startTime = System.nanoTime();
        
        for (int i = 0; i < ITERATIONS; i++) {
            String json = new JsonObjectStringWriter()
                .put("id", i)
                .put("name", "User " + i)
                .put("active", i % 2 == 0)
                .put("score", i * 1.5)
                .put("tags", new JsonArrayStringWriter()
                    .put("tag1")
                    .put("tag2")
                    .put("tag3"))
                .put("profile", new JsonObjectStringWriter()
                    .put("age", 25 + (i % 40))
                    .put("city", "Seoul")
                    .put("country", "Korea"))
                .build();
        }
        
        long endTime = System.nanoTime();
        long endMemory = getUsedMemory();
        
        return new TestResult(
            "JsonStringWriter",
            (endTime - startTime) / 1_000_000,
            (endMemory - startMemory) / 1024 / 1024
        );
    }
    
    private TestResult testSimpleContainer() {
        // 기본 팩토리로 설정
        Jsn4j.setDefaultContainerFactory(Jsn4j.getContainerFactoryByName("simple"));
        
        System.gc();
        long startMemory = getUsedMemory();
        long startTime = System.nanoTime();
        
        for (int i = 0; i < ITERATIONS; i++) {
            ObjectContainer obj = Jsn4j.newObject()
                .put("id", i)
                .put("name", "User " + i)
                .put("active", i % 2 == 0)
                .put("score", i * 1.5);
            
            ArrayContainer tags = Jsn4j.newArray()
                .put("tag1")
                .put("tag2")
                .put("tag3");
            obj.put("tags", tags);
            
            ObjectContainer profile = Jsn4j.newObject()
                .put("age", 25 + (i % 40))
                .put("city", "Seoul")
                .put("country", "Korea");
            obj.put("profile", profile);
            
            String json = obj.getWriter().write();
        }
        
        long endTime = System.nanoTime();
        long endMemory = getUsedMemory();
        
        return new TestResult(
            "Simple Container",
            (endTime - startTime) / 1_000_000,
            (endMemory - startMemory) / 1024 / 1024
        );
    }
    
    private TestResult testSimpleContainerNoCache() {
        // 캐시 비활성화
        StringBuilderCache.setCacheEnabled(false);
        
        System.gc();
        long startMemory = getUsedMemory();
        long startTime = System.nanoTime();
        
        for (int i = 0; i < ITERATIONS; i++) {
            String json = new JsonObjectStringWriter()
                .put("id", i)
                .put("name", "User " + i)
                .put("active", i % 2 == 0)
                .put("score", i * 1.5)
                .put("tags", new JsonArrayStringWriter()
                    .put("tag1")
                    .put("tag2")
                    .put("tag3"))
                .put("profile", new JsonObjectStringWriter()
                    .put("age", 25 + (i % 40))
                    .put("city", "Seoul")
                    .put("country", "Korea"))
                .build();
        }
        
        long endTime = System.nanoTime();
        long endMemory = getUsedMemory();
        
        // 캐시 다시 활성화
        StringBuilderCache.setCacheEnabled(true);
        
        return new TestResult(
            "JsonStringWriter (No Cache)",
            (endTime - startTime) / 1_000_000,
            (endMemory - startMemory) / 1024 / 1024
        );
    }
    
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    private void printResults(List<TestResult> results) {
        System.out.println("\n=== Test Results ===");
        for (TestResult result : results) {
            System.out.printf("%s: %dms (Memory: ~%dMB)%n",
                result.name, result.timeMs, result.memoryMB);
        }
        
        // Calculate performance improvement
        TestResult jsonWriter = results.get(0);
        TestResult simpleContainer = results.get(1);
        TestResult noCacheWriter = results.get(2);
        
        double simpleImprovement = ((double)(simpleContainer.timeMs - jsonWriter.timeMs) / simpleContainer.timeMs) * 100;
        double cacheImprovement = ((double)(noCacheWriter.timeMs - jsonWriter.timeMs) / noCacheWriter.timeMs) * 100;
        
        System.out.println("\n=== Performance Improvement ===");
        System.out.printf("JsonStringWriter vs Simple Container: %.2f%% faster%n", simpleImprovement);
        System.out.printf("JsonStringWriter (with cache) vs JsonStringWriter (no cache): %.2f%% faster%n", cacheImprovement);
    }
    
    private void printMarkdownTable(List<TestResult> results) {
        System.out.println("\n=== Markdown Table for README.md ===");
        System.out.println();
        System.out.println("| 구현 방식 | 실행 시간 (ms) | 메모리 사용량 (MB) | 상대 성능 |");
        System.out.println("|-----------|-----------------|---------------------|-----------|");
        
        long baseTime = results.get(0).timeMs;
        
        for (TestResult result : results) {
            double relativePerf = (double) baseTime / result.timeMs;
            System.out.printf("| %s | %d | ~%d | %.2fx |%n",
                result.name, result.timeMs, result.memoryMB, relativePerf);
        }
        
        System.out.println();
        System.out.println("*테스트 환경: " + ITERATIONS + "개 JSON 객체 생성*");
    }
    
    private static class TestResult {
        final String name;
        final long timeMs;
        final long memoryMB;
        
        TestResult(String name, long timeMs, long memoryMB) {
            this.name = name;
            this.timeMs = timeMs;
            this.memoryMB = memoryMB;
        }
    }
}