package com.lucaslopez.LedgerX;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.lucaslopez.LedgerX.auditoria.domain.LogActivityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ActiveProfiles("test")
class LedgerXApplicationTests {

	@MockBean
	private LogActivityRepository logActivityRepository;

	@Test
	void contextLoads() {
	}

}
