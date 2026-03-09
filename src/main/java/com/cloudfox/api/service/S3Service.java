package com.cloudfox.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final String BUCKET = "cloudfox-model-artifacts";

    private final S3Client s3Client;

    public void uploadFile(String key, String contentType, long size, InputStream stream) {

        try {

            createBucketIfMissing(BUCKET);

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(BUCKET)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(stream, size)
            );

        } catch (S3Exception e) {
            throw new RuntimeException("Failed S3 operation", e);
        }
    }

    private void createBucketIfMissing(String bucket) {

        try {

            s3Client.headBucket(
                    HeadBucketRequest.builder()
                            .bucket(bucket)
                            .build()
            );

        } catch (NoSuchBucketException e) {

            s3Client.createBucket(
                    CreateBucketRequest.builder()
                            .bucket(bucket)
                            .build()
            );

        } catch (S3Exception e) {

            throw new RuntimeException("Failed S3 operation", e);
        }
    }
}