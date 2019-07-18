/*
 * Copyright 2017, Backblaze Inc. All Rights Reserved.
 * License https://www.backblaze.com/using_b2_code.html
 */
package com.backblaze.b2.client.structures;

import com.backblaze.b2.json.B2Json;

import java.util.Comparator;
import java.util.Objects;

public class B2Part {
    @B2Json.required
    private final String fileId;
    @B2Json.required
    private final int partNumber;
    @B2Json.required
    private final long contentLength;
    @B2Json.required
    private final String contentSha1;
    @B2Json.optional  // not present in response from b2_upload_part.
    private final long uploadTimestamp;

    @B2Json.constructor(params = "fileId,partNumber,contentLength,contentSha1,uploadTimestamp")
    public B2Part(String fileId,
                  int partNumber,
                  long contentLength,
                  String contentSha1,
                  long uploadTimestamp) {
        this.fileId = fileId;
        this.partNumber = partNumber;
        this.contentLength = contentLength;
        this.contentSha1 = contentSha1;
        this.uploadTimestamp = uploadTimestamp;
    }


    public String getFileId() {
        return fileId;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getContentSha1() {
        return contentSha1;
    }

    public long getUploadTimestamp() {
        return uploadTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        B2Part b2Part = (B2Part) o;
        return getPartNumber() == b2Part.getPartNumber() &&
                getContentLength() == b2Part.getContentLength() &&
                getUploadTimestamp() == b2Part.getUploadTimestamp() &&
                Objects.equals(getFileId(), b2Part.getFileId()) &&
                Objects.equals(getContentSha1(), b2Part.getContentSha1());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileId(), getPartNumber(), getContentLength(), getContentSha1(), getUploadTimestamp());
    }

    @Override
    public String toString() {
        return "B2Part{" +
                "fileId='" + fileId + '\'' +
                ", partNumber='" + partNumber + '\'' +
                ", contentLength=" + contentLength +
                ", contentSha1='" + contentSha1 + '\'' +
                ", uploadTimestamp=" + uploadTimestamp +
                '}';
    }

    public static Comparator<B2Part> partNumberComparator = new Comparator<B2Part> (){

        @Override
        public int compare(B2Part p1, B2Part p2) {
            return p1.getPartNumber() - p2.getPartNumber();
        }
    };

}
