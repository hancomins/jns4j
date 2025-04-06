package com.hancomins.jsn4j;




import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("UnusedReturnValue")
public class Jsn4j {

    private static final String DEFAULT_CONTAINER_FACTORY = "com.hancomins.jsn4j.simple.SimpleJsonContainerFactory";
    public static final String DEFAULT_CONTAINER_FACTORY_PROPERTY_NAME = "jsn4j.container.factory";
    private static final ConcurrentHashMap<String, ContainerFactory> nameContainerFactories = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<? extends ContainerFactory>, ContainerFactory> classContainerFactories = new ConcurrentHashMap<>();
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

    public static ContainerValue parse(String json) {
        return defaultContainerFactory.getParser().parse(json);
    }

    public static ContainerValue parse(InputStream inputStream) {
        return defaultContainerFactory.getParser().parse(inputStream);
    }

    public static ContainerValue parse(Reader reader) {
        return defaultContainerFactory.getParser().parse(reader);
    }

    public static ContainerValue parse(Class<? extends ContainerFactory> factoryClass,String json) {
        //noinspection DuplicatedCode
        if (factoryClass == null) {
            throw new IllegalArgumentException("ContainerFactory class cannot be null");
        }
        ContainerFactory factory =  classContainerFactories.get(factoryClass);
        if(factory == null) {
            throw new IllegalArgumentException("Unregistered container factory class: " + factoryClass.getName());
        }
        return factory.getParser().parse(json);
    }

    public static ContainerValue parse(Class<? extends ContainerFactory> factoryClass, InputStream inputStream) {
        //noinspection DuplicatedCode
        if (factoryClass == null) {
            throw new IllegalArgumentException("ContainerFactory class cannot be null");
        }
        ContainerFactory factory =  classContainerFactories.get(factoryClass);
        if(factory == null) {
            throw new IllegalArgumentException("Unregistered container factory class: " + factoryClass.getName());
        }
        return factory.getParser().parse(inputStream);
    }

    public static ContainerValue parse(String name, Reader reader) {
        //noinspection DuplicatedCode
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("ContainerFactory name cannot be null or empty");
        }
        ContainerFactory factory =  nameContainerFactories.get(name);
        if(factory == null) {
            throw new IllegalArgumentException("Unknown container factory name " + name);
        }
        return factory.getParser().parse(reader);
    }

    public static ContainerValue parse(String name, String json) {
        //noinspection DuplicatedCode
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("ContainerFactory name cannot be null or empty");
        }
        ContainerFactory factory =  nameContainerFactories.get(name);
        if(factory == null) {
            throw new IllegalArgumentException("Unknown container factory name " + name);
        }
        return factory.getParser().parse(json);
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
        //noinspection unchecked
        return createContainerFactory((Class<? extends ContainerFactory>) clazz);
    }


    public static ContainerFactory createContainerFactory(Class<? extends ContainerFactory> contanerFactoryClass) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {

        try {
            Constructor<? extends ContainerFactory> constructor = contanerFactoryClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Method[] methods = contanerFactoryClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals("getInstance") || method.getName().equals("newInstance") || method.getName().equals("create")) {
                    // 인자가 하나라도 있으면 continue
                    if (method.getParameterCount() > 0) {
                        continue;
                    }
                    // 인자가 없으면
                    if (!method.getReturnType().isAssignableFrom(ContainerFactory.class)) {
                        continue;
                    }
                    // 인자가 없고 리턴타입이 ContainerFactory이면
                    method.setAccessible(true);
                    try {
                        return (ContainerFactory) method.invoke(null);
                    } catch (IllegalAccessException | InvocationTargetException ignored) {}
                }
            }
            throw e;
        }
    }

    public static void registerContainerFactory(Class<? extends ContainerFactory> containerFactoryClass) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        ContainerFactory factory = classContainerFactories.get(containerFactoryClass);
        if (factory == null) {
            factory = createContainerFactory(containerFactoryClass);
            registerContainerFactory(factory);
        }
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
        nameContainerFactories.put(name, factory);
        classContainerFactories.put(factory.getClass(), factory);
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

    public static ContainerFactory getContainerFactoryByName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("ContainerFactory name cannot be null or empty");
        }
        ContainerFactory factory = nameContainerFactories.get(name);
        if (factory == null) {
            throw new IllegalArgumentException("ContainerFactory not found: " + name);
        }
        return factory;
    }

    public static ContainerFactory getContainerFactoryByClass(Class<? extends ContainerFactory> factoryClass) {
        if (factoryClass == null) {
            throw new IllegalArgumentException("ContainerFactory class cannot be null");
        }
        ContainerFactory factory = classContainerFactories.get(factoryClass);
        if (factory == null) {
            throw new IllegalArgumentException("ContainerFactory not found: " + factoryClass.getName());
        }
        return factory;
    }

    public static ContainerFactory getContainerFactoryByClassName(String className) {
        java.util.Map.Entry<Class<? extends ContainerFactory>, ContainerFactory> resultEntry  = classContainerFactories.entrySet().stream().filter(entry -> entry.getValue().getClass().getName().equals(className)).findFirst().orElse(null);
        if (resultEntry != null) {
            return resultEntry.getValue();
        } else {
            throw new IllegalArgumentException("ContainerFactory not found: " + className);
        }

    }


    public static ObjectContainer newObject() {
        return defaultContainerFactory.newObject();
    }
    public static ArrayContainer newArray() {
        return defaultContainerFactory.newArray();
    }

    public static ObjectContainer newObject(ObjectContainer rootContainer) {
        if (rootContainer == null) {
            return defaultContainerFactory.newObject();
        }
        return defaultContainerFactory.newObject(rootContainer);
    }
    public static ObjectContainer newObject(ArrayContainer rootContainer) {
        if (rootContainer == null) {
            return defaultContainerFactory.newObject();
        }
        return defaultContainerFactory.newObject(rootContainer);
    }

    public static ArrayContainer newArray(ObjectContainer rootContainer) {
        if (rootContainer == null) {
            return defaultContainerFactory.newArray();
        }
        return defaultContainerFactory.newArray(rootContainer);
    }

    public static ArrayContainer newArray(ArrayContainer rootContainer) {
        if (rootContainer == null) {
            return defaultContainerFactory.newArray();
        }
        return defaultContainerFactory.newArray(rootContainer);
    }

    public static ContainerParser getParser() {
        return defaultContainerFactory.getParser();
    }

}
