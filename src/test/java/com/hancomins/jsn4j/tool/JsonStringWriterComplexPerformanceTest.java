package com.hancomins.jsn4j.tool;

import com.hancomins.jsn4j.*;
import org.junit.jupiter.api.Test;

import java.util.*;

public class JsonStringWriterComplexPerformanceTest {
    
    private static final int USER_COUNT = 100;
    private static final int ITERATIONS = 1000;
    private static final int WARMUP_ITERATIONS = 100;
    
    @Test
    public void complexPerformanceTest() {
        System.out.println("=== JSN4J Complex Performance Test ===");
        System.out.println("Creating " + USER_COUNT + " complex user objects");
        System.out.println("Iterations: " + ITERATIONS);
        System.out.println();
        
        // Warmup
        System.out.println("Warming up...");
        warmup();
        
        // 여러 번 실행하여 평균 구하기
        int runs = 5;
        List<TestResult> avgResults = new ArrayList<>();
        
        long[] jsonWriterTimes = new long[runs];
        long[] simpleContainerTimes = new long[runs];
        long[] jsonWriterMemory = new long[runs];
        long[] simpleContainerMemory = new long[runs];
        
        for (int i = 0; i < runs; i++) {
            System.out.println("\nRun " + (i + 1) + " of " + runs);
            
            // JsonStringWriter 테스트
            System.gc();
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            long startMem = getUsedMemory();
            long startTime = System.nanoTime();
            
            for (int iter = 0; iter < ITERATIONS; iter++) {
                String json = createComplexJsonWithStringWriter();
            }
            
            long endTime = System.nanoTime();
            long endMem = getUsedMemory();
            jsonWriterTimes[i] = (endTime - startTime) / 1_000_000;
            jsonWriterMemory[i] = (endMem - startMem) / 1024 / 1024;
            
            System.out.println("JsonStringWriter: " + jsonWriterTimes[i] + "ms, Memory: " + jsonWriterMemory[i] + "MB");
            
            // Simple Container 테스트
            System.gc();
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            startMem = getUsedMemory();
            startTime = System.nanoTime();
            
            for (int iter = 0; iter < ITERATIONS; iter++) {
                String json = createComplexJsonWithContainer();
            }
            
            endTime = System.nanoTime();
            endMem = getUsedMemory();
            simpleContainerTimes[i] = (endTime - startTime) / 1_000_000;
            simpleContainerMemory[i] = (endMem - startMem) / 1024 / 1024;
            
            System.out.println("Simple Container: " + simpleContainerTimes[i] + "ms, Memory: " + simpleContainerMemory[i] + "MB");
        }
        
        // 평균 계산
        avgResults.add(new TestResult(
            "JsonStringWriter", 
            average(jsonWriterTimes), 
            average(jsonWriterMemory)
        ));
        avgResults.add(new TestResult(
            "Simple Container", 
            average(simpleContainerTimes), 
            average(simpleContainerMemory)
        ));
        
        // 결과 출력
        printResults(avgResults);
        
        // 샘플 JSON 출력
        System.out.println("\n=== Sample JSON Structure ===");
        String sampleJson = createComplexJsonWithStringWriter();
        System.out.println("Total JSON size: " + sampleJson.length() + " characters");
        System.out.println("Sample (first 500 chars):");
        System.out.println(sampleJson.substring(0, Math.min(500, sampleJson.length())) + "...");
    }
    
    private void warmup() {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            createComplexJsonWithStringWriter();
            createComplexJsonWithContainer();
        }
    }
    
    private String createComplexJsonWithStringWriter() {
        JsonArrayStringWriter users = new JsonArrayStringWriter();
        
        for (int i = 0; i < USER_COUNT; i++) {
            users.put(createComplexUserWithStringWriter(i));
        }
        
        return new JsonObjectStringWriter()
            .put("version", "2.0")
            .put("timestamp", System.currentTimeMillis())
            .put("totalUsers", USER_COUNT)
            .put("users", users)
            .put("metadata", new JsonObjectStringWriter()
                .put("generated", new Date().toString())
                .put("source", "JSN4J Performance Test")
                .put("complexity", "extreme"))
            .build();
    }
    
    private JsonObjectStringWriter createComplexUserWithStringWriter(int userId) {
        Random rand = new Random(userId); // 시드를 사용하여 일관된 데이터 생성
        
        // 주소 목록
        JsonArrayStringWriter addresses = new JsonArrayStringWriter();
        for (int i = 0; i < 3; i++) {
            addresses.put(new JsonObjectStringWriter()
                .put("type", i == 0 ? "home" : i == 1 ? "work" : "other")
                .put("street", "Street " + rand.nextInt(1000))
                .put("city", getRandomCity(rand))
                .put("state", getRandomState(rand))
                .put("country", "Korea")
                .put("postalCode", String.format("%05d", rand.nextInt(100000)))
                .put("coordinates", new JsonObjectStringWriter()
                    .put("latitude", 35 + rand.nextDouble() * 5)
                    .put("longitude", 125 + rand.nextDouble() * 5))
                .put("verified", rand.nextBoolean()));
        }
        
        // 주문 내역
        JsonArrayStringWriter orders = new JsonArrayStringWriter();
        for (int i = 0; i < 10; i++) {
            JsonArrayStringWriter items = new JsonArrayStringWriter();
            int itemCount = rand.nextInt(5) + 1;
            for (int j = 0; j < itemCount; j++) {
                items.put(new JsonObjectStringWriter()
                    .put("productId", "PROD-" + rand.nextInt(10000))
                    .put("name", "Product " + rand.nextInt(1000))
                    .put("quantity", rand.nextInt(5) + 1)
                    .put("price", rand.nextDouble() * 100000)
                    .put("discount", rand.nextDouble() * 0.3));
            }
            
            orders.put(new JsonObjectStringWriter()
                .put("orderId", "ORD-" + userId + "-" + i)
                .put("date", System.currentTimeMillis() - rand.nextInt(365) * 24 * 60 * 60 * 1000L)
                .put("status", getRandomOrderStatus(rand))
                .put("items", items)
                .put("shipping", new JsonObjectStringWriter()
                    .put("method", rand.nextBoolean() ? "express" : "standard")
                    .put("cost", rand.nextDouble() * 10000)
                    .put("estimatedDays", rand.nextInt(7) + 1))
                .put("payment", new JsonObjectStringWriter()
                    .put("method", getRandomPaymentMethod(rand))
                    .put("transactionId", UUID.randomUUID().toString())));
        }
        
        // 활동 로그
        JsonArrayStringWriter activities = new JsonArrayStringWriter();
        for (int i = 0; i < 20; i++) {
            activities.put(new JsonObjectStringWriter()
                .put("timestamp", System.currentTimeMillis() - rand.nextInt(30) * 24 * 60 * 60 * 1000L)
                .put("action", getRandomAction(rand))
                .put("ip", generateRandomIp(rand))
                .put("userAgent", "Mozilla/5.0 " + rand.nextInt(100))
                .put("metadata", new JsonObjectStringWriter()
                    .put("sessionId", UUID.randomUUID().toString())
                    .put("duration", rand.nextInt(3600))
                    .put("pageViews", rand.nextInt(50))));
        }
        
        // 태그와 관심사
        JsonArrayStringWriter tags = new JsonArrayStringWriter();
        for (int i = 0; i < 15; i++) {
            tags.put("tag" + rand.nextInt(100));
        }
        
        // 친구 목록
        JsonArrayStringWriter friends = new JsonArrayStringWriter();
        for (int i = 0; i < 30; i++) {
            friends.put(new JsonObjectStringWriter()
                .put("userId", rand.nextInt(USER_COUNT))
                .put("since", System.currentTimeMillis() - rand.nextInt(1000) * 24 * 60 * 60 * 1000L)
                .put("relationship", rand.nextBoolean() ? "friend" : "colleague"));
        }
        
        return new JsonObjectStringWriter()
            .put("userId", userId)
            .put("username", "user_" + userId)
            .put("email", "user" + userId + "@example.com")
            .put("profile", new JsonObjectStringWriter()
                .put("firstName", "First" + userId)
                .put("lastName", "Last" + userId)
                .put("age", 20 + rand.nextInt(50))
                .put("gender", rand.nextBoolean() ? "M" : "F")
                .put("bio", "This is a bio for user " + userId + " with some random text " + UUID.randomUUID())
                .put("avatar", "https://example.com/avatar/" + userId + ".jpg")
                .put("coverPhoto", "https://example.com/cover/" + userId + ".jpg"))
            .put("settings", new JsonObjectStringWriter()
                .put("theme", rand.nextBoolean() ? "dark" : "light")
                .put("language", getRandomLanguage(rand))
                .put("timezone", "Asia/Seoul")
                .put("notifications", new JsonObjectStringWriter()
                    .put("email", rand.nextBoolean())
                    .put("push", rand.nextBoolean())
                    .put("sms", rand.nextBoolean())
                    .put("frequency", getRandomFrequency(rand)))
                .put("privacy", new JsonObjectStringWriter()
                    .put("profileVisibility", getRandomVisibility(rand))
                    .put("showEmail", rand.nextBoolean())
                    .put("showPhone", rand.nextBoolean())
                    .put("allowMessages", rand.nextBoolean())))
            .put("addresses", addresses)
            .put("orders", orders)
            .put("activities", activities)
            .put("tags", tags)
            .put("friends", friends)
            .put("statistics", new JsonObjectStringWriter()
                .put("totalOrders", orders.size())
                .put("totalSpent", rand.nextDouble() * 1000000)
                .put("avgOrderValue", rand.nextDouble() * 100000)
                .put("lastLogin", System.currentTimeMillis())
                .put("accountAge", rand.nextInt(3650))
                .put("loyaltyPoints", rand.nextInt(10000))
                .put("rank", getRandomRank(rand)));
    }
    
    private String createComplexJsonWithContainer() {
        ArrayContainer users = Jsn4j.newArray();
        
        for (int i = 0; i < USER_COUNT; i++) {
            users.put(createComplexUserWithContainer(i));
        }
        
        ObjectContainer root = Jsn4j.newObject()
            .put("version", "2.0")
            .put("timestamp", System.currentTimeMillis())
            .put("totalUsers", USER_COUNT)
            .put("users", users);
            
        ObjectContainer metadata = Jsn4j.newObject()
            .put("generated", new Date().toString())
            .put("source", "JSN4J Performance Test")
            .put("complexity", "extreme");
        root.put("metadata", metadata);
        
        return root.getWriter().write();
    }
    
    private ObjectContainer createComplexUserWithContainer(int userId) {
        Random rand = new Random(userId);
        ObjectContainer user = Jsn4j.newObject();
        
        user.put("userId", userId)
            .put("username", "user_" + userId)
            .put("email", "user" + userId + "@example.com");
        
        // Profile
        ObjectContainer profile = Jsn4j.newObject()
            .put("firstName", "First" + userId)
            .put("lastName", "Last" + userId)
            .put("age", 20 + rand.nextInt(50))
            .put("gender", rand.nextBoolean() ? "M" : "F")
            .put("bio", "This is a bio for user " + userId + " with some random text " + UUID.randomUUID())
            .put("avatar", "https://example.com/avatar/" + userId + ".jpg")
            .put("coverPhoto", "https://example.com/cover/" + userId + ".jpg");
        user.put("profile", profile);
        
        // Settings
        ObjectContainer notifications = Jsn4j.newObject()
            .put("email", rand.nextBoolean())
            .put("push", rand.nextBoolean())
            .put("sms", rand.nextBoolean())
            .put("frequency", getRandomFrequency(rand));
            
        ObjectContainer privacy = Jsn4j.newObject()
            .put("profileVisibility", getRandomVisibility(rand))
            .put("showEmail", rand.nextBoolean())
            .put("showPhone", rand.nextBoolean())
            .put("allowMessages", rand.nextBoolean());
            
        ObjectContainer settings = Jsn4j.newObject()
            .put("theme", rand.nextBoolean() ? "dark" : "light")
            .put("language", getRandomLanguage(rand))
            .put("timezone", "Asia/Seoul")
            .put("notifications", notifications)
            .put("privacy", privacy);
        user.put("settings", settings);
        
        // Addresses
        ArrayContainer addresses = Jsn4j.newArray();
        for (int i = 0; i < 3; i++) {
            ObjectContainer coordinates = Jsn4j.newObject()
                .put("latitude", 35 + rand.nextDouble() * 5)
                .put("longitude", 125 + rand.nextDouble() * 5);
                
            ObjectContainer address = Jsn4j.newObject()
                .put("type", i == 0 ? "home" : i == 1 ? "work" : "other")
                .put("street", "Street " + rand.nextInt(1000))
                .put("city", getRandomCity(rand))
                .put("state", getRandomState(rand))
                .put("country", "Korea")
                .put("postalCode", String.format("%05d", rand.nextInt(100000)))
                .put("coordinates", coordinates)
                .put("verified", rand.nextBoolean());
            addresses.put(address);
        }
        user.put("addresses", addresses);
        
        // Orders
        ArrayContainer orders = Jsn4j.newArray();
        for (int i = 0; i < 10; i++) {
            ArrayContainer items = Jsn4j.newArray();
            int itemCount = rand.nextInt(5) + 1;
            for (int j = 0; j < itemCount; j++) {
                ObjectContainer item = Jsn4j.newObject()
                    .put("productId", "PROD-" + rand.nextInt(10000))
                    .put("name", "Product " + rand.nextInt(1000))
                    .put("quantity", rand.nextInt(5) + 1)
                    .put("price", rand.nextDouble() * 100000)
                    .put("discount", rand.nextDouble() * 0.3);
                items.put(item);
            }
            
            ObjectContainer shipping = Jsn4j.newObject()
                .put("method", rand.nextBoolean() ? "express" : "standard")
                .put("cost", rand.nextDouble() * 10000)
                .put("estimatedDays", rand.nextInt(7) + 1);
                
            ObjectContainer payment = Jsn4j.newObject()
                .put("method", getRandomPaymentMethod(rand))
                .put("transactionId", UUID.randomUUID().toString());
            
            ObjectContainer order = Jsn4j.newObject()
                .put("orderId", "ORD-" + userId + "-" + i)
                .put("date", System.currentTimeMillis() - rand.nextInt(365) * 24 * 60 * 60 * 1000L)
                .put("status", getRandomOrderStatus(rand))
                .put("items", items)
                .put("shipping", shipping)
                .put("payment", payment);
            orders.put(order);
        }
        user.put("orders", orders);
        
        // Activities
        ArrayContainer activities = Jsn4j.newArray();
        for (int i = 0; i < 20; i++) {
            ObjectContainer metadata = Jsn4j.newObject()
                .put("sessionId", UUID.randomUUID().toString())
                .put("duration", rand.nextInt(3600))
                .put("pageViews", rand.nextInt(50));
                
            ObjectContainer activity = Jsn4j.newObject()
                .put("timestamp", System.currentTimeMillis() - rand.nextInt(30) * 24 * 60 * 60 * 1000L)
                .put("action", getRandomAction(rand))
                .put("ip", generateRandomIp(rand))
                .put("userAgent", "Mozilla/5.0 " + rand.nextInt(100))
                .put("metadata", metadata);
            activities.put(activity);
        }
        user.put("activities", activities);
        
        // Tags
        ArrayContainer tags = Jsn4j.newArray();
        for (int i = 0; i < 15; i++) {
            tags.put("tag" + rand.nextInt(100));
        }
        user.put("tags", tags);
        
        // Friends
        ArrayContainer friends = Jsn4j.newArray();
        for (int i = 0; i < 30; i++) {
            ObjectContainer friend = Jsn4j.newObject()
                .put("userId", rand.nextInt(USER_COUNT))
                .put("since", System.currentTimeMillis() - rand.nextInt(1000) * 24 * 60 * 60 * 1000L)
                .put("relationship", rand.nextBoolean() ? "friend" : "colleague");
            friends.put(friend);
        }
        user.put("friends", friends);
        
        // Statistics
        ObjectContainer statistics = Jsn4j.newObject()
            .put("totalOrders", orders.size())
            .put("totalSpent", rand.nextDouble() * 1000000)
            .put("avgOrderValue", rand.nextDouble() * 100000)
            .put("lastLogin", System.currentTimeMillis())
            .put("accountAge", rand.nextInt(3650))
            .put("loyaltyPoints", rand.nextInt(10000))
            .put("rank", getRandomRank(rand));
        user.put("statistics", statistics);
        
        return user;
    }
    
    // Helper methods
    private String getRandomCity(Random rand) {
        String[] cities = {"Seoul", "Busan", "Incheon", "Daegu", "Daejeon", "Gwangju", "Ulsan", "Suwon"};
        return cities[rand.nextInt(cities.length)];
    }
    
    private String getRandomState(Random rand) {
        String[] states = {"Gyeonggi", "Gangwon", "Chungbuk", "Chungnam", "Jeonbuk", "Jeonnam", "Gyeongbuk", "Gyeongnam"};
        return states[rand.nextInt(states.length)];
    }
    
    private String getRandomOrderStatus(Random rand) {
        String[] statuses = {"pending", "processing", "shipped", "delivered", "cancelled", "refunded"};
        return statuses[rand.nextInt(statuses.length)];
    }
    
    private String getRandomPaymentMethod(Random rand) {
        String[] methods = {"credit_card", "debit_card", "paypal", "bank_transfer", "cryptocurrency"};
        return methods[rand.nextInt(methods.length)];
    }
    
    private String getRandomAction(Random rand) {
        String[] actions = {"login", "logout", "view_product", "add_to_cart", "purchase", "update_profile", "change_password"};
        return actions[rand.nextInt(actions.length)];
    }
    
    private String generateRandomIp(Random rand) {
        return rand.nextInt(256) + "." + rand.nextInt(256) + "." + rand.nextInt(256) + "." + rand.nextInt(256);
    }
    
    private String getRandomLanguage(Random rand) {
        String[] languages = {"ko", "en", "ja", "zh", "es", "fr", "de"};
        return languages[rand.nextInt(languages.length)];
    }
    
    private String getRandomFrequency(Random rand) {
        String[] frequencies = {"immediate", "daily", "weekly", "monthly"};
        return frequencies[rand.nextInt(frequencies.length)];
    }
    
    private String getRandomVisibility(Random rand) {
        String[] visibilities = {"public", "friends", "private"};
        return visibilities[rand.nextInt(visibilities.length)];
    }
    
    private String getRandomRank(Random rand) {
        String[] ranks = {"bronze", "silver", "gold", "platinum", "diamond"};
        return ranks[rand.nextInt(ranks.length)];
    }
    
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    private long average(long[] values) {
        long sum = 0;
        for (long value : values) {
            sum += value;
        }
        return sum / values.length;
    }
    
    private void printResults(List<TestResult> results) {
        System.out.println("\n=== Complex Performance Test Results ===");
        System.out.println("(Average of 5 runs, " + ITERATIONS + " iterations each)");
        System.out.println();
        
        System.out.println("| 구현 방식 | 실행 시간 (ms) | 메모리 사용량 (MB) | 상대 성능 |");
        System.out.println("|-----------|-----------------|---------------------|-----------|");
        
        long baseTime = results.get(0).timeMs;
        
        for (TestResult result : results) {
            double relativePerf = (double) baseTime / result.timeMs;
            System.out.printf("| %s | %d | ~%d | %.2fx |%n",
                result.name, result.timeMs, result.memoryMB, relativePerf);
        }
        
        // Performance improvement
        TestResult jsonWriter = results.get(0);
        TestResult simpleContainer = results.get(1);
        
        double improvement = ((double)(simpleContainer.timeMs - jsonWriter.timeMs) / simpleContainer.timeMs) * 100;
        System.out.printf("\nJsonStringWriter는 Simple Container 대비 %.2f%% 더 빠릅니다.%n", improvement);
        
        double memoryReduction = ((double)(simpleContainer.memoryMB - jsonWriter.memoryMB) / simpleContainer.memoryMB) * 100;
        if (memoryReduction > 0) {
            System.out.printf("메모리 사용량은 %.2f%% 감소했습니다.%n", memoryReduction);
        }
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