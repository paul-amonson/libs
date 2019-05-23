// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.xdg;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * <p>Class used to open files based loosely on the XDG specification. The specification is located at
 * <a href="https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html">
 *     https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html</a>. The following features of
 * the specification are supported:</p>
 * <ul>
 *     <li>Data home folder</li>
 *     <li>Data search path</li>
 *     <li>Configuration home folder</li>
 *     <li>Configuration search path</li>
 *     <li>Cache directory (temporary files that are not sensitive)</li>
 * </ul>
 * <p>
 * The only feature not supported is the runtime directory (<b>XDG_RUNTIME_DIR</b>) for temporary files that are user
 * sensitive.</p>
 *
 */
public class Xdg {
    /**
     * Create a non-specific version of the Xdg class that will not use a application name as a subfolder.
     */
    public Xdg() {
        this(null);
    }

    /**
     * Create a specific version of the Xdg class that will use a application name as a subfolder.
     *
     * @param applicationName The name to use as an application subfolder to the base XDG specification defined folder.
     */
    public Xdg(String applicationName) {
        if(applicationName != null && !applicationName.trim().equals(""))
            applicationName_ = applicationName.trim();
        initialize();
    }

    /**
     * Open a XDG data input stream if found in the XDG_DATA_DIRS path. The XDG_DATA_HOME is the first priority if
     * specified.
     *
     * @param fileNameOnly The name to find in the data path (XDG_DATA_DIRS).
     * @return The InputStream object if successful.
     * @throws IOException When the file cannot be opened or is missing.
     */
    public InputStream openDataInputStream(String fileNameOnly) throws IOException {
        File fullPath = findDataFile(fileNameOnly);
        if(fullPath == null)
            throw new FileNotFoundException(String.format("The data file %s was not found in the XDG_DATA_DIRS!",
                    fileNameOnly));
        return new FileInputStream(fullPath);
    }

    /**
     * Open a XDG data reader if found in the XDG_DATA_DIRS path. The XDG_DATA_HOME is the first priority if
     * specified.
     *
     * @param fileNameOnly The name to find in the data path (XDG_DATA_DIRS).
     * @return The Reader object if successful.
     * @throws IOException When the file cannot be opened or is missing.
     */
    public Reader openDataReader(String fileNameOnly) throws IOException {
        File fullPath = findDataFile(fileNameOnly);
        if(fullPath == null)
            throw new FileNotFoundException(String.format("The data file %s was not found in the XDG_DATA_DIRS!",
                    fileNameOnly));
        return new FileReader(fullPath);
    }

    /**
     * Open a XDG data output stream in the XDG_DATA_HOME path.
     *
     * @param fileNameOnly The name to open in the data path (XDG_DATA_HOME).
     * @return The OutputStream object if successful.
     * @throws IOException When the file cannot be opened.
     */
    public OutputStream openDataOutputStream(String fileNameOnly) throws IOException {
        File fullPath = Paths.get(dataHome_, fileNameOnly).toFile();
        return new FileOutputStream(fullPath);
    }

    /**
     * Open a XDG data writer in the XDG_DATA_HOME path.
     *
     * @param fileNameOnly The name to open in the data path (XDG_DATA_HOME).
     * @return The Writer object if successful.
     * @throws IOException When the file cannot be opened.
     */
    public Writer openDataWriter(String fileNameOnly) throws IOException {
        File fullPath = Paths.get(dataHome_, fileNameOnly).toFile();
        return new FileWriter(fullPath);
    }

    /**
     * Open a XDG config input stream if found in the XDG_CONFIG_DIRS path. The XDG_CONFIG_HOME is the first priority if
     * specified.
     *
     * @param fileNameOnly The name to find in the config path (XDG_DATA_DIRS).
     * @return The InputStream object if successful.
     * @throws IOException When the file cannot be opened or is missing.
     */
    public InputStream openConfigInputStream(String fileNameOnly) throws IOException {
        File fullPath = findConfigFile(fileNameOnly);
        if(fullPath == null)
            throw new FileNotFoundException(String.format("The config file %s was not found in the XDG_CONFIG_DIRS!",
                    fileNameOnly));
        return new FileInputStream(fullPath);
    }

    /**
     * Open a XDG config reader if found in the XDG_CONFIG_DIRS path. The XDG_CONFIG_HOME is the first priority if
     * specified.
     *
     * @param fileNameOnly The name to find in the config path (XDG_CONFIG_DIRS).
     * @return The Reader object if successful.
     * @throws IOException When the file cannot be opened or is missing.
     */
    public Reader openConfigReader(String fileNameOnly) throws IOException {
        File fullPath = findConfigFile(fileNameOnly);
        if(fullPath == null)
            throw new FileNotFoundException(String.format("The config file %s was not found in the XDG_CONFIG_DIRS!",
                    fileNameOnly));
        return new FileReader(fullPath);
    }

    /**
     * Open a XDG config output stream in the XDG_CONFIG_HOME path.
     *
     * @param fileNameOnly The name to open in the config path (XDG_CONFIG_HOME).
     * @return The OutputStream object if successful.
     * @throws IOException When the file cannot be opened.
     */
    public OutputStream openConfigOutputStream(String fileNameOnly) throws IOException {
        File fullPath = Paths.get(configHome_, fileNameOnly).toFile();
        return new FileOutputStream(fullPath);
    }

    /**
     * Open a XDG config writer in the XDG_CONFIG_HOME path.
     *
     * @param fileNameOnly The name to open in the config path (XDG_CONFIG_HOME).
     * @return The Writer object if successful.
     * @throws IOException When the file cannot be opened.
     */
    public Writer openConfigWriter(String fileNameOnly) throws IOException {
        File fullPath = Paths.get(configHome_, fileNameOnly).toFile();
        return new FileWriter(fullPath);
    }

    /**
     * Open a XDG cache input stream if found in the XDG_CACHE_HOME path.
     *
     * @param fileNameOnly The name to open in the cache home (XDG_CACHE_HOME).
     * @return The InputStream object if successful.
     * @throws IOException When the file cannot be opened.
     */
    public InputStream openCacheInputStream(String fileNameOnly) throws IOException {
        File fullPath = Paths.get(cacheHome_, fileNameOnly).toFile();
        return new FileInputStream(fullPath);
    }

    /**
     * Open a XDG cache reader if found in the XDG_CACHE_HOME path.
     *
     * @param fileNameOnly The name to open in the cache home (XDG_CACHE_HOME).
     * @return The Reader object if successful.
     * @throws IOException When the file cannot be opened.
     */
    public Reader openCacheReader(String fileNameOnly) throws IOException {
        File fullPath = Paths.get(cacheHome_, fileNameOnly).toFile();
        return new FileReader(fullPath);
    }

    /**
     * Open a XDG cache output stream in the XDG_CACHE_HOME path.
     *
     * @param fileNameOnly The name to open in the cache home (XDG_CACHE_HOME).
     * @return The InputStream object if successful.
     * @throws IOException When the file cannot be opened.
     */
    public OutputStream openCacheOutputStream(String fileNameOnly) throws IOException {
        File fullPath = Paths.get(cacheHome_, fileNameOnly).toFile();
        return new FileOutputStream(fullPath);
    }

    /**
     * Open a XDG cache writer in the XDG_CACHE_HOME path.
     *
     * @param fileNameOnly The name to open in the cache home (XDG_CACHE_HOME).
     * @return The InputStream object if successful.
     * @throws IOException When the file cannot be opened.
     */
    public Writer openCacheWriter(String fileNameOnly) throws IOException {
        File fullPath = Paths.get(cacheHome_, fileNameOnly).toFile();
        return new FileWriter(fullPath);
    }

    /**
     * Find the file in the data path (XDG_DATA_DIRS).
     *
     * @param fileNameOnly The name to find in the data path (XDG_DATA_DIRS).
     * @return Either the File object if it exists, otherwise null is returned.
     */
    public File findDataFile(String fileNameOnly) {
        return findFileInPath(fileNameOnly, dataDirs_);
    }

    /**
     * Find the file in the config path (XDG_CONFIG_DIRS).
     *
     * @param fileNameOnly The name to find in the config path (XDG_CONFIG_DIRS).
     * @return Either the File object if it exists, otherwise null is returned.
     */
    public File findConfigFile(String fileNameOnly) {
        return findFileInPath(fileNameOnly, configDirs_);
    }

    /**
     * Get the storage location for new or updated configuration files.
     *
     * @return The folder name.
     */
    public File getConfigHome() {
        return new File(configHome_);
    }

    /**
     * Get the storage location for new or updated data files.
     *
     * @return The folder name.
     */
    public File getDataHome() {
        return new File(dataHome_);
    }

    /**
     * Get the storage location for temporary data files.
     *
     * @return The folder name.
     */
    public File getCacheHome() { return new File(cacheHome_); }

    private File findFileInPath(String fileNameOnly, List<String> path) {
        for(String parent: path) {
            File tryFile = Paths.get(parent, fileNameOnly).toFile();
            if(tryFile.exists())
                return tryFile;
        }
        return null;
    }

    private void initialize() {
        setDataHome();
        setConfigHome();
        setCacheHome();
        setDataDirs();
        setConfigDirs();
    }

    private void setDataDirs() {
        String candidate = (applicationName_ != null)?String.format("/usr/local/share/%s:/usr/share/%s",
                applicationName_, applicationName_):"/usr/local/share:/usr/share";
        candidate = getPropertyOrDefault("xdg.data.dirs", candidate);
        candidate = getEnvironmentOrDefault("XDG_DATA_DIRS", candidate);
        candidate = String.format("%s:%s", dataHome_, candidate);
        dataDirs_ = Arrays.asList(candidate.split(":"));
    }

    private void setConfigDirs() {
        String candidate = (applicationName_ != null)?String.format("/etc/xdg/%s:/etc/%s.d:/etc",
                applicationName_, applicationName_):"/etc/xdg:/etc";
        candidate = getPropertyOrDefault("xdg.config.dirs", candidate);
        candidate = getEnvironmentOrDefault("XDG_CONFIG_DIRS", candidate);
        candidate = String.format("%s:%s", configHome_, candidate);
        configDirs_ = Arrays.asList(candidate.split(":"));
    }

    private void setDataHome() {
        String candidate = Paths.get(home_, ".local", "share").toString();
        candidate = addApplicationName(candidate);
        candidate = getPropertyOrDefault("xdg.data.home", candidate);
        candidate = getEnvironmentOrDefault("XDG_DATA_HOME", candidate);
        mkdirs(candidate);
        dataHome_ = candidate;
    }

    private void setConfigHome() {
        String candidate = Paths.get(home_, ".config").toString();
        candidate = addApplicationName(candidate);
        candidate = getPropertyOrDefault("xdg.config.home", candidate);
        candidate = getEnvironmentOrDefault("XDG_CONFIG_HOME", candidate);
        mkdirs(candidate);
        configHome_ = candidate;
    }

    private void setCacheHome() {
        String candidate = Paths.get(home_, ".cache").toString();
        candidate = addApplicationName(candidate);
        candidate = getPropertyOrDefault("xdg.cache.home", candidate);
        candidate = getEnvironmentOrDefault("XDG_CACHE_HOME", candidate);
        mkdirs(candidate);
        cacheHome_ = candidate;
    }

    private String addApplicationName(String path) {
        if(applicationName_ != null)
            return Paths.get(path, applicationName_).toString();
        return path;
    }

    private String getPropertyOrDefault(String propertyName, String defaultValue) {
        String property = System.getProperty(propertyName);
        if(property == null) return defaultValue;
        if(property.trim().equals("")) return defaultValue;
        return property.trim();
    }

    private String getEnvironmentOrDefault(String envName, String defaultValue) {
        String env = environment_.get(envName);
        if(env == null) return defaultValue;
        if(env.trim().equals("")) return defaultValue;
        return env.trim();
    }

    private void mkdirs(String folder) {
        if(!isValidFolder(folder)) {
            File dir = new File(folder);
            dir.mkdirs();
        }
    }

    private boolean isValidFolder(String folder) {
        return (new File(folder)).isDirectory();
    }

    private String applicationName_ = null;
    private String dataHome_;
    private String configHome_;
    private List<String> dataDirs_ = new ArrayList<>();
    private List<String> configDirs_ = new ArrayList<>();
    private String cacheHome_;
    static String home_ = System.getProperty("user.home");
    static Map<String, String> environment_ = new HashMap<>(System.getenv());
}
