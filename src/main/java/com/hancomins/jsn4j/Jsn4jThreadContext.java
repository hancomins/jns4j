package com.hancomins.jsn4j;




import java.io.InputStream;
import java.io.Reader;

/**
 * Jsn4jThreadContext 는 Jsn4j의 ThreadLocal을 사용하여 JSON 파싱 및 생성에 필요한 컨테이너 팩토리를 관리한다.
 * 동일한 스레드 내에서 JSON 파싱 및 생성을 수행할 때, Jsn4jThreadContext를 사용하여 컨테이너 팩토리를 설정하고 사용할 수 있다.
 */
@SuppressWarnings("UnusedReturnValue")
public class Jsn4jThreadContext {

    private static final ThreadLocal<ContainerFactory> threadLocal = new ThreadLocal<>();
    private static final ThreadLocal<ContainerValue> defaultRootValue = new ThreadLocal<>();



    private Jsn4jThreadContext() {
        throw new AssertionError("Cannot instantiate Jsn4j");
    }


    public static ContainerFactory getContainerFactory() {
        ContainerFactory factory = threadLocal.get();
        if (factory == null) {
            return Jsn4j.getDefaultContainerFactory();
        }
        return factory;
    }

    public static void setContainerFactory(ContainerFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("Container factory cannot be null");
        }
        threadLocal.set(factory);
    }

    public static void setContainerFactory(String name) {
        ContainerFactory containerFactory =  Jsn4j.getContainerFactoryByName(name);
        threadLocal.set(containerFactory);
    }


    public static void setContainerFactoryByClassName(String className) {
        ContainerFactory containerFactory = Jsn4j.getContainerFactoryByClassName(className);
        threadLocal.set(containerFactory);
    }

    public static void setDefaultRootValue(ContainerValue rootValue) {
        defaultRootValue.set(rootValue);
    }


    public static void clearContainerFactory() {
        threadLocal.remove();
        defaultRootValue.remove();
    }


    public static ContainerValue parse(String json) {
        return getContainerFactory().getParser().parse(json);
    }

    public static ContainerValue parse(InputStream inputStream) {
        return getContainerFactory().getParser().parse(inputStream);
    }

    public static ContainerValue parse(Reader reader) {
        return getContainerFactory().getParser().parse(reader);
    }




    public static ObjectContainer newObject() {
        if(defaultRootValue.get() != null) {
            return getContainerFactory().newObject(defaultRootValue.get());
        }
        return getContainerFactory().newObject();
    }
    public static ArrayContainer newArray() {
        if(defaultRootValue.get() != null) {
            return getContainerFactory().newArray(defaultRootValue.get());
        }
        return getContainerFactory().newArray();
    }

    public static ObjectContainer newObject(ObjectContainer rootContainer) {
        if (rootContainer == null) {
            return getContainerFactory().newObject();
        }
        return getContainerFactory().newObject(rootContainer);
    }
    public static ObjectContainer newObject(ArrayContainer rootContainer) {
        if (rootContainer == null) {
            return getContainerFactory().newObject();
        }
        return getContainerFactory().newObject(rootContainer);
    }

    public static ArrayContainer newArray(ObjectContainer rootContainer) {
        if (rootContainer == null) {
            return getContainerFactory().newArray();
        }
        return getContainerFactory().newArray(rootContainer);
    }

    public static ArrayContainer newArray(ArrayContainer rootContainer) {
        if (rootContainer == null) {
            return getContainerFactory().newArray();
        }
        return getContainerFactory().newArray(rootContainer);
    }

    public static ContainerParser getParser() {
        return getContainerFactory().getParser();
    }

}
