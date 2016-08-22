package com.mhy.netty.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: mahaiyang
 * Date: 2015/11/27
 * Time: 16:18
 */
public class FileItem implements  java.io.Serializable {
    private static final Logger log = LoggerFactory.getLogger(FileItem.class);
    private final  byte[] bytes;
    private final String contentType;
    private final String fileName;

    public FileItem(byte[] bytes, String contentType, String fileName) {
        this.bytes = bytes;
        this.contentType = contentType;
        this.fileName = fileName;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "FileItem{" +
                "contentType='" + contentType + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileLength='" + bytes.length + '\'' +
                '}';
    }
}
