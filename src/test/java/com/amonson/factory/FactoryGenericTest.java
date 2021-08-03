package com.amonson.factory;

import java.util.Properties;
import java.util.logging.*;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

interface InterfaceType {
}

class GoodType implements InterfaceType {
    public GoodType(Properties properties, Logger logger) {}
}

class BadType implements InterfaceType {
    public BadType(Logger logger) {}
}

class FactoryType extends FactoryGeneric<InterfaceType> {
    public FactoryType(Logger logger) { super(logger); }
}

public class FactoryGenericTest {
    @BeforeEach
    public void setUp() throws Exception {
        Logger logger = Mockito.mock(Logger.class);
        factory = new FactoryType(logger);
    }

    @Test
    public void getInstance() throws Exception {
        factory.registerClass("good", GoodType.class);
        factory.registerClass("good", GoodType.class);
        factory.getInstance("good", null);
    }

    @Test
    public void getInstanceNegative1() throws Exception {
        Assertions.assertThrows(FactoryException.class, () -> {
            factory.getInstance("good", null);
        });
    }

    @Test
    public void getInstanceNegative2() throws Exception {
        factory.registerClass("bad", BadType.class);
        Assertions.assertThrows(FactoryException.class, () -> {
            factory.getInstance("bad", null);
        });
    }

    @Test
    public void unregisterClass() throws Exception {
        factory.registerClass("good", GoodType.class);
        factory.unregisterClass("good", GoodType.class);
        Assertions.assertThrows(FactoryException.class, () -> {
            factory.getInstance("good", null);
        });
    }

    @Test
    public void getSingltonInstance() throws Exception {
        factory.registerClass("good", GoodType.class);
        InterfaceType instance1 = factory.getSingletonInstance("good", null);
        InterfaceType instance2 = factory.getSingletonInstance("good", null);
        Assertions.assertEquals(instance1, instance2);
    }

    private FactoryType factory;
}
