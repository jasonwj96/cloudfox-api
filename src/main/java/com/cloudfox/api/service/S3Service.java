package com.cloudfox.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

@Service
@RequiredArgsConstructor

public class S3Service {

    private static final String BUCKET = "cloudfox-model-artifacts";
    private final S3Client s3Client;

    public void saveFile(MultipartFile file, String key) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File payload is required");
        }

        try {
            createBucketIfMissing(BUCKET);

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(BUCKET)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload model", e);
        } catch (S3Exception e) {
            throw new RuntimeException("Failed S3 operation", e);
        }
    }

    private void createBucketIfMissing(String bucket) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        } catch (S3Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed S3 operation", e);
        }
    }
}