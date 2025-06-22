/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger.access;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

import com.joyzl.logger.LoggerBuilder;
import com.joyzl.logger.RotateFile;

/**
 * 访问日志读取
 * 
 * @author ZhangXi 2025年6月8日
 */
public class AccessReader implements AccessCodes {

	private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

	public void read(File file, Collection<AccessRecord> records) throws IOException {
		read(file.toPath(), records, Long.MIN_VALUE, Long.MAX_VALUE);
	}

	public void read(RotateFile file, Collection<AccessRecord> records) throws IOException {
		read(file.path(), records, file.begin(), file.end());
	}

	public void read(Path file, Collection<AccessRecord> records, long begin, long end) throws IOException {
		AccessRecordDefault record = null;
		final LoggerBuilder builder = LoggerBuilder.instance();
		try (final FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
			/*-
			 * 1749459420945 16:57:0.945 80 www.joyzl.com 192.168.0.1 GET /web HTTP/1.1 0 TEST(Servlet) 0 200 0
			 */
			char c;
			int start = 0, i = 0, field = 0;
			long timestamp;

			while (channel.read(buffer) > 0) {
				if (builder.decodeUTF8(buffer.flip())) {
					buffer.clear();
				} else {
					buffer.compact();
				}

				while (i < builder.length()) {
					c = builder.builder().charAt(i);
					if (c == SPACE) {
						if (i > start) {
							if (field == 0) {
								timestamp = parseLong(builder.builder(), start, i);
								if (timestamp >= begin && timestamp <= end) {
									record = new AccessRecordDefault();
									record.setRequestTimestamp(timestamp);
								}
							} else if (record != null) {
								if (field == 1) {
									// 忽略
								} else if (field == 2) {
									record.setServerPort(parseInt(builder.builder(), start, i));
								} else if (field == 3) {
									record.setHost(builder.builder().substring(start, i));
								} else if (field == 4) {
									record.setRemoteAddress(null);
								} else if (field == 5) {
									record.setRequestMethod(builder.builder().substring(start, i));
								} else if (field == 6) {
									record.setRequestURI(builder.builder().substring(start, i));
								} else if (field == 7) {
									record.setRequestVersion(builder.builder().substring(start, i));
								} else if (field == 8) {
									record.setRequestBodySize(parseInt(builder.builder(), start, i));
								} else if (field == 9) {
									record.setServletName(builder.builder().substring(start, i));
								} else if (field == 10) {
									record.setServletSpend(parseInt(builder.builder(), start, i));
								} else if (field == 10) {
									record.setResponseStatus(parseInt(builder.builder(), start, i));
								} else if (field == 10) {
									record.setResponseBodySize(parseInt(builder.builder(), start, i));
								}
							}
							field++;
						}
						start = i + 1;
					} else //
					if (c == LINE) {
						if (i > start) {
							record.setResponseBodySize(parseInt(builder.builder(), start, i));
						}
						if (record != null) {
							records.add(record);
							record = null;
						}
						start = i + 1;
						field = 0;
					}
					i++;
				}
				// 修正剩余字符
				builder.builder().delete(0, start);
				start = 0;
				i = 0;
			}

			// 修正尾部换行丢失
			if (record != null) {
				record.setResponseBodySize(parseInt(builder.builder(), 0, builder.length()));
				records.add(record);
				record = null;
			}
		} finally {
			builder.release();
		}
	}

	private long parseLong(CharSequence cs, int b, int e) {
		try {
			return Long.parseUnsignedLong(cs, b, e, 10);
		} catch (Exception x) {
			return 0;
		}
	}

	private int parseInt(CharSequence cs, int b, int e) {
		try {
			return Integer.parseUnsignedInt(cs, b, e, 10);
		} catch (Exception x) {
			return 0;
		}
	}
}