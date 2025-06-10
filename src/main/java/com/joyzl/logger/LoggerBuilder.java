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
	 * 编码字符串为字节串
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
			code = builder.codePointAt(index);
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

	@Override
	public String toString() {
		return builder.toString();
	}
}