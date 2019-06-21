package com.amonson.shutdown_hook;

import com.amonson.logger.Logger;
import org.junit.Test;

import static org.junit.Assert.*;

public class ShutdownHookTest {
    @Test
    public void realStaticCreation() {
        ShutdownHook hook = ShutdownHook.createShutdownHook(null);
        ShutdownHook.createShutdownHook(new Logger(null));
    }

    @Test
    public void pushRemoveTests() {
        AutoCloseable closeMe = new CloseMe();
        MockShutdownHook hook = new MockShutdownHook(new Logger(null));
        hook.pushAutoCloseableResource(closeMe);
        hook.pushAutoCloseableResource(null);
        assertEquals(1, hook.closeables_.size());
        hook.pushAutoCloseableResource(closeMe);
        assertEquals(1, hook.closeables_.size());
        hook.removeAutoCloseableResource(closeMe);
        assertEquals(0, hook.closeables_.size());
        hook.removeAutoCloseableResource(closeMe);
        hook.pushAutoCloseableResource(closeMe);
        hook.pushAutoCloseableResource(new BadCloseMe());
        hook.thread_.start();
        try { Thread.sleep(50); } catch(InterruptedException e) { /* Ignore */ }
        hook.pushAutoCloseableResource(closeMe);
        hook.removeAutoCloseableResource(closeMe);
    }

    @Test
    public void badAutoCloseable() {
        MockShutdownHook hook = new MockShutdownHook(null);
        hook.pushAutoCloseableResource(new BadCloseMe());
        hook.thread_.start();
        try { Thread.sleep(50); } catch(InterruptedException e) { /* Ignore */ }
    }

    static class CloseMe implements AutoCloseable {
        @Override
        public void close() throws Exception {}
    }

    static class BadCloseMe implements AutoCloseable {
        @Override
        public void close() throws Exception { throw new Exception(); }
    }

    static class MockShutdownHook extends ShutdownHook {
        MockShutdownHook(Logger logger) { super(logger); }

        @Override
        void addHook(Thread hookThread) { thread_ = hookThread; }

        Thread thread_;
    }
}
