// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.prop_store;

import java.io.*;
import java.util.Properties;

/**
 * Class representing a Store of properies (maps/lists nested or flat). Any stream, reader/writer, or files can be
 * used.
 */
public abstract class PropStore {
    /**
     * Default constructor that takes a amp of configuration values for the property store (not used in the is base
     * abstract class).
     *
     * @param config The properties of configuration parameters that may or may not be used by the derived classes.
     *              This may be null.
     */
    public PropStore(Properties config) {
    }

    /**
     * Convert the PropMap to the text Store format.
     *
     * @param map The map to Store.
     *
     * @return The String version of the PropMap.
     */
    public abstract String toString(PropMap map);

    /**
     * Convert the PropList to the text Store format.
     *
     * @param list The list to Store.
     * @return The String version of the PropList.
     */
    public abstract String toString(PropList list);

    /**
     * Convert a String representation of the properties to a PropMap.
     *
     * @param storeText The String representation of the properties.
     * @return The parsed PropMap.
     * @throws PropStoreException is thrown when the implementation cannot parse the text representation.
     */
    public abstract PropMap fromStringToMap(String storeText) throws PropStoreException;

    /**
     * Convert a String representation of the properties to a PropList.
     *
     * @param storeText The String representation of the properties.
     * @return The parsed PropList.
     * @throws PropStoreException is thrown when the implementation cannot parse the text representation.
     */
    public abstract PropList fromStringToList(String storeText) throws PropStoreException;

    /**
     * Read the text representation from the filename.
     *
     * @param filename The filename to read from.
     * @return The parsed property map.
     * @throws IOException if the filename cannot be found or cannot be read.
     * @throws PropStoreException is thrown when the implementation cannot parse the text representation.
     */
    public final PropMap readMap(String filename) throws IOException, PropStoreException {
        return readMap(new File(filename));
    }

    /**
     * Read the text representation from the filename.
     *
     * @param file The file to to read from.
     * @return The parsed property map.
     * @throws IOException if the filename cannot be found or cannot be read.
     * @throws PropStoreException is thrown when the implementation cannot parse the text representation.
     */
    public final PropMap readMap(File file) throws IOException, PropStoreException {
        try (InputStream stream = new FileInputStream(file)) {
            return readMap(stream);
        }
    }

    /**
     * Read the text representation from the InputStream object.
     *
     * @param stream The stream to read from.
     * @return The parsed property map.
     * @throws IOException if cannot be read from the stream.
     * @throws PropStoreException is thrown when the implementation cannot parse the text representation.
     */
    public final PropMap readMap(InputStream stream) throws IOException, PropStoreException {
        try (Reader reader = new InputStreamReader(stream)) {
            return readMap(reader);
        }
    }

    /**
     * Read the text representation from the Reader object.
     *
     * @param reader The reader to read from.
     * @return The parsed property map.
     * @throws IOException if cannot be read from the reader.
     * @throws PropStoreException is thrown when the implementation cannot parse the text representation.
     */
    public final PropMap readMap(Reader reader) throws IOException, PropStoreException {
        String storeText = readStoreText(reader);
        return fromStringToMap(storeText);
    }

    /**
     * Read the text representation from the filename.
     *
     * @param filename The filename to read from.
     * @return The parsed property list.
     * @throws IOException if the filename cannot be found or cannot be read.
     * @throws PropStoreException is thrown when the implementation cannot parse the text representation.
     */
    public final PropList readList(String filename) throws IOException, PropStoreException {
        return readList(new File(filename));
    }

    /**
     * Read the text representation from the filename.
     *
     * @param file The file to to read from.
     * @return The parsed property list.
     * @throws IOException if the file cannot be found or cannot be read.
     * @throws PropStoreException is thrown when the implementation cannot parse the text representation.
     */
    public final PropList readList(File file) throws IOException, PropStoreException {
        try (InputStream stream = new FileInputStream(file)) {
            return readList(stream);
        }
    }

    /**
     * Read the text representation from the InputStream object.
     *
     * @param stream The stream to read from.
     * @return The parsed property list.
     * @throws IOException if cannot be read from the stream.
     * @throws PropStoreException is thrown when the implementation cannot parse the text representation.
     */
    public final PropList readList(InputStream stream) throws IOException, PropStoreException {
        try (Reader reader = new InputStreamReader(stream)) {
            return readList(reader);
        }
    }

    /**
     * Read the text representation from the Reader object.
     *
     * @param reader The reader to read from.
     * @return The parsed property list.
     * @throws IOException if cannot be read from the reader.
     * @throws PropStoreException is thrown when the implementation cannot parse the text representation.
     */
    public final PropList readList(Reader reader) throws IOException, PropStoreException {
        String storeText = readStoreText(reader);
        return fromStringToList(storeText);
    }

    /**
     * Write the PropMap to the filename as text.
     *
     * @param filename The filename to write to.
     * @param map The PropMap to write to the filename.
     * @throws IOException if cannot be written to the filename.
     */
    public final void writeTo(String filename, PropMap map) throws IOException {
        writeTo(new File(filename), map);
    }

    /**
     * Write the PropMap to the filename as text.
     *
     * @param file The file to write to.
     * @param map The PropMap to write to the file.
     * @throws IOException if cannot be written to the file.
     */
    public final void writeTo(File file, PropMap map) throws IOException {
        try (OutputStream stream = new FileOutputStream(file)) {
            writeTo(stream, map);
        }
    }

    /**
     * Write the PropMap to the filename as text.
     *
     * @param stream The stream to write to.
     * @param map The PropMap to write to the stream.
     * @throws IOException if cannot be written to the stream.
     */
    public final void writeTo(OutputStream stream, PropMap map) throws IOException {
        try (Writer writer = new OutputStreamWriter(stream)) {
            writeTo(writer, map);
        }
    }

    /**
     * Write the PropMap to the filename as text.
     *
     * @param writer The writer to write to.
     * @param map The PropMap to write to the writer.
     * @throws IOException if cannot be written to the writer.
     */
    public final void writeTo(Writer writer, PropMap map) throws IOException {
        String storeText = toString(map);
        writer.write(storeText);
    }

    /**
     * Write the PropMap to the filename as text.
     *
     * @param filename The filename to write to.
     * @param list The PropMap to write to the filename.
     * @throws IOException if cannot be written to the filename.
     */
    public final void writeTo(String filename, PropList list) throws IOException {
        writeTo(new File(filename), list);
    }

    /**
     * Write the PropMap to the file as text.
     *
     * @param file The file to write to.
     * @param list The PropMap to write to the file.
     * @throws IOException if cannot be written to the file.
     */
    public final void writeTo(File file, PropList list) throws IOException {
        try (OutputStream stream = new FileOutputStream(file)) {
            writeTo(stream, list);
        }
    }

    /**
     * Write the PropMap to the stream as text.
     *
     * @param stream The stream to write to.
     * @param list The PropMap to write to the stream.
     * @throws IOException if cannot be written to the stream.
     */
    public final void writeTo(OutputStream stream, PropList list) throws IOException {
        try (Writer writer = new OutputStreamWriter(stream)) {
            writeTo(writer, list);
        }
    }

    /**
     * Write the PropMap to the writer as text.
     *
     * @param writer The stream to write to.
     * @param list The PropMap to write to the writer.
     * @throws IOException if cannot be written to the writer.
     */
    public final void writeTo(Writer writer, PropList list) throws IOException {
        String storeText = toString(list);
        writer.write(storeText);
    }

    private String readStoreText(Reader reader) throws IOException {
        try (BufferedReader buffered = new BufferedReader(reader)) {
            StringBuilder builder = new StringBuilder();
            String line = null;
            String newline = System.getProperty("line.separator");
            while((line = buffered.readLine()) != null)
                builder.append(line).append(newline);
            return builder.toString();
        }
    }
}
