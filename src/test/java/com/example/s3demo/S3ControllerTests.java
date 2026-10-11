package com.example.s3demo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class S3ControllerTests {
    private final S3Client s3 = mock(S3Client.class);
    private final S3Controller controller = new S3Controller(s3, "test-bucket");

    @Test
    void uploadsFileToConfiguredBucket() throws Exception {
        doAnswer(invocation -> {
            Consumer<PutObjectRequest.Builder> configure = invocation.getArgument(0);
            var builder = PutObjectRequest.builder();
            configure.accept(builder);
            assertEquals("test-bucket", builder.build().bucket());
            assertEquals("demo/hello.txt", builder.build().key());
            RequestBody body = invocation.getArgument(1);
            try (var input = body.contentStreamProvider().newStream()) {
                assertEquals("Xin chào AWS", new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            }
            return null;
        }).when(s3).putObject(org.mockito.ArgumentMatchers.<Consumer<PutObjectRequest.Builder>>any(), any(RequestBody.class));

        var file = new MockMultipartFile("file", "hello.txt", "text/plain",
                "Xin chào AWS".getBytes(StandardCharsets.UTF_8));
        assertEquals("Uploaded: demo/hello.txt", controller.upload("demo/hello.txt", file));
        verify(s3).putObject(org.mockito.ArgumentMatchers.<Consumer<PutObjectRequest.Builder>>any(), any(RequestBody.class));
    }

    @Test
    void downloadsFileFromConfiguredBucket() throws Exception {
        when(s3.getObject(org.mockito.ArgumentMatchers.<Consumer<GetObjectRequest.Builder>>any())).thenAnswer(invocation -> {
            Consumer<GetObjectRequest.Builder> configure = invocation.getArgument(0);
            var builder = GetObjectRequest.builder();
            configure.accept(builder);
            assertEquals("test-bucket", builder.build().bucket());
            assertEquals("demo/hello.txt", builder.build().key());
            byte[] content = "Xin chào AWS".getBytes(StandardCharsets.UTF_8);
            return new ResponseInputStream<>(GetObjectResponse.builder()
                    .contentType("text/plain").contentLength((long) content.length).build(),
                    new ByteArrayInputStream(content));
        });

        var response = controller.download("demo/hello.txt");
        var output = new ByteArrayOutputStream();
        assertNotNull(response.getBody());
        response.getBody().writeTo(output);
        assertEquals("Xin chào AWS", output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void rejectsBlankKeysWithoutCallingAws() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> controller.upload(" ", new MockMultipartFile("file", new byte[] {1}))).getStatusCode().value());
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> controller.download(" ")).getStatusCode().value());
        verifyNoInteractions(s3);
    }
}
