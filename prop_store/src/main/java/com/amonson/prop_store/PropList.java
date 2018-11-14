// Copyright (C) 2018 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.prop_store;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Class to store a list of properties.
 */
@SuppressWarnings("serial")
public class PropList extends ArrayList<Object> {
    public PropList() {
        super();
    }

    public PropList(Collection<?> list) {
        addAll(list);
    }

    public String getString(int index) {
        return get(index).toString();
    }

    public PropMap getMap(int index) {
        return (PropMap)get(index);
    }

    public PropList getArray(int index) {
        return (PropList)get(index);
    }

    public Boolean getBoolean(int index) {
        return (Boolean)get(index);
    }

    public Short getShort(int index) {
        if(isNull(index)) return null;
        return ((BigDecimal)get(index)).shortValue();
    }

    public Integer getInteger(int index) {
        if(isNull(index)) return null;
        return ((BigDecimal)get(index)).intValue();
    }

    public Long getLong(int index) {
        if(isNull(index)) return null;
        return ((BigDecimal)get(index)).longValue();
    }

    public BigInteger getBigInteger(int index) {
        if(isNull(index)) return null;
        return ((BigDecimal)get(index)).toBigInteger();
    }

    public Float getFloat(int index) {
        if(isNull(index)) return null;
        return ((BigDecimal)get(index)).floatValue();
    }

    public Double getDouble(int index) {
        if(isNull(index)) return null;
        return ((BigDecimal)get(index)).doubleValue();
    }

    public BigDecimal getBigDecimal(int index) {
        if(isNull(index)) return null;
        return (BigDecimal)get(index);
    }

    public boolean isNull(int index) {
        return get(index) == null;
    }
    
    @Override
    public boolean add(Object value) {
        PropMap.checkType(value);
        return super.add(PropMap.makeStoredValue(value));
    }
    
    @Override
    public void add(int index, Object value) {
        PropMap.checkType(value);
        super.add(index, PropMap.makeStoredValue(value));
    }
    
    @Override
    public boolean addAll(Collection<?> collection) {
        boolean rv = true;
        for(Object item: collection)
            rv = rv && add(item);
        return rv;
    }

    @Override
    public boolean addAll(int index, Collection<?> collection) {
        for(Object item: collection)
            add(index++, item);
        return true;
    }
    
    @Override
    public Object set(int index, Object value) {
        PropMap.checkType(value);
        return super.set(index, PropMap.makeStoredValue(value));
    }
}
