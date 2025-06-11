package com.joyzl.logger.test;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.joyzl.logger.Logger;
import com.joyzl.logger.LoggerCleaner;
import com.joyzl.logger.LoggerService;

class TestLogger {

	@Test
	void test() throws IOException {

		Logger.setFile("", null, null);
		Logger.info("没有扩展名");

		Logger.setFile(null, null, null);
		Logger.info("没有文件");

		Logger.setUDP("LocalHost", 8210);
		Logger.info("本地网络");

		Logger.setUDP(null, 0);
		Logger.info("网络关闭");

		Logger.setFile("", null, ".log");
		Logger.info("没有文件名");

		Logger.setFile("log", null, ".log");
		LoggerService.setExpires(0);
		LoggerCleaner c = LoggerService.clean();
		System.out.print(c);
	}

}