package com.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.app.config.TestMailConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class AdminClubApplicationTests {

	@Test
	void contextLoads() {
	}

}
