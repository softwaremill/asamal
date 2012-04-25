package pl.softwaremill.asamal.controller;

import java.io.InputStream;

/**
 * Wrapper of an input stream to download, to pass some extra information such as file name
 */
public class DownloadDescription {

    private final InputStream inputStream;

    private final String fileName;

    public DownloadDescription(InputStream inputStream, String fileName) {
        this.inputStream = inputStream;
        this.fileName = fileName;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getFileName() {
        return fileName;
    }
}
