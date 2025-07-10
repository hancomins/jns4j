package com.hancomins.jsn4j;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

public class PrimitiveValue implements ContainerValue {

    protected final Object raw;

    @SuppressWarnings("unused")
    protected PrimitiveValue() {
        this.raw = null;
    }

    public PrimitiveValue(Object raw) {
        this.raw = raw;
        // 타입 검사
        if(raw != null && !(raw instanceof CharSequence) && !(raw instanceof Number) && !(raw instanceof Boolean) && !(raw instanceof byte[])) {
            throw new IllegalArgumentException("PrimitiveValue is only support String, Number, Boolean and byte[]");
        }
    }



    @Override
    public ValueType getValueType() {
        return ValueType.PRIMITIVE;
    }

    @Override
    public Object raw() {
        return raw;
    }

    public boolean isNull() {
        return this.raw == null;
    }



    // ---- 숫자형 변환 ----




    public short asShort() {
        if(raw instanceof Short) {
            return (short) raw;
        } else if(raw instanceof Number) {
            return ((Number) raw).shortValue();
        } else if(raw instanceof String) {
            try {
                return Short.parseShort((String) raw);
            } catch (NumberFormatException e) {
                return Short.MIN_VALUE;
            }
        } else if(raw instanceof byte[]) {
            return (short) bufferToInt((byte[]) raw);
        } else {
            return Short.MIN_VALUE;
        }
    }

    public short asShortOr(short defaultValue) {
        short value = asShort();
        if (value != Short.MIN_VALUE) {
            return value;
        } else {
            return defaultValue;
        }
    }

    public int asInt() {
        if(raw instanceof Integer) {
            return (int) raw;
        } else if(raw instanceof Number) {
            return ((Number) raw).intValue();
        } else if(raw instanceof String) {
            try {
                return Integer.parseInt((String) raw);
            } catch (NumberFormatException e) {
                return Integer.MIN_VALUE;
            }
        } else if(raw instanceof byte[]) {
            return bufferToInt((byte[]) raw);
        } else {
            return Integer.MIN_VALUE;
        }
    }

    public int asIntOr(int defaultValue) {
        int value = asInt();
        if (value != Integer.MIN_VALUE) {
            return value;
        } else {
            return defaultValue;
        }
    }

    public long asLong() {
        if(raw instanceof Long) {
            return (long) raw;
        } else if(raw instanceof Number) {
            return ((Number) raw).longValue();
        } else if(raw instanceof String) {
            try {
                return Long.parseLong((String) raw);
            } catch (NumberFormatException e) {
                return Long.MIN_VALUE;
            }
        } else if(raw instanceof byte[]) {
           return bufferToLong((byte[]) raw);
        } else {
            return Long.MIN_VALUE;
        }
    }

    public long asLongOr(long defaultValue) {
        long value = asLong();
        if (value != Long.MIN_VALUE) {
            return value;
        } else {
            return defaultValue;
        }
    }

    protected static int bufferToInt(byte[] b) {
        int result = 0;
        // 만약 byte[]의 길이가 4보다 작으면, 새로운 byte[]를 만들어서 나머지 부분을 0으로 채운다.
        if (b.length < 4) {
            byte[] newB = new byte[4];
            System.arraycopy(b, 0, newB, 4 - b.length, b.length);
            b = newB;
        }
        // 배열 길이가 4보다 크면 처음 4바이트만 사용
        @SuppressWarnings("DataFlowIssue") int len = Math.min(b.length, 4);
        for (int i = 0; i < len; i++) {
            result |= ((b[i] & 0xFF)) << (8 * (3 - i));
        }
        return result;
    }


    protected static long bufferToLong(byte[] b) {
        long result = 0;
        // 만약 byte[]의 길이가 8보다 작으면, 새로운 byte[]를 만들어서 나머지 부분을 0으로 채운다.
        if (b.length < 8) {
            byte[] newB = new byte[8];
            System.arraycopy(b, 0, newB, 8 - b.length, b.length);
            b = newB;
        }
        // 배열 길이가 8보다 크면 처음 8바이트만 사용
        @SuppressWarnings("DataFlowIssue") int len = Math.min(b.length, 8);
        for (int i = 0; i < len; i++) {
            result |= ((long) (b[i] & 0xFF)) << (8 * (7 - i));
        }
        return result;
    }


    public float asFloat() {
        if(raw instanceof Float) {
            return (float) raw;
        } else if(raw instanceof Number) {
            return ((Number) raw).floatValue();
        } else if(raw instanceof String) {
            try {
                return Float.parseFloat((String) raw);
            } catch (NumberFormatException e) {
                return Float.NaN;
            }
        } else if(raw instanceof byte[]) {
            int int32Value = bufferToInt((byte[]) raw);
            return Float.intBitsToFloat(int32Value);
        } else {
            return Float.NaN;
        }
    }

    public float asFloatOr(float defaultValue) {
        float value = asFloat();
        if (!Float.isNaN(value)) {
            return value;
        } else {
            return defaultValue;
        }
    }

    public double asDouble() {
        if(raw instanceof Double) {
            return (double) raw;
        } else if(raw instanceof Number) {
            return ((Number) raw).doubleValue();
        } else if(raw instanceof String) {
            try {
                return Double.parseDouble((String) raw);
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
        } else if(raw instanceof byte[]) {
            long int64Value = bufferToLong((byte[]) raw);
            return Double.longBitsToDouble(int64Value);
        } else {
            return Double.NaN;
        }

    }

    public double asDoubleOr(double defaultValue) {
        double value = asDouble();
        if (!Double.isNaN(value)) {
            return value;
        } else {
            return defaultValue;
        }
    }

    // ---- 불리언 ----

    @SuppressWarnings("DuplicatedCode")
    public boolean asBoolean() {
        if (raw instanceof Boolean) return (boolean) raw;
        if (raw instanceof Number) return ((Number) raw).intValue() > 0;
        if (raw instanceof String) {
            String str = (String) raw;
            return str.equalsIgnoreCase("true") || str.equals("1");
        }
        if (raw instanceof byte[]) {
            byte[] bytes = (byte[]) raw;
            return bytes.length > 0 && bytes[0] != 0;
        }
        return false;
    }


    @SuppressWarnings("DuplicatedCode")
    private Boolean asBooleanBox() {
        if (raw instanceof Boolean) return (boolean) raw;
        if (raw instanceof Number) return ((Number) raw).intValue() > 0;
        if (raw instanceof String) {
            String str = (String) raw;
            return str.equalsIgnoreCase("true") || str.equals("1");
        }
        if (raw instanceof byte[]) {
            byte[] bytes = (byte[]) raw;
            return bytes.length > 0 && bytes[0] != 0;
        }
        return null;
    }


    public boolean asBooleanOr(boolean defaultValue) {
        Boolean value = asBooleanBox();
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }

    // ---- 문자열 ----

    public String asString() {
        if (raw instanceof String) return (String) raw;
        if (raw instanceof byte[]) {
            byte[] bytes = (byte[]) raw;
            return new String(bytes);
        }
        return String.valueOf(raw);
    }


    // ---- 바이트 배열 ----

    public byte[] asByteArray() {
        if (raw instanceof byte[]) return (byte[]) raw;
        if (raw instanceof String) {
            String str = (String) raw;
            return Base64.getDecoder().decode(str);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(8);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            if (raw instanceof Integer || raw instanceof Short || raw instanceof Byte) {
                int value = (int) raw;
                dataOutputStream.writeInt(value);
                return byteArrayOutputStream.toByteArray();
            } else if(raw instanceof Long) {
                long value = (long) raw;
                dataOutputStream.writeLong(value);
                return byteArrayOutputStream.toByteArray();
            } else if (raw instanceof Float) {
                float value = (float) raw;
                dataOutputStream.writeFloat(value);
                return byteArrayOutputStream.toByteArray();
            } else if (raw instanceof Double) {
                double value = (double) raw;
                dataOutputStream.writeDouble(value);
                return byteArrayOutputStream.toByteArray();
            } else if (raw instanceof Boolean) {
                boolean value = (boolean) raw;
                dataOutputStream.writeBoolean(value);
                return byteArrayOutputStream.toByteArray();
            }
            else if (raw instanceof Number) {
                long value = ((Number) raw).longValue();
                return new byte[]{(byte) value};
            }
        } catch (Exception ignored) {}
        return null;

    }

    public byte[] asByteArrayOr(byte[] defaultValue) {
        try {
            return asByteArray();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ---- equals & hashCode ----

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o instanceof PrimitiveValue) {
            o = ((PrimitiveValue)  o).raw;
        }
        if(o == null) return false;
        return Objects.equals(o, this.raw);
    }

    @Override
    public int hashCode() {
        if(raw == null) return 0;
        if(raw instanceof byte[]) {
            return Arrays.hashCode((byte[]) raw);
        }
        return raw.hashCode();
    }




    @Override
    public String toString() {
        return String.valueOf(asString());
    }
}
