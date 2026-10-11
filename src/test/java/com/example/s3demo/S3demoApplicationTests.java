package com.example.s3demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "aws.s3.bucket=test-bucket")
class S3demoApplicationTests {

	@Test
	void contextLoads() {
	}

}
