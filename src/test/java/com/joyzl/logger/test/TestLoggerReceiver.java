/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
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