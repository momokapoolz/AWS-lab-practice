package com.example.s3demo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@RestController
public class S3Controller {
    private final S3Client s3;
    private final String bucket;

    public S3Controller(S3Client s3, @Value("${aws.s3.bucket}") String bucket) {
        Assert.hasText(bucket, "Set S3_BUCKET to an existing bucket name");
        this.s3 = s3;
        this.bucket = bucket;
    }

    @PostMapping(value = "/s3", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "text/plain;charset=UTF-8")
    public String upload(@RequestParam("key") String key, @RequestParam("file") MultipartFile file) throws IOException {
        if (key.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "key must not be blank");
        }
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file must not be empty");
        }

        String contentType = file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        s3.putObject(r -> r.bucket(bucket).key(key).contentType(contentType), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return "Uploaded: " + key;
    }

    @GetMapping("/s3")
    public ResponseEntity<StreamingResponseBody> download(@RequestParam("key") String key) {
        if (key.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "key must not be blank");
        }

        ResponseInputStream<GetObjectResponse> in;
        try {
            in = s3.getObject(r -> r.bucket(bucket).key(key));
        } catch (NoSuchKeyException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "key not found: " + key);
        }

        GetObjectResponse meta = in.response();
        String filename = key.substring(key.lastIndexOf('/') + 1);

        StreamingResponseBody body = out -> {
            try (in) {
                in.transferTo(out);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        meta.contentType() != null ? meta.contentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .contentLength(meta.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .body(body);
    }
}