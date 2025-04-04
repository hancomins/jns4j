package com.hancomins.jsn4j;




import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("UnusedReturnValue")
public class Jsn4j {

    private static final String DEFAULT_CONTAINER_FACTORY = "com.hancomins.jsn4j.simple.SimpleJsonContainerFactory";
    public static final String DEFAULT_CONTAINER_FACTORY_PROPERTY_NAME = "jsn4j.container.factory";
    private static final ConcurrentHashMap<String, ContainerFactory> containerFactories = new ConcurrentHashMap<>();
    private static ContainerFactory defaultContainerFactory;

    private Jsn4j() {
        throw new AssertionError("Cannot instantiate Jsn4j");
    }


    static {

        ContainerFactory factory;
        try {
            factory =createContainerFactory(getDefaultContainerFactoryClassName());
            setDefaultContainerFactory(factory);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            e.printStackTrace(System.err);
        }

    }

    public static String getDefaultContainerFactoryClassName() {
        String className = System.getProperty(DEFAULT_CONTAINER_FACTORY_PROPERTY_NAME);
        if(className == null) {
            className = DEFAULT_CONTAINER_FACTORY;
        }
        return className;
    }


    public static ContainerFactory createContainerFactory(String classPath) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException  {
        return createContainerFactory(classPath, Jsn4j.class.getClassLoader());
    }

    public static ContainerFactory createContainerFactory(String classPath, ClassLoader classLoader)throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException  {
        Class<?> clazz = classLoader.loadClass(classPath);
        if (!ContainerFactory.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Class " + classPath + " is not a ContainerFactory");
        }
        @SuppressWarnings("unchecked")
        Constructor<? extends ContainerFactory> constructor = (Constructor<? extends ContainerFactory>) clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();

    }


    public static void registerContainerFactory(String classPath) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        registerContainerFactory(classPath, Jsn4j.class.getClassLoader());
    }

    public static void registerContainerFactory(String className, ClassLoader classLoader)throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException  {
        ContainerFactory containerFactory = createContainerFactory(className, classLoader);
        registerContainerFactory(containerFactory);
    }



    public static void registerContainerFactory(ContainerFactory factory) {
        String name = factory.getJsn4jModuleName();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("ContainerFactory name cannot be null or empty");
        }
        containerFactories.put(name, factory);
    }

    public static void setDefaultContainerFactory(ContainerFactory factory) {
        String name = factory.getJsn4jModuleName();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("ContainerFactory name cannot be null or empty");
        }
        defaultContainerFactory = factory;
        registerContainerFactory(factory);
    }

    public static ContainerFactory getDefaultContainerFactory() {
        return defaultContainerFactory;
    }

    public static ContainerFactory getContainerFactory(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("ContainerFactory name cannot be null or empty");
        }
        ContainerFactory factory = containerFactories.get(name);
        if (factory == null) {
            throw new IllegalArgumentException("ContainerFactory not found: " + name);
        }
        return factory;
    }


    public static ObjectContainer newObject() {
        return defaultContainerFactory.newObject();
    }
    public static ArrayContainer newArray() {
        return defaultContainerFactory.newArray();
    }

    public static ObjectContainer newObject(ObjectContainer rootContainer) {
        if (rootContainer == null) {
            throw new IllegalArgumentException("Root container cannot be null");
        }
        return defaultContainerFactory.newObject(rootContainer);
    }
    public static ObjectContainer newObject(ArrayContainer rootContainer) {
        if (rootContainer == null) {
            throw new IllegalArgumentException("Root container cannot be null");
        }
        return defaultContainerFactory.newObject(rootContainer);
    }

    public static ArrayContainer newArray(ObjectContainer rootContainer) {
        if (rootContainer == null) {
            throw new IllegalArgumentException("Root container cannot be null");
        }
        return defaultContainerFactory.newArray(rootContainer);
    }

    public static ArrayContainer newArray(ArrayContainer rootContainer) {
        if (rootContainer == null) {
            throw new IllegalArgumentException("Root container cannot be null");
        }
        return defaultContainerFactory.newArray(rootContainer);
    }

    public static ContainerParser getParser() {
        return defaultContainerFactory.getParser();
    }

}
