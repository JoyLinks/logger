/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger.access;

import com.joyzl.logger.LoggerBuilder;

/**
 * 访问日志读取
 * 
 * @author ZhangXi 2025年6月8日
 */
public class AccessWriter implements AccessCodes {

	/** 构造日志字符串 */
	public static void encode(StringBuilder builder, AccessRecord record) {
		builder.append(record.getRequestTimestamp());
		builder.append(SPACE);

		LoggerBuilder.encodeTime(builder, record.getRequestTimestamp());
		builder.append(SPACE);

		builder.append(record.getServerPort());
		builder.append(SPACE);

		if (record.getHost() != null) {
			builder.append(record.getHost());
		} else {
			builder.append(MINUS);
		}
		builder.append(SPACE);

		if (record.getRemoteAddress() != null) {
			builder.append(record.getRemoteAddress().getHostString());
		} else {
			builder.append(MINUS);
		}
		builder.append(SPACE);

		builder.append(record.getRequestMethod());
		builder.append(SPACE);

		builder.append(record.getRequestURI());
		builder.append(SPACE);

		builder.append(record.getRequestVersion());
		builder.append(SPACE);

		builder.append(record.getRequestBodySize());
		builder.append(SPACE);

		if (record.getServletName() != null) {
			builder.append(record.getServletName());
		} else {
			builder.append(MINUS);
		}
		builder.append(SPACE);

		builder.append(record.getServletSpend());
		builder.append(SPACE);

		builder.append(record.getResponseStatus());
		builder.append(SPACE);

		builder.append(record.getResponseBodySize());
		builder.append(LINE);
	}
}