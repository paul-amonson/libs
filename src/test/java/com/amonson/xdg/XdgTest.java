// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.xdg;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

public class XdgTest {
    private static String tmpFolder = Paths.get("./build", "tmp").toString();

    @BeforeEach
    public void setUp() throws IOException {
        Xdg.home_ = tmpFolder;
        Xdg.environment_.remove("XDG_DATA_HOME");
        Xdg.environment_.remove("XDG_CONFIG_HOME");
        Xdg.environment_.remove("XDG_CACHE_HOME");
        Xdg.environment_.remove("XDG_CONFIG_DIRS");
        Xdg.environment_.remove("XDG_DATA_DIRS");
        System.clearProperty("xdg.data.home");
        System.clearProperty("xdg.config.home");
        System.clearProperty("xdg.cache.home");
        System.clearProperty("xdg.config.dirs");
        System.clearProperty("xdg.data.dirs");
        try (Writer writer = new FileWriter("./build/tmp/testFile1")) {
            writer.write("This is a test.\nThis is only a test.\n");
        }
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertTrue(deleteTree(Paths.get(tmpFolder, ".local").toString()));
        Assertions.assertTrue(deleteTree(Paths.get(tmpFolder, ".config").toString()));
        Assertions.assertTrue(deleteTree(Paths.get(tmpFolder, ".cache").toString()));
    }

    private boolean deleteTree(String folder) {
        final class DeleteVisitor extends SimpleFileVisitor<Path> {
            private boolean ok = true;

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                boolean result = file.toFile().delete();
                if(!result) ok = false;
                return result?FileVisitResult.CONTINUE:FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) {
                boolean result = dir.toFile().delete();
                if(!result) ok = false;
                return result?FileVisitResult.CONTINUE:FileVisitResult.TERMINATE;
            }
        }
        File dir = new File(folder);
        if(!dir.exists()) return true;
        try {
            DeleteVisitor visitor = new DeleteVisitor();
            Files.walkFileTree(dir.toPath(), visitor);
            return visitor.ok;
        } catch(IOException e) {
            return false;
        }
    }

    @Test
    public void ctor() {
        new Xdg();
        new Xdg("");
        Xdg xdg = new Xdg("test");
        assertEquals(Paths.get(tmpFolder, ".config", "test").toString(), xdg.getConfigHome().toString());
        assertEquals(Paths.get(tmpFolder, ".local", "share", "test").toString(), xdg.getDataHome().toString());

        assertEquals(Paths.get(tmpFolder, ".cache", "test").toString(), xdg.getCacheHome().toString());
    }

    @Test
    public void properties() {
        System.setProperty("xdg.data.home", tmpFolder);
        System.setProperty("xdg.config.home", tmpFolder);
        System.setProperty("xdg.cache.home", tmpFolder);
        new Xdg("test");
        Xdg xdg = new Xdg("test");
        assertEquals(tmpFolder, xdg.getConfigHome().toString());
        assertEquals(tmpFolder, xdg.getDataHome().toString());
        assertEquals(tmpFolder, xdg.getCacheHome().toString());
    }

    @Test
    public void environment() {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg("test");
        assertEquals(tmpFolder, xdg.getConfigHome().toString());
        assertEquals(tmpFolder, xdg.getDataHome().toString());
        assertEquals(tmpFolder, xdg.getCacheHome().toString());
    }

    @Test
    public void openDataInputStreamTests() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        xdg.openDataInputStream("testFile1");
    }

    @Test
    public void openDataReaderTests() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        xdg.openDataReader("testFile1");
    }

    @Test
    public void openDataInputStreamTestsNegative() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        Assertions.assertThrows(IOException.class, () -> {
            xdg.openDataInputStream("testFile2");
        });
    }

    @Test
    public void openDataReaderTestsNegative() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        Assertions.assertThrows(IOException.class, () -> {
            xdg.openDataReader("testFile2");
        });
    }

    @Test
    public void openConfigInputStreamTests() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        xdg.openConfigInputStream("testFile1");
    }

    @Test
    public void openConfigReaderTests() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        xdg.openConfigReader("testFile1");
    }

    @Test
    public void openCacheInputStreamTests() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        xdg.openCacheInputStream("testFile1");
    }

    @Test
    public void openCacheReaderTests() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        xdg.openCacheReader("testFile1");
    }

    @Test
    public void openConfigInputStreamTestsNegative() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        Assertions.assertThrows(IOException.class, () -> {
            xdg.openConfigInputStream("testFile2");
        });
    }

    @Test
    public void openConfigReaderTestsNegative() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        Assertions.assertThrows(IOException.class, () -> {
            xdg.openConfigReader("testFile2");
        });
    }

    @Test
    public void openDataOutputStreamTests() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        xdg.openDataOutputStream("testFile3");
    }

    @Test
    public void openDataWriterTests() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        xdg.openDataWriter("testFile4");
    }

    @Test
    public void openConfigOutputStreamTests() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        xdg.openConfigOutputStream("testFile3");
    }

    @Test
    public void openConfigWriterTests() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        xdg.openConfigWriter("testFile4");
    }

    @Test
    public void openCacheOutputStreamTests() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        xdg.openCacheOutputStream("testFile3");
    }

    @Test
    public void openCacheWriterTests() throws IOException {
        Xdg.environment_.put("XDG_DATA_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CONFIG_HOME", tmpFolder);
        Xdg.environment_.put("XDG_CACHE_HOME", tmpFolder);
        Xdg xdg = new Xdg();
        xdg.openCacheWriter("testFile4");
    }
}
