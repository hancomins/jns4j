package com.hancomins.jsn4j;


import java.util.Objects;

public class ContainerValues {

    /**
     * 두 ContainerValue 객체를 비교하여 동일한지 확인합니다.
     *
     * @param a 비교할 첫 번째 ContainerValue
     * @param b 비교할 두 번째 ContainerValue
     * @return 두 ContainerValue 객체가 동일하면 true, 그렇지 않으면 false
     */
    public static boolean equals(ContainerValue a, ContainerValue b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.getValueType() != b.getValueType()) return false;

        switch (a.getValueType()) {

            case PRIMITIVE:
                return Objects.equals(a.raw(), b.raw());
            case OBJECT:
                ObjectContainer objA = a.asObject();
                ObjectContainer objB = b.asObject();
                if (objA.size() != objB.size()) return false;
                for (String key : objA.keySet()) {
                    if (!equals(objA.get(key), objB.get(key))) return false;
                }
                return true;
            case ARRAY:
                ArrayContainer arrA = a.asArray();
                ArrayContainer arrB = b.asArray();
                if (arrA.size() != arrB.size()) return false;
                for (int i = 0; i < arrA.size(); i++) {
                    if (!equals(arrA.get(i), arrB.get(i))) return false;
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * 하나의 ContainerValue의 내용을 다른 ContainerValue에 복사합니다.
     *
     * @param target 복사할 대상 ContainerValue
     * @param source 복사할 원본 ContainerValue
     * @throws IllegalArgumentException 대상과 원본의 값 유형이 일치하지 않는 경우
     * @throws UnsupportedOperationException 값 유형에 대해 복사 작업이 지원되지 않는 경우
     */
    public static void copy(ContainerValue target, ContainerValue source) {
        if (target == null || source == null) return;
        if (target.getValueType() != source.getValueType()) {
            throw new IllegalArgumentException("Cannot copy between different value types: "
                    + target.getValueType() + " and " + source.getValueType());
        }

        switch (target.getValueType()) {
            case OBJECT:
                ObjectContainer tgtObj = target.asObject();
                ObjectContainer srcObj = source.asObject();
                tgtObj.clear();
                for (String key : srcObj.keySet()) {
                    ContainerValue val = srcObj.get(key);
                    addValue(tgtObj, key, val);
                }
                break;
            case ARRAY:
                ArrayContainer tgtArr = target.asArray();
                ArrayContainer srcArr = source.asArray();
                tgtArr.clear();
                for (int i = 0; i < srcArr.size(); i++) {
                    ContainerValue val = srcArr.get(i);
                    addValue(tgtArr, val);
                }
                break;
            default:
                throw new UnsupportedOperationException("Copy not supported for type: " + target.getValueType());
        }
    }

    /**
     * 하나의 ContainerValue의 내용을 다른 ContainerValue에 병합합니다.
     *
     * @param target 병합할 대상 ContainerValue
     * @param source 병합할 원본 ContainerValue
     * @throws IllegalArgumentException 대상과 원본의 값 유형이 일치하지 않는 경우
     * @throws UnsupportedOperationException 값 유형에 대해 병합 작업이 지원되지 않는 경우
     */
    public static void merge(ContainerValue target, ContainerValue source) {
        if (target == null || source == null || source.isNull()) return;
        if (target.getValueType() != source.getValueType()) {
            throw new IllegalArgumentException("Cannot merge different value types: "
                    + target.getValueType() + " vs " + source.getValueType());
        }

        switch (target.getValueType()) {
            case OBJECT:
                ObjectContainer tgtObj = target.asObject();
                ObjectContainer srcObj = source.asObject();
                for (String key : srcObj.keySet()) {
                    ContainerValue val = srcObj.get(key);
                    addValue(tgtObj, key, val);
                }
                break;
            case ARRAY:
                ArrayContainer tgtArr = target.asArray();
                ArrayContainer srcArr = source.asArray();
                for (int i = 0; i < srcArr.size(); i++) {
                    ContainerValue val = srcArr.get(i);
                    addValue(tgtArr, val);
                }
                break;
            default:
                throw new UnsupportedOperationException("Merge not supported for type: " + target.getValueType());
        }
    }

    /**
     * 두 ContainerValue 객체의 교집합을 계산합니다.
     *
     * @param a 첫 번째 ContainerValue
     * @param b 두 번째 ContainerValue
     * @return 두 입력 값의 교집합을 나타내는 새로운 ContainerValue
     */
    public static ContainerValue intersection(ContainerValue a, ContainerValue b) {
        if (a == null || b == null || a.isNull() || b.isNull()) return new PrimitiveValue(null);
        if (a.getValueType() != b.getValueType()) return new PrimitiveValue(null);

        switch (a.getValueType()) {
            case PRIMITIVE:
                return Objects.equals(a.raw(), b.raw()) ? new PrimitiveValue(a.raw()) : new PrimitiveValue(null);
            case OBJECT:
                ObjectContainer objA = a.asObject();
                ObjectContainer objB = b.asObject();
                ObjectContainer result = objA.getFactory().newObject();
                for (String key : objA.keySet()) {
                    if (objB.containsKey(key) && equals(objA.get(key), objB.get(key))) {
                        ContainerValue val = objA.get(key);
                        addValue(result, key, val);
                    }
                }
                return result;
            case ARRAY:
                ArrayContainer arrA = a.asArray();
                ArrayContainer arrB = b.asArray();
                ArrayContainer resultArr = arrA.getFactory().newArray();
                int size = Math.min(arrA.size(), arrB.size());
                for (int i = 0; i < size; i++) {
                    if (equals(arrA.get(i), arrB.get(i))) {
                        ContainerValue val = arrA.get(i);
                        addValue(resultArr, val);
                    }
                }
                return resultArr;
            default:
                return new PrimitiveValue(null);
        }
    }

    /**
     * 두 ContainerValue 객체의 차집합을 계산합니다.
     *
     * @param a 첫 번째 ContainerValue
     * @param b 두 번째 ContainerValue
     * @return 두 입력 값의 차집합을 나타내는 새로운 ContainerValue
     */
    public static ContainerValue diff(ContainerValue a, ContainerValue b) {
        if (a == null || a.isNull()) return new PrimitiveValue(null);
        if (b == null || b.isNull()) return a;
        if (a.getValueType() != b.getValueType()) return a;

        switch (a.getValueType()) {
            case PRIMITIVE:
                return Objects.equals(a.raw(), b.raw()) ? new PrimitiveValue(null) : new PrimitiveValue(a.raw());
            case OBJECT:
                ObjectContainer objA = a.asObject();
                ObjectContainer objB = b.asObject();
                ObjectContainer result = objA.getFactory().newObject();
                for (String key : objA.keySet()) {
                    if (!objB.containsKey(key) || !equals(objA.get(key), objB.get(key))) {
                        ContainerValue val = objA.get(key);
                        addValue(result, key, val);
                    }
                }
                return result;
            case ARRAY:
                ArrayContainer arrA = a.asArray();
                ArrayContainer arrB = b.asArray();
                ArrayContainer resultArr = arrA.getFactory().newArray();
                int size = Math.min(arrA.size(), arrB.size());
                for (int i = 0; i < size; i++) {
                    if (!equals(arrA.get(i), arrB.get(i))) {
                        ContainerValue val = arrA.get(i);
                       addValue(resultArr, val);
                    }
                }
                for (int i = arrB.size(); i < arrA.size(); i++) {
                    ContainerValue val = arrA.get(i);
                    addValue(resultArr, val);
                }
                return resultArr;
            default:
                return new PrimitiveValue(null);
        }
    }

    /**
     * ArrayContainer에 값을 추가합니다.
     *
     * @param resultArr 값을 추가할 ArrayContainer
     * @param val 추가할 값
     */
    private static void addValue(ArrayContainer resultArr, ContainerValue val) {
        if(val == null) {
            resultArr.put(new PrimitiveValue(null));
        }
        else if (val.isPrimitive()) {
            resultArr.put(new PrimitiveValue(val.raw()));
        } else if (val.isObject()) {
            resultArr.putCopy(val.asObject());
        } else if (val.isArray()) {
            resultArr.putCopy(val.asArray());
        }
    }

    /**
     * ObjectContainer에 값을 추가합니다.
     *
     * @param resultObj 값을 추가할 ObjectContainer
     * @param key 값을 추가할 키
     * @param val 추가할 값
     */
    private static void addValue(ObjectContainer resultObj,String key, ContainerValue val) {
        if(val == null) {
            resultObj.put(key, new PrimitiveValue(null));
        }
        else if (val.isPrimitive()) {
            resultObj.put(key,new PrimitiveValue(val.raw()));
        } else if (val.isObject()) {
            resultObj.putCopy(key,val.asObject());
        } else if (val.isArray()) {
            resultObj.putCopy(key,val.asArray());
        }
    }
}
