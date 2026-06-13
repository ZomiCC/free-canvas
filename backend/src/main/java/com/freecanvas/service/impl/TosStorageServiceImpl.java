package com.freecanvas.service.impl;

import com.freecanvas.service.CloudStorageService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Base64;

/**
 * 火山引擎 TOS (Tinder Object Storage) 实现
 * 基于 AWS S3 SDK v2，TOS 完全兼容 S3 协议
 */
@Service
public class TosStorageServiceImpl implements CloudStorageService {

    private static final Logger log = LoggerFactory.getLogger(TosStorageServiceImpl.class);

    @Value("${tos.access-key-id}")
    private String accessKeyId;

    @Value("${tos.secret-access-key}")
    private String secretAccessKey;

    @Value("${tos.region}")
    private String region;

    @Value("${tos.bucket}")
    private String bucket;

    @Value("${tos.endpoint-url}")
    private String endpointUrl;

    @Value("${tos.public-base-url}")
    private String publicBaseUrl;

    @Value("${tos.presign-expires}")
    private long presignExpires;

    private S3Client s3Client;
    private S3Presigner presigner;

    @PostConstruct
    public void init() {
        // TOS 的 secret access key 是 base64 编码的，需要解码
        String decodedSecret = decodeSecret(secretAccessKey);

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, decodedSecret);

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpointUrl))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .forcePathStyle(true)  // TOS 使用 path-style
                .build();

        this.presigner = S3Presigner.builder()
                .endpointOverride(URI.create(endpointUrl))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        log.info("TOS S3Client 初始化完成: bucket={}, region={}", bucket, region);
    }

    @PreDestroy
    public void destroy() {
        if (s3Client != null) s3Client.close();
        if (presigner != null) presigner.close();
    }

    @Override
    public String upload(String objectKey, InputStream inputStream, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            byte[] bytes = inputStream.readAllBytes();
            s3Client.putObject(request, RequestBody.fromBytes(bytes));

            String publicUrl = getPublicUrl(objectKey);
            log.info("文件上传成功: {} -> {} ({} bytes)", objectKey, publicUrl, bytes.length);
            return publicUrl;

        } catch (Exception e) {
            log.error("上传文件失败: {}", objectKey, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPresignedUrl(String objectKey) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(presignExpires))
                    .getObjectRequest(req -> req.bucket(bucket).key(objectKey))
                    .build();

            return presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("生成预签名 URL 失败: {}", objectKey, e);
            return getPublicUrl(objectKey);
        }
    }

    /**
     * 生成上传预签名 URL（前端直传用）
     */
    public String getPresignedUploadUrl(String objectKey, String contentType) {
        try {
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))
                    .putObjectRequest(req -> req.bucket(bucket).key(objectKey).contentType(contentType))
                    .build();

            return presigner.presignPutObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("生成上传预签名 URL 失败: {}", objectKey, e);
            throw new RuntimeException("预签名 URL 生成失败", e);
        }
    }

    @Override
    public String getPublicUrl(String objectKey) {
        return publicBaseUrl + "/" + objectKey;
    }

    @Override
    public void delete(String objectKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(request);
            log.info("文件已删除: {}", objectKey);
        } catch (Exception e) {
            log.error("删除文件失败: {}", objectKey, e);
        }
    }

    /**
     * 检查对象是否存在
     */
    public boolean exists(String objectKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    /**
     * 解码 TOS 的 Base64 编码 Secret Key
     */
    private String decodeSecret(String encoded) {
        try {
            return new String(Base64.getDecoder().decode(encoded));
        } catch (IllegalArgumentException e) {
            // 如果解码失败，可能已经是原始格式
            log.warn("Secret key 不是 base64 格式，使用原始值");
            return encoded;
        }
    }
}
