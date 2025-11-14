package Devroup.hidaddy.util;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.net.URI;
import java.net.URLDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NcpObjectStorageUploader {

    // ====== 설정 값 ======
    private static final String ENDPOINT = "https://kr.object.ncloudstorage.com";
    private static final String REGION   = "kr-standard";
    private static final String NCP_ENDPOINT_HOST = "kr.object.ncloudstorage.com";

    @Value("${ncp.access-key}") private String accessKey;
    @Value("${ncp.secret-key}") private String secretKey;

    @Value("${ncp.object-storage.bucket}") private String bucket;
    @Value("${ncp.object-storage.cdn-domain:}") private String cdnDomain;     
    @Value("${ncp.object-storage.kp-enabled:false}") private boolean kpEnabled;

    // ====== S3 Client (필요 시 재사용) ======
    private AmazonS3 buildClient() {
        return AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(ENDPOINT, REGION))
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
            .withPathStyleAccessEnabled(true)   // NCP 권장
            .disableChunkedEncoding()           // 호환성 향상
            .build();
    }

    // ====== Multipart 업로드 (스프링) ======
    public String uploadMultipart(MultipartFile file, String dirName) {
        String safeName = sanitize(file.getOriginalFilename());
        String key = dir(dirName) + UUID.randomUUID() + "_" + safeName;

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        String ct = file.getContentType();
        meta.setContentType(ct != null ? ct : guessContentType(safeName));
        meta.setCacheControl("public, max-age=31536000, immutable");

        try (InputStream in = file.getInputStream()) {
            PutObjectRequest req = new PutObjectRequest(bucket, key, in, meta);
            buildClient().putObject(req);
            return toPublicUrl(key);
        } catch (IOException e) {
            throw new RuntimeException("NCP 업로드 실패(입력 스트림): " + key, e);
        } catch (SdkClientException e) {
            throw new RuntimeException("NCP 업로드 실패: " + e.getMessage(), e);
        }
    }

    // ====== 삭제 ======
    public boolean deleteByCdnUrl(String url) {
        if (url == null || url.isBlank()) return false;
        if (cdnDomain == null || cdnDomain.isBlank()) return false;

        try {
            URI u = URI.create(url);
            String host = u.getHost();
            if (host == null || !host.equalsIgnoreCase(cdnDomain)) {
                // CDN 도메인이 아니면 스킵
                log.info("[NCP] skip deleteByCdnUrl: host mismatch url={} host={} cdn={}", url, host, cdnDomain);
                return false;
            }

            String path = u.getPath(); // "/community/abc.png"
            if (path == null || path.isBlank()) return false;

            // CDN 경로 == Object Storage key 라는 가정
            String keyEncoded = path.startsWith("/") ? path.substring(1) : path; // "community/abc.png"
            String key = URLDecoder.decode(keyEncoded, StandardCharsets.UTF_8);

            log.info("[NCP] deleteByCdnUrl -> key={}", key);
            delete(key); // 기존 delete(String key) 사용
            return true;

        } catch (Exception e) {
            log.warn("[NCP] deleteByCdnUrl failed url={}", url, e);
            return false;
        }
    }

    /** 실제 삭제: bucket + key */
    public void delete(String key) {
        try {
            buildClient().deleteObject(bucket, key);
        } catch (SdkClientException e) {
            throw new RuntimeException("NCP 삭제 실패: " + e.getMessage(), e);
        }
    }

    // ====== 공개 URL 생성 (CDN 우선) ======
    public String toPublicUrl(String key) {
        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8)
            .replace("+", "%20").replace("%2F", "/");
        if (cdnDomain != null && !cdnDomain.isBlank()) {
            return "https://" + cdnDomain + "/" + encodedKey;
        }
        return "https://" + NCP_ENDPOINT_HOST + "/" + bucket + "/" + encodedKey;
    }

    // ====== 유틸 ======
    private String dir(String d) { return (d == null || d.isBlank()) ? "" : (d.endsWith("/") ? d : d + "/"); }

    private String sanitize(String filename) {
        if (filename == null || filename.isBlank()) return "file";
        return filename.replaceAll("[\\\\/]+", "_");
    }

    private String guessContentType(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".svg"))  return "image/svg+xml";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".pdf"))  return "application/pdf";
        if (lower.endsWith(".txt"))  return "text/plain; charset=utf-8";
        return "application/octet-stream";
    }
}
