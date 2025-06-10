package com.joyzl.logger.test;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.joyzl.logger.Logger;
import com.joyzl.logger.LoggerReceiver;

class TestLoggerReceiver {

	@Test
	void test() throws IOException, InterruptedException {
		Logger.setUDP("127.0.0.1", 1982);
		Logger.setConsole(false);

		final LoggerReceiver receiver = new LoggerReceiver(1982);

		Logger.info("开始");
		for (int i = 0; i < 100; i++) {
			Logger.debug(i);
		}
		Logger.info("结束");
		Thread.sleep(3000);
		receiver.close();
	}

}