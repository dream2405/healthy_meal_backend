package kr.ac.dankook.ace.healthy_meal_backend.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

@Service
public class ImagePreprocessorService {

    // 기본 퀄리티 값 (70%)
    private static final float DEFAULT_QUALITY = 0.7f;

    /**
     * 입력: 이미지 바이트
     * 출력: JPEG로 재인코딩된 raw base64 문자열(접두사 없이)
     *
     * @param input 원본 이미지 바이트
     * @param longest 최장변(px) 캡
     * @param quality JPEG 품질 0.0~1.0 (예: 0.65f)
     */
    public String toJpegBase64(byte[] input, int longest, float quality) throws IOException {
        BufferedImage src = ImageIO.read(new ByteArrayInputStream(input));
        if (src == null) throw new IOException("Unsupported or corrupted image");

        int w = src.getWidth(), h = src.getHeight();
        double scale = 1.0;
        int maxSide = Math.max(w, h);
        if (maxSide > longest) {
            scale = (double) longest / (double) maxSide;
        }

        BufferedImage resized = (scale < 1.0)
                ? Thumbnails.of(src).scale(scale).asBufferedImage()
                : src;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        try (MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(baos)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality); // 0.65 등

            // progressive + 메타 제거(재인코딩으로 대부분 제거됨)
            writer.prepareWriteSequence(null);
            writer.writeToSequence(new IIOImage(resized, null, null), param);
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public byte[] base64ToBytes(String rawBase64) {
        // "data:image/..;base64," 접두사가 없는 순수 base64를 가정
        return Base64.getDecoder().decode(rawBase64);
    }
}
