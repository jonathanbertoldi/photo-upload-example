package com.example.jonathan.photouploadexample;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MultpartUtility {

    private static final String BOUNDARY = "*****";
    private static final String LINE_FEED = "\r\n";
    private static final String TWO_HIPHENS = "--";

    private HttpURLConnection httpCon;
    private DataOutputStream dos;
    private FileInputStream fis;
    private String charset = "UTF-8";
    private OutputStream outputStream;
    private PrintWriter writer;

    public MultpartUtility(String requestUrl) throws IOException {
        URL url = new URL(requestUrl);

        httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setUseCaches(false);
        httpCon.setDoOutput(true);
        httpCon.setDoInput(true);
        httpCon.setRequestMethod("POST");
        httpCon.setRequestProperty("ENCTYPE", "multipart/form-data");
        httpCon.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

        outputStream = httpCon.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
        dos = new DataOutputStream(httpCon.getOutputStream());
    }

    public void addHeaderField(String name, String value) {
        writer.append(name + ": " + value).append(LINE_FEED);
        writer.flush();
    }

    public void addFilePart(String fieldName, File uploadFile) throws IOException {
        fis = new FileInputStream(uploadFile);

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        String fileName = uploadFile.getName();

        dos.writeBytes(TWO_HIPHENS + BOUNDARY + LINE_FEED);
        dos.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\"" + fileName + "\"" + LINE_FEED);
        dos.writeBytes(LINE_FEED);

        bytesAvailable = fis.available();
        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        buffer = new byte[bufferSize];

        bytesRead = fis.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            dos.write(buffer, 0, bufferSize);
            bytesAvailable = fis.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fis.read(buffer, 0, bufferSize);
        }

        dos.writeBytes(LINE_FEED);
        dos.writeBytes(TWO_HIPHENS + BOUNDARY + TWO_HIPHENS + LINE_FEED);

        fis.close();

        dos.flush();
        dos.close();
    }

    public String finish() throws IOException {
        StringBuilder builder = new StringBuilder();

        int status = httpCon.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));

            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            reader.close();
            httpCon.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
        return builder.toString();
    }
}
