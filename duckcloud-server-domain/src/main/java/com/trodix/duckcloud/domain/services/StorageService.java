package com.trodix.duckcloud.domain.services;

import com.trodix.duckcloud.domain.models.FileStoreMetadata;
import io.minio.*;
import io.minio.messages.Bucket;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class StorageService {

    public static final String ROOT_BUCKET = "root";

    private final MinioClient minioClient;

    @PostConstruct
    private void init() throws Exception {
        final boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(ROOT_BUCKET).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(ROOT_BUCKET).build());
        } else {
            log.info("Bucket '{}' already exists.", ROOT_BUCKET);
        }
    }

    public List<Bucket> listBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public ObjectWriteResponse uploadFile(final FileStoreMetadata fileStoreMetadata, final byte[] content) {

        String bucket = fileStoreMetadata.getBucket() != null ? fileStoreMetadata.getBucket() : ROOT_BUCKET;
        String directoryPath = fileStoreMetadata.getDirectoryPath() != null ? fileStoreMetadata.getDirectoryPath() : generateDirectoryPath();
        String uuid = fileStoreMetadata.getUuid() != null ? fileStoreMetadata.getUuid() : UUID.randomUUID().toString();

        try {
            final PutObjectArgs obj = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(Path.of(directoryPath, uuid).toString())
                    .contentType(fileStoreMetadata.getContentType())
                    .stream(new ByteArrayInputStream(content), content.length, -1)
                    .build();
            final ObjectWriteResponse response = minioClient.putObject(obj);
            log.debug("Created new object in storage: {}", response.object());
            return response;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public byte[] getFile(final String bucket, final String path) {
        final GetObjectArgs args = GetObjectArgs.builder().bucket(bucket).object(path).build();
        try (final InputStream obj = minioClient.getObject(args)) {
            return IOUtils.toByteArray(obj);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteFile(final String path, final String name) {
        final RemoveObjectArgs args = RemoveObjectArgs.builder().bucket(ROOT_BUCKET).object(Path.of(path, name).toString()).build();
        try {
            minioClient.removeObject(args);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private String generateDirectoryPath() {
        Date date = Date.from(Instant.now());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.valueOf(calendar.get(Calendar.MONTH));
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

        return Path.of(year, month, day).toString();
    }

}
