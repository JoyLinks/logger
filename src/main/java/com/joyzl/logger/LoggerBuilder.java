/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 具有时间戳的StringBuilder对象，用于构造日志字符串
 * 
 * @author ZhangXi 2025年6月8日
 */
public class LoggerBuilder {

	final static int OFFSET_SECONDS = ZoneId.systemDefault().getRules().getStandardOffset(Instant.EPOCH).getTotalSeconds();
	final static int DAY_MILLISECOND = 24 * 60 * 60 * 1000;
	final static int HOUR_MILLISECOND = 60 * 60 * 1000;
	final static int MINUTES_MILLISECOND = 60 * 1000;
	final static int SECONDS_MILLISECOND = 1000;

	final static char COLON = ':';
	final static char SPACE = ' ';
	final static char POINT = '.';

	private final static ConcurrentLinkedQueue<LoggerBuilder> BUILDERS = new ConcurrentLinkedQueue<>();
	static {
		for (int i = 0; i < 64; i++) {
			BUILDERS.offer(new LoggerBuilder());
		}
	}

	public static LoggerBuilder instance() {
		LoggerBuilder builder = BUILDERS.poll();
		if (builder == null) {
			builder = new LoggerBuilder();
		}
		return builder;
	}

	/** 10:25:20.998 */
	public static void encodeTime(StringBuilder builder, long timestamp) {
		timestamp = timestamp % DAY_MILLISECOND;
		timestamp += OFFSET_SECONDS * 1000;

		builder.append(timestamp / HOUR_MILLISECOND);
		builder.append(COLON);
		builder.append((timestamp / MINUTES_MILLISECOND) % 60);
		builder.append(COLON);
		builder.append((timestamp / SECONDS_MILLISECOND) % 60);
		builder.append(POINT);
		builder.append(timestamp % SECONDS_MILLISECOND);
	}

	////////////////////////////////////////////////////////////////////////////////

	private final StringBuilder builder = new StringBuilder(512);
	public long timestamp;
	private int index;

	public int length() {
		return builder.length();
	}

	public StringBuilder builder() {
		return builder;
	}

	public void release() {
		index = 0;
		timestamp = 0;
		builder.setLength(0);
		BUILDERS.offer(this);
	}

	/**
	 * 编码字符串为UTF8字节串
	 * 
	 * @param output 输出字节串的缓存
	 * @return true 字符串已全部输出
	 */
	public boolean encodeUTF8(ByteBuffer output) {
		// U+0000 ~ U+007F
		// 0xxxxxxx (1Byte)

		// U+0080 ~ U+07FF
		// 110xxxxx 10xxxxxx (2Byte)

		// U+0800 ~ U+FFFF
		// 1110xxxx 10xxxxxx 10xxxxxx (3Byte)

		// U+10000 ~ U+10FFFF
		// 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx (4Byte)

		int code;
		for (; index < builder.length(); index++) {
			code = builder.charAt(index);

			// 处理高代理项(Surrogate High)
			if (code >= 0xD800 && code <= 0xDBFF) {
				// 检查是否存在低代理项(Surrogate Low)
				if (index + 1 < builder.length()) {
					char low = builder.charAt(index += 1);
					if (low >= 0xDC00 && low <= 0xDFFF) {
						// 组合为完整码点（U+10000 到 U+10FFFF）
						int codePoint = ((code - 0xD800) << 10) + (low - 0xDC00) + 0x10000;
						// 编码 4 字节 UTF-8（U+10000 到 U+10FFFF）
						if (output.remaining() >= 4) {
							output.put((byte) (0xF0 | (codePoint >> 18)));
							output.put((byte) (0x80 | ((codePoint >> 12) & 0x3F)));
							output.put((byte) (0x80 | ((codePoint >> 6) & 0x3F)));
							output.put((byte) (0x80 | (codePoint & 0x3F)));
							continue;
						} else {
							return false;
						}
					}
				} else {
					// 不完整的代理对或非法代理项
					// 按普通字符处理（可能导致乱码）
				}
			}

			if (code <= 0x7F) {
				if (output.remaining() > 0) {
					output.put((byte) code);
				} else {
					return false;
				}
			} else if (code <= 0x7FF) {
				if (output.remaining() > 1) {
					output.put((byte) (0xC0 | (code >> 6)));
					output.put((byte) (0x80 | (code & 0x3F)));
				} else {
					return false;
				}
			} else if (code <= 0xFFFF) {
				if (output.remaining() > 2) {
					output.put((byte) (0xE0 | (code >> 12)));
					output.put((byte) (0x80 | ((code >> 6) & 0x3F)));
					output.put((byte) (0x80 | (code & 0x3F)));
				} else {
					return false;
				}
			} else if (code <= 0x10FFFF) {
				if (output.remaining() > 3) {
					output.put((byte) (0xF0 | (code >> 18)));
					output.put((byte) (0x80 | ((code >> 12) & 0x3F)));
					output.put((byte) (0x80 | ((code >> 6) & 0x3F)));
					output.put((byte) (0x80 | (code & 0x3F)));
				} else {
					return false;
				}
			} else {
				continue;
			}
		}
		return true;
	}

	/**
	 * 解码UTF8字节串为字符串
	 * 
	 * @return false 需要更多输入字节
	 */
	public boolean decodeUTF8(ByteBuffer buffer) {
		// U+0000 ~ U+007F
		// 0xxxxxxx (1Byte)

		// U+0080 ~ U+07FF
		// 110xxxxx 10xxxxxx (2Byte)

		// U+0800 ~ U+FFFF
		// 1110xxxx 10xxxxxx 10xxxxxx (3Byte)

		// U+10000 ~ U+10FFFF
		// 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx (4Byte)

		int code;
		while (buffer.hasRemaining()) {
			code = buffer.get();
			if ((code & 0x80) == 0) {
				builder.append((char) code);
			} else if ((code & 0xE0) == 0xC0) {
				if (buffer.remaining() > 0) {
					code = (code & 0x1F) << 6;
					code |= (buffer.get() & 0x3F);
					builder.append((char) code);
				} else {
					break;
				}
			} else if ((code & 0xF0) == 0xE0) {
				if (buffer.remaining() > 1) {
					code = (code & 0x0F) << 12;
					code |= (buffer.get() & 0x3F) << 6;
					code |= (buffer.get() & 0x3F);
					builder.append((char) code);
				} else {
					break;
				}
			} else if ((code & 0xF8) == 0xF0) {
				if (buffer.remaining() > 2) {
					code = (code & 0x07) << 18;
					code |= (buffer.get() & 0x3F) << 12;
					code |= (buffer.get() & 0x3F) << 6;
					code |= (buffer.get() & 0x3F);
					if (code >= 0x010000 && code <= 0x10FFFF) {
						// 处理代理对
						int high = ((code - 0x010000) >> 10) + 0xD800;
						int low = ((code - 0x010000) & 0x3FF) + 0xDC00;
						builder.append((char) high);
						builder.append((char) low);
					} else {
						// 无效代理对
						builder.append('\uFFFD');
					}
				} else {
					break;
				}
			} else {
				// 非法字节
				builder.append('\uFFFD');
			}
		}
		if (buffer.hasRemaining()) {
			// 字节不足，回退1字节
			buffer.position(buffer.position() - 1);
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return builder.toString();
	}
}