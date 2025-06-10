package com.joyzl.logger.test;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

import com.joyzl.logger.access.AccessLogger;
import com.joyzl.logger.access.AccessRecord;

class TestAccessLogger {

	AccessRecord record() {
		return new AccessRecord() {

			@Override
			public int serverPort() {
				return 80;
			}

			@Override
			public InetSocketAddress remoteAddress() {
				return InetSocketAddress.createUnresolved("192.168.0.1", 0);
			}

			@Override
			public long requestTimestamp() {
				return System.currentTimeMillis();
			}

			@Override
			public String requestMethod() {
				return "GET";
			}

			@Override
			public String requestURI() {
				return "/web";
			}

			@Override
			public String requestVersion() {
				return "HTTP/1.1";
			}

			@Override
			public int requestBodySize() {
				return 0;
			}

			@Override
			public String servletName() {
				return "TEST(中华人民共和国)";
			}

			@Override
			public int servletSpend() {
				return 0;
			}

			@Override
			public int responseStatus() {
				return 200;
			}

			@Override
			public int responseBodySize() {
				return 0;
			}
		};
	}

	@Test
	void test() throws Exception {
		final AccessLogger logger = new AccessLogger("access.log");

		for (int i = 0; i < 100; i++) {
			logger.record(record());
		}

		logger.close();
	}
}
