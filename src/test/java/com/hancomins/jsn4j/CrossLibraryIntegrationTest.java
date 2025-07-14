package com.hancomins.jsn4j;

import com.hancomins.jsn4j.jackson.JacksonContainerFactory;
import com.hancomins.jsn4j.fastjson2.Fastjson2ContainerFactory;
import com.hancomins.jsn4j.orgjson.OrgJsonContainerFactory;
import com.hancomins.jsn4j.json5.Json5ContainerFactory;
import com.hancomins.jsn4j.gson.GsonContainerFactory;
import com.hancomins.jsn4j.simple.SimpleJsonContainerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 여러 JSON 라이브러리 간의 통합 테스트
 * 복잡한 시나리오에서 라이브러리들이 함께 잘 동작하는지 검증
 */
public class CrossLibraryIntegrationTest {

    @BeforeAll
    public static void setupFactories() {
        // 모든 팩토리 등록
        Jsn4j.registerContainerFactory(SimpleJsonContainerFactory.getInstance());
        Jsn4j.registerContainerFactory(JacksonContainerFactory.getInstance());
        Jsn4j.registerContainerFactory(GsonContainerFactory.getInstance());
        Jsn4j.registerContainerFactory(Fastjson2ContainerFactory.getInstance());
        Jsn4j.registerContainerFactory(OrgJsonContainerFactory.getInstance());
        Jsn4j.registerContainerFactory(Json5ContainerFactory.getInstance());
    }

    @Test
    public void testCompleteRoundTripConversion() {
        // Given: 복잡한 데이터 구조
        ObjectContainer originalData = createComplexSystemData();
        String originalJson = originalData.getWriter().write();

        // When: 모든 라이브러리를 거쳐 순환 변환
        ObjectContainer simpleData = originalData.convertTo(JsonLibrary.SIMPLE);
        ObjectContainer jacksonData = simpleData.convertTo(JsonLibrary.JACKSON);
        ObjectContainer gsonData = jacksonData.convertTo(JsonLibrary.GSON);
        ObjectContainer fastjsonData = gsonData.convertTo(JsonLibrary.FASTJSON2);
        ObjectContainer orgJsonData = fastjsonData.convertTo(JsonLibrary.ORG_JSON);
        ObjectContainer json5Data = orgJsonData.convertTo(JsonLibrary.JSON5);
        ObjectContainer finalData = json5Data.convertTo(JsonLibrary.SIMPLE);

        // Then: 최종 데이터가 원본과 동일한지 검증
        String finalJson = finalData.getWriter().write();
        
        // 모든 필드가 유지되었는지 확인
        verifyComplexSystemData(finalData);
        
        // ContainerValues.equals로 구조적 동일성 확인
        assertTrue(ContainerValues.equals(originalData, finalData));
    }

    @Test
    public void testMixedLibraryOperations() {
        // Given: 각 라이브러리로 생성된 컨테이너들
        ObjectContainer simpleUser = SimpleJsonContainerFactory.getInstance().newObject()
                .put("id", 1)
                .put("name", "Simple User")
                .put("library", "simple");

        ObjectContainer jacksonSettings = JacksonContainerFactory.getInstance().newObject()
                .put("theme", "dark")
                .put("language", "ko")
                .put("notifications", true);

        ArrayContainer gsonTags = GsonContainerFactory.getInstance().newArray()
                .put("java")
                .put("json")
                .put("library");

        ObjectContainer fastjsonStats = Fastjson2ContainerFactory.getInstance().newObject()
                .put("posts", 150)
                .put("followers", 1200)
                .put("following", 300);

        // When: 다른 라이브러리의 컨테이너들을 조합
        ObjectContainer combinedProfile = Jsn4j.newObject()
                .put("user", simpleUser)
                .put("settings", jacksonSettings)
                .put("tags", gsonTags)
                .put("stats", fastjsonStats);

        // Then: 모든 데이터가 올바르게 통합되었는지 확인
        assertEquals("Simple User", combinedProfile.get("user").asObject().getString("name"));
        assertTrue(combinedProfile.get("settings").asObject().getBoolean("notifications"));
        assertEquals(3, combinedProfile.get("tags").asArray().size());
        assertEquals(1200, combinedProfile.get("stats").asObject().getInt("followers"));

        // 전체를 JSON으로 변환 가능
        String json = combinedProfile.getWriter().write();
        assertNotNull(json);
        assertTrue(json.contains("Simple User"));
        assertTrue(json.contains("dark"));
        assertTrue(json.contains("java"));
        assertTrue(json.contains("1200"));
    }

    @Test
    public void testComplexMergeAcrossLibraries() {
        // Given: 서로 다른 라이브러리로 생성된 주문 데이터
        ObjectContainer baseOrder = SimpleJsonContainerFactory.getInstance().newObject()
                .put("orderId", "ORD-001")
                .put("status", "pending")
                .put("createdAt", new Date().toString());

        ObjectContainer jacksonCustomer = JacksonContainerFactory.getInstance().newObject()
                .put("customerId", 12345)
                .put("name", "John Doe")
                .put("email", "john@example.com");

        ArrayContainer gsonItems = GsonContainerFactory.getInstance().newArray()
                .put(GsonContainerFactory.getInstance().newObject()
                        .put("productId", "PROD-A")
                        .put("quantity", 2)
                        .put("price", 50000))
                .put(GsonContainerFactory.getInstance().newObject()
                        .put("productId", "PROD-B")
                        .put("quantity", 1)
                        .put("price", 30000));

        // When: 병합
        baseOrder.put("customer", jacksonCustomer);
        baseOrder.put("items", gsonItems);
        
        // 다른 라이브러리로 추가 정보 병합
        ObjectContainer fastjsonPayment = Fastjson2ContainerFactory.getInstance().newObject()
                .put("method", "creditCard")
                .put("amount", 130000)
                .put("currency", "KRW");
        
        baseOrder.merge(Jsn4j.newObject().put("payment", fastjsonPayment));

        // Then: 모든 데이터가 잘 통합되었는지 확인
        assertEquals("ORD-001", baseOrder.getString("orderId"));
        assertEquals("John Doe", baseOrder.get("customer").asObject().getString("name"));
        assertEquals(2, baseOrder.get("items").asArray().size());
        assertEquals("creditCard", baseOrder.get("payment").asObject().getString("method"));
    }

    @Test
    public void testConcatAcrossLibraries() {
        // Given: 두 개의 서로 다른 라이브러리로 생성된 설정
        ObjectContainer simpleConfig = SimpleJsonContainerFactory.getInstance().newObject()
                .put("app", Jsn4j.newObject()
                        .put("name", "MyApp")
                        .put("version", "1.0.0"))
                .put("server", Jsn4j.newObject()
                        .put("host", "localhost")
                        .put("port", 8080));

        ObjectContainer jacksonConfig = JacksonContainerFactory.getInstance().newObject()
                .put("database", JacksonContainerFactory.getInstance().newObject()
                        .put("type", "mysql")
                        .put("host", "db.example.com")
                        .put("port", 3306))
                .put("cache", JacksonContainerFactory.getInstance().newObject()
                        .put("enabled", true)
                        .put("ttl", 3600));

        // When: concat 연산
        ObjectContainer mergedConfig = simpleConfig.concat(jacksonConfig);

        // Then: 두 설정이 합쳐졌는지 확인
        assertEquals("MyApp", mergedConfig.get("app").asObject().getString("name"));
        assertEquals(8080, mergedConfig.get("server").asObject().getInt("port"));
        assertEquals("mysql", mergedConfig.get("database").asObject().getString("type"));
        assertTrue(mergedConfig.get("cache").asObject().getBoolean("enabled"));
    }

    @Test
    public void testLargeScaleDataProcessing() {
        // Given: 대규모 데이터 시뮬레이션 - 회사 조직도
        ObjectContainer company = createLargeCompanyStructure();

        // When: 각 라이브러리로 변환하며 성능 측정
        Map<String, Long> conversionTimes = new HashMap<>();
        
        for (JsonLibrary library : JsonLibrary.values()) {
            long start = System.currentTimeMillis();
            ObjectContainer converted = company.convertTo(library);
            long end = System.currentTimeMillis();
            
            conversionTimes.put(library.name(), end - start);
            
            // 데이터 무결성 확인
            assertEquals("TechCorp", converted.getString("companyName"));
            assertEquals(5, converted.get("departments").asArray().size());
        }

        // Then: 모든 변환이 합리적인 시간 내에 완료
        for (Map.Entry<String, Long> entry : conversionTimes.entrySet()) {
            assertTrue(entry.getValue() < 5000, 
                      entry.getKey() + " conversion took too long: " + entry.getValue() + "ms");
        }
    }

    @Test
    public void testRealWorldScenario_EcommercePlatform() {
        // Given: 전자상거래 플랫폼의 실제 시나리오
        
        // 1. 사용자가 장바구니에 상품 추가 (Simple 라이브러리)
        ObjectContainer cart = SimpleJsonContainerFactory.getInstance().newObject()
                .put("userId", "user-123")
                .put("sessionId", UUID.randomUUID().toString())
                .put("items", SimpleJsonContainerFactory.getInstance().newArray());

        // 2. 상품 정보는 Jackson으로 관리
        ObjectContainer product1 = JacksonContainerFactory.getInstance().newObject()
                .put("productId", "PROD-001")
                .put("name", "Laptop")
                .put("price", 1500000)
                .put("stock", 10);

        // 3. 장바구니에 추가
        cart.get("items").asArray().put(
            Jsn4j.newObject()
                .put("product", product1)
                .put("quantity", 1)
                .put("addedAt", new Date().toString())
        );

        // 4. 주문 생성 (Gson으로 변환)
        ObjectContainer order = cart.convertTo(JsonLibrary.GSON);
        order.put("orderId", "ORD-" + System.currentTimeMillis());
        order.put("status", "pending");

        // 5. 결제 정보 추가 (Fastjson2)
        ObjectContainer payment = Fastjson2ContainerFactory.getInstance().newObject()
                .put("method", "creditCard")
                .put("cardNumber", "**** **** **** 1234")
                .put("amount", calculateTotal(order));

        order.put("payment", payment);

        // 6. 최종적으로 JSON5로 저장 (주석 포함 가능)
        ObjectContainer finalOrder = order.convertTo(JsonLibrary.JSON5);

        // Then: 전체 프로세스가 정상 동작
        assertNotNull(finalOrder);
        assertEquals("user-123", finalOrder.getString("userId"));
        assertEquals(1, finalOrder.get("items").asArray().size());
        assertEquals("creditCard", finalOrder.get("payment").asObject().getString("method"));
        assertTrue(finalOrder.getString("orderId").startsWith("ORD-"));
    }

    @Test
    public void testDataMigrationScenario() {
        // Given: 레거시 시스템 데이터 (org.json)
        ObjectContainer legacyData = OrgJsonContainerFactory.getInstance().newObject()
                .put("user_id", 12345)  // 언더스코어 명명
                .put("user_name", "John Doe")
                .put("user_email", "john@legacy.com")
                .put("created_date", "2020-01-15")
                .put("is_active", "true");  // 문자열로 저장된 boolean

        // When: 최신 시스템으로 마이그레이션
        
        // 1. 먼저 Simple로 변환하여 정리
        ObjectContainer cleaned = legacyData.convertTo(JsonLibrary.SIMPLE);
        
        // 2. 필드명 변경 및 타입 정리
        ObjectContainer modernData = Jsn4j.newObject()
                .put("userId", cleaned.getInt("user_id"))
                .put("userName", cleaned.getString("user_name"))
                .put("email", cleaned.getString("user_email"))
                .put("createdAt", cleaned.getString("created_date") + "T00:00:00Z")
                .put("active", "true".equals(cleaned.getString("is_active")))
                .put("migrationInfo", Jsn4j.newObject()
                        .put("migratedAt", new Date().toString())
                        .put("fromSystem", "legacy")
                        .put("toSystem", "modern"));

        // 3. Jackson으로 변환하여 고급 기능 활용
        ObjectContainer finalData = modernData.convertTo(JsonLibrary.JACKSON);

        // Then: 마이그레이션 검증
        assertEquals(12345, finalData.getInt("userId"));
        assertEquals("John Doe", finalData.getString("userName"));
        assertTrue(finalData.getBoolean("active"));
        assertTrue(finalData.getString("createdAt").endsWith("T00:00:00Z"));
        assertEquals("modern", finalData.get("migrationInfo").asObject().getString("toSystem"));
    }

    @Test
    public void testMultiTenantDataIsolation() {
        // Given: 멀티테넌트 시스템에서 각 테넌트가 다른 라이브러리 사용
        Map<String, ObjectContainer> tenantData = new HashMap<>();
        
        // Tenant A - Simple JSON
        tenantData.put("tenantA", SimpleJsonContainerFactory.getInstance().newObject()
                .put("tenantId", "A")
                .put("config", Jsn4j.newObject()
                        .put("theme", "blue")
                        .put("features", Jsn4j.newArray().put("basic").put("reports"))));

        // Tenant B - Jackson
        tenantData.put("tenantB", JacksonContainerFactory.getInstance().newObject()
                .put("tenantId", "B")
                .put("config", JacksonContainerFactory.getInstance().newObject()
                        .put("theme", "green")
                        .put("features", JacksonContainerFactory.getInstance().newArray()
                                .put("advanced").put("api").put("analytics"))));

        // Tenant C - Gson
        tenantData.put("tenantC", GsonContainerFactory.getInstance().newObject()
                .put("tenantId", "C")
                .put("config", GsonContainerFactory.getInstance().newObject()
                        .put("theme", "custom")
                        .put("customization", true)));

        // When: 통합 대시보드 생성
        ObjectContainer dashboard = Jsn4j.newObject();
        for (Map.Entry<String, ObjectContainer> entry : tenantData.entrySet()) {
            dashboard.put(entry.getKey(), entry.getValue());
        }

        // Then: 각 테넌트 데이터가 격리되어 있는지 확인
        assertEquals("A", dashboard.get("tenantA").asObject().getString("tenantId"));
        assertEquals("green", dashboard.get("tenantB").asObject()
                .get("config").asObject().getString("theme"));
        assertTrue(dashboard.get("tenantC").asObject()
                .get("config").asObject().getBoolean("customization"));

        // 전체를 하나의 라이브러리로 통합 가능
        ObjectContainer unified = dashboard.convertTo(JsonLibrary.JACKSON);
        String unifiedJson = unified.getWriter().write();
        assertNotNull(unifiedJson);
    }

    // 헬퍼 메서드들

    private ObjectContainer createComplexSystemData() {
        return Jsn4j.newObject()
                .put("systemId", UUID.randomUUID().toString())
                .put("metadata", Jsn4j.newObject()
                        .put("version", "2.0.0")
                        .put("timestamp", new Date().toString())
                        .put("environment", "production"))
                .put("components", Jsn4j.newArray()
                        .put(createComponent("auth-service", "running", 99.9))
                        .put(createComponent("api-gateway", "running", 98.5))
                        .put(createComponent("database", "running", 99.99)))
                .put("metrics", Jsn4j.newObject()
                        .put("cpu", Jsn4j.newObject()
                                .put("usage", 45.2)
                                .put("cores", 8))
                        .put("memory", Jsn4j.newObject()
                                .put("used", 12884901888L)
                                .put("total", 17179869184L))
                        .put("requests", Jsn4j.newObject()
                                .put("total", 1000000)
                                .put("success", 995000)
                                .put("errors", 5000)))
                .put("configuration", Jsn4j.newObject()
                        .put("features", Jsn4j.newArray()
                                .put("caching")
                                .put("loadBalancing")
                                .put("autoScaling"))
                        .put("limits", Jsn4j.newObject()
                                .put("maxConnections", 10000)
                                .put("timeout", 30000)
                                .put("retries", 3)));
    }

    private void verifyComplexSystemData(ObjectContainer data) {
        assertNotNull(data.getString("systemId"));
        assertEquals("2.0.0", data.get("metadata").asObject().getString("version"));
        assertEquals(3, data.get("components").asArray().size());
        assertEquals(45.2, data.get("metrics").asObject()
                .get("cpu").asObject().getDouble("usage"), 0.1);
        assertEquals(3, data.get("configuration").asObject()
                .get("features").asArray().size());
    }

    private ObjectContainer createComponent(String name, String status, double uptime) {
        return Jsn4j.newObject()
                .put("name", name)
                .put("status", status)
                .put("uptime", uptime)
                .put("lastCheck", new Date().toString());
    }

    private ObjectContainer createLargeCompanyStructure() {
        ObjectContainer company = Jsn4j.newObject()
                .put("companyName", "TechCorp")
                .put("founded", 2010)
                .put("employees", 5000);

        ArrayContainer departments = Jsn4j.newArray();
        String[] deptNames = {"Engineering", "Sales", "Marketing", "HR", "Finance"};
        
        for (String deptName : deptNames) {
            ObjectContainer dept = Jsn4j.newObject()
                    .put("name", deptName)
                    .put("headCount", 200 + (int)(Math.random() * 800));
            
            ArrayContainer employees = Jsn4j.newArray();
            for (int i = 0; i < 50; i++) {
                employees.put(Jsn4j.newObject()
                        .put("id", deptName + "-" + i)
                        .put("name", "Employee " + i)
                        .put("position", getRandomPosition())
                        .put("salary", 50000 + (int)(Math.random() * 100000)));
            }
            dept.put("employees", employees);
            departments.put(dept);
        }
        
        company.put("departments", departments);
        return company;
    }

    private String getRandomPosition() {
        String[] positions = {"Junior", "Senior", "Lead", "Manager", "Director"};
        return positions[(int)(Math.random() * positions.length)];
    }

    private int calculateTotal(ObjectContainer order) {
        ArrayContainer items = order.get("items").asArray();
        int total = 0;
        for (int i = 0; i < items.size(); i++) {
            ObjectContainer item = items.get(i).asObject();
            ObjectContainer product = item.get("product").asObject();
            int price = product.getInt("price");
            int quantity = item.getInt("quantity");
            total += price * quantity;
        }
        return total;
    }
}