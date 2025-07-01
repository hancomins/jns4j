package com.hancomins.jsn4j;

import com.hancomins.jsn4j.jackson.JacksonContainerFactory;
import com.hancomins.jsn4j.fastjson2.Fastjson2ContainerFactory;
import com.hancomins.jsn4j.orgjson.OrgJsonContainerFactory;
import com.hancomins.jsn4j.json5.Json5ContainerFactory;
import com.hancomins.jsn4j.gson.GsonContainerFactory;
import com.hancomins.jsn4j.simple.SimpleJsonContainerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 여러 JSN4J 구현체를 섞어서 사용하는 테스트
 * - SimpleJson, Jackson, Fastjson2, OrgJson, JSON5, Gson 등을 혼합
 * - 각 구현체의 컨테이너를 서로 중첩시켜서 올바르게 동작하는지 확인
 */
public class CrossImplementationTest {
    
    @Test
    public void testMixedImplementations() {
        // 1. SimpleJson으로 루트 객체 생성
        ObjectContainer root = SimpleJsonContainerFactory.getInstance().newObject();
        root.put("implementation", "simple");
        
        // 2. Jackson 객체를 추가
        ObjectContainer jacksonObj = JacksonContainerFactory.getInstance().newObject();
        jacksonObj.put("type", "jackson");
        jacksonObj.put("version", "2.15.2");
        root.put("jackson", jacksonObj);
        
        // 3. Fastjson2 배열을 추가
        ArrayContainer fastjsonArr = Fastjson2ContainerFactory.getInstance().newArray();
        fastjsonArr.put("fastjson2");
        fastjsonArr.put(2.0);
        fastjsonArr.put(40);
        root.put("fastjson", fastjsonArr);
        
        // 4. OrgJson 중첩 객체를 추가
        ObjectContainer orgJsonObj = OrgJsonContainerFactory.getInstance().newObject();
        orgJsonObj.put("library", "org.json");
        
        // OrgJson 내부에 SimpleJson 배열 추가
        ArrayContainer simpleArr = SimpleJsonContainerFactory.getInstance().newArray();
        simpleArr.put("nested");
        simpleArr.put("array");
        simpleArr.put(true);
        orgJsonObj.put("data", simpleArr);
        
        root.put("orgJson", orgJsonObj);
        
        // 5. JSON5 객체를 추가
        ObjectContainer json5Obj = Json5ContainerFactory.getInstance().newObject();
        json5Obj.put("format", "JSON5");
        json5Obj.put("features", Json5ContainerFactory.getInstance().newArray()
            .put("trailing commas")
            .put("single quotes")
            .put("comments"));
        root.put("json5", json5Obj);
        
        // 6. Gson 객체를 추가
        ObjectContainer gsonObj = GsonContainerFactory.getInstance().newObject();
        gsonObj.put("format", "Gson");
        gsonObj.put("vendor", "Google");
        gsonObj.put("features", GsonContainerFactory.getInstance().newArray()
            .put("type adapters")
            .put("custom serialization")
            .put("streaming API"));
        root.put("gson", gsonObj);
        
        // 7. 전체 구조를 문자열로 변환하여 확인
        String jsonString = root.getWriter().write();
        assertNotNull(jsonString);
        
        // JSON 문자열에 모든 구현체의 데이터가 포함되어 있는지 확인
        assertTrue(jsonString.contains("simple"));
        assertTrue(jsonString.contains("jackson"));
        assertTrue(jsonString.contains("fastjson"));
        assertTrue(jsonString.contains("org.json"));
        assertTrue(jsonString.contains("JSON5"));
        assertTrue(jsonString.contains("Gson"));
        
        // 8. 각 구현체로 역변환하여 확인
        verifyWithJackson(jsonString);
        verifyWithFastjson2(jsonString);
        verifyWithOrgJson(jsonString);
        verifyWithJson5(jsonString);
        verifyWithGson(jsonString);

        ObjectContainer objectContainer = Jsn4j.parse(jsonString).asObject();
        ContainerValues.equals(objectContainer,  JacksonContainerFactory.getInstance().getParser().parse(jsonString));
        ContainerValues.equals(objectContainer,  Fastjson2ContainerFactory.getInstance().getParser().parse(jsonString));
        ContainerValues.equals(objectContainer,  OrgJsonContainerFactory.getInstance().getParser().parse(jsonString));
        ContainerValues.equals(objectContainer,  Json5ContainerFactory.getInstance().getParser().parse(jsonString));
        ContainerValues.equals(objectContainer,  GsonContainerFactory.getInstance().getParser().parse(jsonString));

    }
    
    @Test
    public void testDeepNesting() {
        // JSON5로 시작
        ObjectContainer json5Root = Json5ContainerFactory.getInstance().newObject();
        json5Root.put("level", 1);
        
        // Jackson 객체 중첩
        ObjectContainer jacksonNested = JacksonContainerFactory.getInstance().newObject();
        jacksonNested.put("level", 2);
        
        // Fastjson2 배열 중첩
        ArrayContainer fastjsonNested = Fastjson2ContainerFactory.getInstance().newArray();
        fastjsonNested.put("level3");
        
        // OrgJson 객체를 배열에 추가
        ObjectContainer orgJsonItem = OrgJsonContainerFactory.getInstance().newObject();
        orgJsonItem.put("level", 4);
        
        // SimpleJson 배열을 가장 깊은 곳에
        ArrayContainer simpleDeep = SimpleJsonContainerFactory.getInstance().newArray();
        simpleDeep.put("deepest");
        simpleDeep.put(5);
        simpleDeep.put(true);
        
        // 구조 조립
        orgJsonItem.put("data", simpleDeep);
        fastjsonNested.put(orgJsonItem);
        jacksonNested.put("array", fastjsonNested);
        json5Root.put("nested", jacksonNested);
        
        // Gson 객체를 루트에 추가
        ObjectContainer gsonPart = GsonContainerFactory.getInstance().newObject();
        gsonPart.put("gson_level", 1);
        gsonPart.put("gson_data", "Gson in deep nesting");
        json5Root.put("gson", gsonPart);
        
        // 문자열로 변환
        String result = json5Root.getWriter().write();
        assertNotNull(result);
        
        // 구조 검증
        assertTrue(result.contains("level"));
        assertTrue(result.contains("deepest"));
        
        // 역탐색으로 데이터 확인
        ContainerValue nested = json5Root.get("nested");
        assertTrue(nested.isObject());
        assertEquals(2, nested.asObject().getInt("level"));
        
        ContainerValue array = nested.asObject().get("array");
        assertTrue(array.isArray());
        assertEquals("level3", array.asArray().getString(0));
        
        ContainerValue orgItem = array.asArray().get(1);
        assertTrue(orgItem.isObject());
        assertEquals(4, orgItem.asObject().getInt("level"));
        
        ContainerValue deepData = orgItem.asObject().get("data");
        assertTrue(deepData.isArray());
        assertEquals("deepest", deepData.asArray().getString(0));
        assertEquals(5, deepData.asArray().getInt(1));
        assertTrue(deepData.asArray().getBoolean(2));
    }
    
    @Test
    public void testCrossImplementationArrays() {
        // 다양한 구현체의 배열을 하나의 배열에 모으기
        ArrayContainer mixedArray = SimpleJsonContainerFactory.getInstance().newArray();
        
        // Jackson 객체
        ObjectContainer jacksonObj = JacksonContainerFactory.getInstance().newObject();
        jacksonObj.put("impl", "jackson");
        mixedArray.put(jacksonObj);
        
        // Fastjson2 객체
        ObjectContainer fastjsonObj = Fastjson2ContainerFactory.getInstance().newObject();
        fastjsonObj.put("impl", "fastjson2");
        mixedArray.put(fastjsonObj);
        
        // OrgJson 배열
        ArrayContainer orgJsonArr = OrgJsonContainerFactory.getInstance().newArray();
        orgJsonArr.put("org");
        orgJsonArr.put("json");
        mixedArray.put(orgJsonArr);
        
        // JSON5 객체
        ObjectContainer json5Obj = Json5ContainerFactory.getInstance().newObject();
        json5Obj.put("impl", "json5");
        mixedArray.put(json5Obj);
        
        // Gson 객체
        ObjectContainer gsonObj = GsonContainerFactory.getInstance().newObject();
        gsonObj.put("impl", "gson");
        mixedArray.put(gsonObj);
        
        // 각 요소가 올바르게 저장되었는지 확인
        assertEquals(5, mixedArray.size());
        
        // 첫 번째 요소 (Jackson)
        ContainerValue first = mixedArray.get(0);
        assertTrue(first.isObject());
        assertEquals("jackson", first.asObject().getString("impl"));
        
        // 두 번째 요소 (Fastjson2)
        ContainerValue second = mixedArray.get(1);
        assertTrue(second.isObject());
        assertEquals("fastjson2", second.asObject().getString("impl"));
        
        // 세 번째 요소 (OrgJson 배열)
        ContainerValue third = mixedArray.get(2);
        assertTrue(third.isArray());
        assertEquals("org", third.asArray().getString(0));
        assertEquals("json", third.asArray().getString(1));
        
        // 네 번째 요소 (JSON5)
        ContainerValue fourth = mixedArray.get(3);
        assertTrue(fourth.isObject());
        assertEquals("json5", fourth.asObject().getString("impl"));
        
        // 다섯 번째 요소 (Gson)
        ContainerValue fifth = mixedArray.get(4);
        assertTrue(fifth.isObject());
        assertEquals("gson", fifth.asObject().getString("impl"));
        
        // 전체를 문자열로 변환
        String jsonString = mixedArray.getWriter().write();
        assertNotNull(jsonString);
        assertTrue(jsonString.contains("jackson"));
        assertTrue(jsonString.contains("fastjson2"));
        assertTrue(jsonString.contains("org"));
        assertTrue(jsonString.contains("json5"));
        assertTrue(jsonString.contains("gson"));



    }
    
    private void verifyWithJackson(String json) {
        ContainerParser parser = JacksonContainerFactory.getInstance().getParser();
        ContainerValue parsed = parser.parse(json);
        assertTrue(parsed.isObject());
        assertEquals("simple", parsed.asObject().getString("implementation"));

    }
    
    private void verifyWithFastjson2(String json) {
        ContainerParser parser = Fastjson2ContainerFactory.getInstance().getParser();
        ContainerValue parsed = parser.parse(json);
        assertTrue(parsed.isObject());
        assertTrue(parsed.asObject().has("fastjson"));
    }
    
    private void verifyWithOrgJson(String json) {
        ContainerParser parser = OrgJsonContainerFactory.getInstance().getParser();
        ContainerValue parsed = parser.parse(json);
        assertTrue(parsed.isObject());
        assertTrue(parsed.asObject().containsKey("orgJson"));
    }
    
    private void verifyWithJson5(String json) {
        ContainerParser parser = Json5ContainerFactory.getInstance().getParser();
        ContainerValue parsed = parser.parse(json);
        assertTrue(parsed.isObject());
        assertTrue(parsed.asObject().containsKey("json5"));
    }
    
    private void verifyWithGson(String json) {
        ContainerParser parser = GsonContainerFactory.getInstance().getParser();
        ContainerValue parsed = parser.parse(json);
        assertTrue(parsed.isObject());
        assertTrue(parsed.asObject().containsKey("gson"));
    }
}