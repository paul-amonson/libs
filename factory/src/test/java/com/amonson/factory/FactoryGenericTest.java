package com.amonson.factory;

import java.util.Properties;
import java.util.logging.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

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
    @BeforeEach
    public void setUp() throws Exception {
        logger = mock(Logger.class);
        factory = new FactoryType();
    }

    @Test
    public void getInstance() throws Exception {
        factory.registerClass("good", GoodType.class);
        factory.registerClass("good", GoodType.class);
        factory.getInstance("good", null,  logger);
    }

    @Test
    public void getInstanceNegative1() throws Exception {
        assertThrows(FactoryException.class, () -> {
            factory.getInstance("good", null, logger);
        });
    }

    @Test
    public void getInstanceNegative2() throws Exception {
        factory.registerClass("bad", BadType.class);
        assertThrows(FactoryException.class, () -> {
            factory.getInstance("bad", null,  logger);
        });
    }

    @Test
    public void unregisterClass() throws Exception {
        factory.registerClass("good", GoodType.class);
        factory.unregisterClass("good", GoodType.class);
        assertThrows(FactoryException.class, () -> {
            factory.getInstance("good", null,  logger);
        });
    }

    @Test
    public void getSingltonInstance() throws Exception {
        factory.registerClass("good", GoodType.class);
        InterfaceType instance1 = factory.getSingletonInstance("good", null,  logger);
        InterfaceType instance2 = factory.getSingletonInstance("good", null,  logger);
        assertEquals(instance1, instance2);
    }

    private Logger logger;
    private FactoryType factory;
}
