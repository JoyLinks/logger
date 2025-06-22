/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger.test;

import java.net.InetSocketAddress;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.joyzl.logger.access.AccessLogger;
import com.joyzl.logger.access.AccessRecord;
import com.joyzl.logger.access.AccessRecordDefault;

class TestAccessLogger {

	AccessRecord record() {
		final AccessRecordDefault record = new AccessRecordDefault();
		record.setServerPort(80);
		record.setRemoteAddress(InetSocketAddress.createUnresolved("192.168.0.1", 0));
		record.setRequestTimestamp(System.currentTimeMillis());
		record.setRequestMethod("GET");
		record.setRequestURI("/web");
		record.setRequestVersion("HTTP/1.1");
		record.setRequestBodySize(0);
		record.setServletName("TEST(中华人民共和国)");
		record.setServletSpend(0);
		record.setResponseStatus(200);
		record.setResponseBodySize(0);
		return record;
	}

	@Test
	void test() throws Exception {
		final AccessLogger logger = new AccessLogger("access.log");

		for (int i = 0; i < 100; i++) {
			logger.record(record());
		}

		List<AccessRecord> records = logger.search(null, null);
		System.out.println(records.size());

		logger.close();
	}
}
