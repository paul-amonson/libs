package com.amonson.factory;

import com.amonson.logger.Logger;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

interface InterfaceType {
}

class GoodType implements InterfaceType {
    public GoodType(Properties properties, Logger logger) {}
}

class NotValidParent {
    public NotValidParent(Properties properties, Logger logger) {}
}

class BadType implements InterfaceType {
    public BadType(Logger logger) {}
}

class FactoryType extends FactoryGeneric<InterfaceType> {
}

public class FactoryGenericTest {
    @Before
    public void setUp() throws Exception {
        logger = new Logger();
        factory = new FactoryType();
    }

    @Test
    public void getInstance() throws Exception {
        factory.registerClass("good", GoodType.class);
        factory.registerClass("good", GoodType.class);
        factory.getInstance("good", null,  new Logger());
    }

    @Test(expected = FactoryException.class)
    public void getInstanceNegative1() throws Exception {
        factory.getInstance("good", null,  new Logger());
    }

    @Test(expected = FactoryException.class)
    public void getInstanceNegative2() throws Exception {
        factory.registerClass("bad", BadType.class);
        factory.getInstance("bad", null,  new Logger());
    }

    @Test(expected = FactoryException.class)
    public void unregisterClass() throws Exception {
        factory.registerClass("good", GoodType.class);
        factory.unregisterClass("good", GoodType.class);
        factory.getInstance("good", null,  new Logger());
    }

    @Test
    public void getSingltonInstance() throws Exception {
        factory.registerClass("good", GoodType.class);
        InterfaceType instance1 = factory.getSingletonInstance("good", null,  new Logger());
        InterfaceType instance2 = factory.getSingletonInstance("good", null,  new Logger());
        assertEquals(instance1, instance2);
    }

    private Logger logger;
    private FactoryType factory;
}
