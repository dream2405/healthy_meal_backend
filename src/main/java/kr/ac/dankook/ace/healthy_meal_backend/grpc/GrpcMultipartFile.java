package kr.ac.dankook.ace.healthy_meal_backend.grpc;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class GrpcMultipartFile implements MultipartFile {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public GrpcMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
        this.name = name;
        this.originalFilename = (originalFilename != null) ? originalFilename : "";
        this.contentType = contentType;
        this.content = (content != null) ? content : new byte[0];
    }

    @Override
    public String getName() { return name; }

    @Override
    public String getOriginalFilename() { return originalFilename; }

    @Override
    public String getContentType() { return contentType; }

    @Override
    public boolean isEmpty() { return content.length == 0; }

    @Override
    public long getSize() { return content.length; }

    @Override
    public byte[] getBytes() { return content; }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(content);
        }
    }
}
