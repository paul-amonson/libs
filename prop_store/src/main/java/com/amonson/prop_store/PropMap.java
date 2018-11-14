// Copyright (C) 2018 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.prop_store;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Class to store a dictionary of property name/value pairs.
 */
@SuppressWarnings("serial")
public class PropMap extends HashMap<String, Object> {
    public PropMap() {
        super();
    }

    public PropMap(Map<String, ?> map) {
        putAll(map);
    }

    public String getString(String key) {
        return getStringOrDefault(key, null);
    }

    public String getStringOrDefault(String key, String defValue) {
        if(!containsKey(key)) return defValue;
        Object obj = get(key);
        if(obj == null) return null;
        return obj.toString();
    }

    public PropMap getMap(String key) {
        return getMapOrDefault(key, null);
    }

    public PropMap getMapOrDefault(String key, PropMap defValue) {
        if(!containsKey(key)) return defValue;
        Object obj = get(key);
        if(obj == null) return null;
        return (PropMap)obj;
    }

    public PropList getArray(String key) {
        return getArrayOrDefault(key, null);
    }

    public PropList getArrayOrDefault(String key, PropList defValue) {
        if(!containsKey(key)) return defValue;
        Object obj = get(key);
        if(obj == null) return null;
        return (PropList)obj;
    }

    public Boolean getBoolean(String key) {
        return getBooleanOrDefault(key, null);
    }

    public Boolean getBooleanOrDefault(String key, Boolean defValue) {
        if(!containsKey(key)) return defValue;
        Object obj = get(key);
        if(obj == null) return null;
        return (Boolean)obj;
    }

    public Short getShort(String key) {
        return getShortOrDefault(key, null);
    }

    public Short getShortOrDefault(String key, Short defValue) {
        if(!containsKey(key)) return defValue;
        Object obj = get(key);
        if(obj == null) return null;
        return ((BigDecimal)obj).shortValue();
    }

    public Integer getInteger(String key) {
        return getIntegerOrDefault(key, null);
    }

    public Integer getIntegerOrDefault(String key, Integer defValue) {
        if(!containsKey(key)) return defValue;
        Object obj = get(key);
        if(obj == null) return null;
        return ((BigDecimal)obj).intValue();
    }

    public Long getLong(String key) {
        return getLongOrDefault(key, null);
    }

    public Long getLongOrDefault(String key, Long defValue) {
        if(!containsKey(key)) return defValue;
        Object obj = get(key);
        if(obj == null) return null;
        return ((BigDecimal)obj).longValue();
    }

    public BigInteger getBigInteger(String key) {
        return getBigIntegerOrDefault(key, null);
    }

    public BigInteger getBigIntegerOrDefault(String key, BigInteger defValue) {
        if(!containsKey(key)) return defValue;
        Object obj = get(key);
        if(obj == null) return null;
        return ((BigDecimal)obj).toBigInteger();
    }

    public Float getFloat(String key) {
        return getFloatOrDefault(key, null);
    }

    public Float getFloatOrDefault(String key, Float defValue) {
        if(!containsKey(key)) return defValue;
        Object obj = get(key);
        if(obj == null) return null;
        return ((BigDecimal)obj).floatValue();
    }

    public Double getDouble(String key) {
        return getDoubleOrDefault(key, null);
    }

    public Double getDoubleOrDefault(String key, Double defValue) {
        if(!containsKey(key)) return defValue;
        Object obj = get(key);
        if(obj == null) return null;
        return ((BigDecimal)obj).doubleValue();
    }

    public BigDecimal getBigDecimal(String key) {
        return getBigDecimalOrDefault(key, null);
    }

    public BigDecimal getBigDecimalOrDefault(String key, BigDecimal defValue) {
        if(!containsKey(key)) return defValue;
        Object obj = get(key);
        if(obj == null) return null;
        return (BigDecimal)obj;
    }

    @Override
    public Object put(String key, Object value) {
        checkKey(key);
        checkType(value);
        return super.put(key, makeStoredValue(value));
    }

    @Override
    public void putAll(Map<? extends String, ?> map) {
        for(String key: map.keySet())
            put(key, map.get(key));
    }

    @Override
    public Object replace(String key, Object newValue) {
        checkKey(key);
        checkType(newValue);
        return super.replace(key, makeStoredValue(newValue));
    }

    @Override
    public boolean replace(String key, Object oldValue, Object newValue) {
        checkKey(key);
        checkType(newValue);
        return super.replace(key, oldValue, makeStoredValue(newValue));
    }

    @Override
    public Object merge(String key, Object value, BiFunction<? super Object,? super Object,?> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void	replaceAll(BiFunction<? super String,? super Object,?> function) {
        throw new UnsupportedOperationException();
    }

    public boolean isNull(String key) {
        return get(key) == null;
    }

    private void checkKey(String key) {
        if(key.trim().equals("")) throw new IllegalArgumentException("A key in the PropMap cannot be empty!");
    }

    static void checkType(Object value) {
        if(value == null) return;
        if(!(value instanceof String) &&
                !(value instanceof PropMap) &&
                !(value instanceof PropList) &&
                !(value instanceof Number) &&
                !(value instanceof Boolean)) {
            throw new ClassCastException(String.format("'value' is not a supported type: %s",
                    value.getClass().getCanonicalName()));
        }
    }

    static Object makeStoredValue(Object value) {
        if(value instanceof Float || value instanceof Double || value instanceof Short || value instanceof Integer ||
                value instanceof Long || value instanceof BigInteger)
            return new BigDecimal(value.toString());
        return value;
    }
}
