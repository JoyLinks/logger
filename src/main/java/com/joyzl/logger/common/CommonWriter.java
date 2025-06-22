/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger.common;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Collection;

/**
 * Common Log Format (CLF)
 * 
 * <pre>
 * RFC6872: The Common Log Format (CLF) for the Session Initiation Protocol (SIP):Framework and Information Model
 * RFC6873: Format for the Session Initiation Protocol (SIP) Common Log Format (CLF)
 * </pre>
 * 
 * @author ZhangXi 2024年12月2日
 */
public class CommonWriter implements CommonCodes {

	/**
	 * 编码CLF记录，由三部分组成：[Index Pointers | Mandatory Fields | Optional
	 * Fields]，所有字段最长不超过4096
	 */
	public static void encode(CommonRecord record, ByteBuffer buffer) {
		// Index Pointers

		// VERSION 1Byte 'A'
		buffer.put(VERSION);
		// LENGTH 6Byte HEX
		buffer.position(7);
		// 0x2C ','
		buffer.put(COMMA);
		// POINTERS 8~55 56~59
		buffer.position(60);

		// 0x0A '\n'
		buffer.put(LF);
		// Timestamp 14Byte "seconds.millis"
		final long timestamp = record.getTimestamp();
		putTimestamp(buffer, timestamp);
		// 0x09 '\t'
		buffer.put(TAB);
		// Flags 5Byte
		buffer.put((byte) record.getType());
		buffer.put((byte) record.getRetransmission());
		buffer.put((byte) record.getDirection());
		buffer.put((byte) record.getTransport());
		buffer.put((byte) record.getEncryption());
		buffer.put(TAB);

		// Mandatory Fields (variable length)

		setPointer(buffer, 8);
		putMandatory(buffer, record.getCSeqNumber());
		buffer.put(SPACE);
		putMandatory(buffer, record.getCSeqMethod());
		buffer.put(TAB);
		setPointer(buffer, 12);
		putMandatory(buffer, record.getStatus());
		buffer.put(TAB);
		setPointer(buffer, 16);
		putMandatory(buffer, record.getRURI());
		buffer.put(TAB);
		setPointer(buffer, 20);
		putMandatory(buffer, record.getDestination());
		buffer.put(TAB);
		setPointer(buffer, 24);
		putMandatory(buffer, record.getSource());
		buffer.put(TAB);
		setPointer(buffer, 28);
		putMandatory(buffer, record.getTo());
		buffer.put(TAB);
		setPointer(buffer, 32);
		putMandatory(buffer, record.getToTag());
		buffer.put(TAB);
		setPointer(buffer, 36);
		putMandatory(buffer, record.getFrom());
		buffer.put(TAB);
		setPointer(buffer, 40);
		putMandatory(buffer, record.getFromTag());
		buffer.put(TAB);
		setPointer(buffer, 44);
		putMandatory(buffer, record.getCallId());
		buffer.put(TAB);
		setPointer(buffer, 48);
		putMandatory(buffer, record.getServerTxn());
		buffer.put(TAB);
		setPointer(buffer, 52);
		putMandatory(buffer, record.getClientTxn());

		// Optional Fields

		setPointer(buffer, 56);
		final Collection<OptionalField> optionalFields = record.getOptionalFields();
		if (optionalFields != null && optionalFields.size() > 0) {
			// Tag@Vendor,Length,BEB,Value
			for (OptionalField optionalField : optionalFields) {
				if (optionalField != null && optionalField.getValue() != null) {
					buffer.put(TAB);
					// TAG 2Byte
					putTag(buffer, optionalField.getTag());
					buffer.put(AT);
					// Vendor 8Byte
					putVendor(buffer, optionalField.getVendor());
					buffer.put(COMMA);
					// Length 4Byte HEX
					int position = buffer.position();
					buffer.position(buffer.position() + 4);
					buffer.put(COMMA);
					// BEB 1Byte
					if (optionalField.isCharSequence()) {
						buffer.put((byte) '0');
						buffer.put((byte) '0');
					} else {
						buffer.put((byte) '0');
						buffer.put((byte) '1');
					}
					buffer.put(COMMA);
					// Value
					if (optionalField.getTag() == OptionalField.TAG_00) {
						// name: value
						putOptionalName(buffer, optionalField.getName());
						buffer.put(COLON);
						buffer.put(SPACE);
						if (optionalField.getValue() instanceof CharSequence) {
							putOptionalValue(buffer, (CharSequence) optionalField.getValue());
						}
					} else if (optionalField.getTag() == OptionalField.TAG_01) {
						// type body
						putOptionalName(buffer, optionalField.getName());
						buffer.put(SPACE);
						if (optionalField.getValue() instanceof CharSequence) {
							putOptionalValue(buffer, (CharSequence) optionalField.getValue());
						} else if (optionalField.getValue() instanceof ByteBuffer) {
							putOptionalBase64(buffer, (ByteBuffer) optionalField.getValue());
						}
					} else if (optionalField.getTag() == OptionalField.TAG_02) {
						// data
						if (optionalField.getValue() instanceof CharSequence) {
							putOptionalValue(buffer, (CharSequence) optionalField.getValue());
						} else if (optionalField.getValue() instanceof ByteBuffer) {
							putOptionalBase64(buffer, (ByteBuffer) optionalField.getValue());
						}
					} else {
						// data
						if (optionalField.getValue() instanceof CharSequence) {
							putOptionalValue(buffer, (CharSequence) optionalField.getValue());
						} else if (optionalField.getValue() instanceof ByteBuffer) {
							putOptionalBase64(buffer, (ByteBuffer) optionalField.getValue());
						}
					}
					setOptionalLength(buffer, position, buffer.position() - position - 8);
				}
			}
		}

		// 0x0A '\n'
		buffer.put(LF);
		// 设置长度 6Byte
		setTotalLength(buffer, buffer.position());
	}

	/**
	 * 设置位置指针（固定4字节，十六进制字符）
	 */
	static void setPointer(ByteBuffer bytes, int index) {
		int pointer = bytes.position() + 1;
		bytes.put(index++, digit((int) (pointer / 0x1000), 16));
		pointer = pointer % 0x1000;
		bytes.put(index++, digit((int) (pointer / 0x100), 16));
		pointer = pointer % 0x100;
		bytes.put(index++, digit((int) (pointer / 0x10), 16));
		pointer = pointer % 0x10;
		bytes.put(index, digit((int) pointer, 16));
	}

	/**
	 * 设置长度（固定6字节，十六进制字符）
	 */
	static void setTotalLength(ByteBuffer bytes, int length) {
		bytes.put(1, digit((int) (length / 0x100000), 16));
		length = length % 0x100000;
		bytes.put(2, digit((int) (length / 0x10000), 16));
		length = length % 0x10000;
		bytes.put(3, digit((int) (length / 0x1000), 16));
		length = length % 0x1000;
		bytes.put(4, digit((int) (length / 0x100), 16));
		length = length % 0x100;
		bytes.put(5, digit((int) (length / 0x10), 16));
		length = length % 0x10;
		bytes.put(6, digit((int) length, 16));
	}

	/**
	 * 设置长度（固定4字节，十六进制字符）
	 */
	static void setOptionalLength(ByteBuffer bytes, int index, int length) {
		length = length % 0x10000;
		bytes.put(index++, digit((int) (length / 0x1000), 16));
		length = length % 0x1000;
		bytes.put(index++, digit((int) (length / 0x100), 16));
		length = length % 0x100;
		bytes.put(index++, digit((int) (length / 0x10), 16));
		length = length % 0x10;
		bytes.put(index, digit((int) length, 16));
	}

	/**
	 * 10进制时间戳 (固定14字节，十进制字符，"seconds(10).millis(3)")
	 */
	static void putTimestamp(ByteBuffer bytes, long timestamp) {
		int millis = (int) (timestamp % 1000);
		long seconds = timestamp / 1000;
		// Seconds
		bytes.put(digit((int) (seconds / 1000000000), 10));
		seconds = seconds % 1000000000;
		bytes.put(digit((int) (seconds / 100000000), 10));
		seconds = seconds % 100000000;
		bytes.put(digit((int) (seconds / 10000000), 10));
		seconds = seconds % 10000000;
		bytes.put(digit((int) (seconds / 1000000), 10));
		seconds = seconds % 1000000;
		bytes.put(digit((int) (seconds / 100000), 10));
		seconds = seconds % 100000;
		bytes.put(digit((int) (seconds / 10000), 10));
		seconds = seconds % 10000;
		bytes.put(digit((int) (seconds / 1000), 10));
		seconds = seconds % 1000;
		bytes.put(digit((int) (seconds / 100), 10));
		seconds = seconds % 100;
		bytes.put(digit((int) (seconds / 10), 10));
		seconds = seconds % 10;
		bytes.put(digit((int) seconds, 10));
		// 0x2E '.'
		bytes.put(DOT);
		// Millisecond 3Byte
		bytes.put(digit(millis / 100, 10));
		millis = millis % 100;
		bytes.put(digit(millis / 10, 10));
		millis = millis % 10;
		bytes.put(digit(millis, 10));
	}

	/**
	 * 10进制标记（固定2字节，十进制字符）
	 */
	static void putTag(ByteBuffer bytes, int tag) {
		tag = tag % 100;
		bytes.put(digit(tag / 10, 10));
		tag = tag % 10;
		bytes.put(digit(tag, 10));
	}

	/**
	 * 10进制商标（固定8字节，十进制字符）
	 */
	static void putVendor(ByteBuffer bytes, int vender) {
		vender = vender % 100000000;
		bytes.put(digit((int) (vender / 10000000), 10));
		vender = vender % 10000000;
		bytes.put(digit((int) (vender / 1000000), 10));
		vender = vender % 1000000;
		bytes.put(digit((int) (vender / 100000), 10));
		vender = vender % 100000;
		bytes.put(digit((int) (vender / 10000), 10));
		vender = vender % 10000;
		bytes.put(digit((int) (vender / 1000), 10));
		vender = vender % 1000;
		bytes.put(digit((int) (vender / 100), 10));
		vender = vender % 100;
		bytes.put(digit((int) (vender / 10), 10));
		vender = vender % 10;
		bytes.put(digit((int) vender, 10));
	}

	/**
	 * 10进制无符号整数字符串
	 */
	static void putMandatory(ByteBuffer bytes, int value) {
		if (value > 0) {
			int decs = 1;
			while (value / decs > 10) {
				decs *= 10;
			}
			while (decs > 0) {
				bytes.put(digit(value / decs, 10));
				value %= decs;
				decs /= 10;
			}
		} else {
			bytes.put(MINUS);
		}
	}

	/**
	 * 10进制无符号整数字符串
	 */
	static void putMandatory(ByteBuffer bytes, long value) {
		if (value > 0) {
			long decs = 1;
			while (value / decs > 10) {
				decs *= 10;
			}
			while (decs > 0) {
				bytes.put(digit((int) (value / decs), 10));
				value %= decs;
				decs /= 10;
			}
		} else {
			bytes.put(MINUS);
		}
	}

	/**
	 * 必要字段字符串，执行转义，UTF-8
	 */
	static void putMandatory(ByteBuffer bytes, CharSequence value) {
		if (value == null) {
			// 失败标记为'?'
			bytes.put(QUESTION);
		} else if (value.length() == 0) {
			// 空值则为'-'
			bytes.put(MINUS);
		} else {
			char c;
			if (value.length() == 1) {
				c = value.charAt(0);
				if (c == TAB) {
					// '\t'转义空格
					bytes.put(SPACE);
				} else if (c == MINUS) {
					// 值为单个'-'转义为"%2D"
					bytes.put((byte) '%');
					bytes.put((byte) '2');
					bytes.put((byte) 'D');
				} else if (c == QUESTION) {
					// 值为单个'?'转义为"%3F"
					bytes.put((byte) '%');
					bytes.put((byte) '3');
					bytes.put((byte) 'F');
				} else {
					putUTF8(bytes, c);
				}
			} else {
				for (int index = 0; index < value.length(); index++) {
					c = value.charAt(index);
					if (c == TAB) {
						// '\t'转义空格
						bytes.put(SPACE);
					} else if (c == CR || c == LF) {
						// 忽略回车换行[CRLF]
					} else {
						putUTF8(bytes, c);
					}
				}
			}
		}
	}

	/**
	 * 可选字段名称，执行转义，ASCII
	 */
	static void putOptionalName(ByteBuffer bytes, CharSequence value) {
		char c;
		for (int index = 0; index < value.length(); index++) {
			c = value.charAt(index);
			if (c == TAB) {
				// '\t'转义空格
				bytes.put(SPACE);
			} else if (c == CR || c == LF) {
				// 忽略回车换行[CRLF]
			} else {
				bytes.put((byte) c);
			}
		}
	}

	/**
	 * 可选字段字符串值，执行转义，UTF-8
	 */
	static void putOptionalValue(ByteBuffer bytes, CharSequence value) {
		int c;
		for (int index = 0; index < value.length(); index++) {
			c = value.charAt(index);
			if (c == TAB) {
				// '\t'转义空格
				bytes.put(SPACE);
			} else if (c == CR) {
				// [CR]转义%0D
				bytes.put((byte) '%');
				bytes.put((byte) '0');
				bytes.put((byte) 'D');
			} else if (c == LF) {
				// [LF]转义%0A
				bytes.put((byte) '%');
				bytes.put((byte) '0');
				bytes.put((byte) 'A');
			} else {
				putUTF8(bytes, c);
			}
		}
	}

	/**
	 * 可选字段字节值，Base64编码
	 */
	static void putOptionalBase64(ByteBuffer bytes, ByteBuffer data) {
		// 超长截断
		if (data.remaining() > 4096) {
			data.limit(data.limit() - (data.remaining() - 4096));
		}
		data.mark();
		// 每行76字符，结尾添加回车换行
		// 转义CRLF为%0D%A
		final ByteBuffer mimeData = Base64.getMimeEncoder().encode(data);
		// byte c;
		// while (mimeData.hasRemaining()) {
		// c = mimeData.get();
		// if (c == CR) {
		// bytes.put((byte) '%');
		// bytes.put((byte) '0');
		// bytes.put((byte) 'D');
		// } else if (c == LF) {
		// bytes.put((byte) '%');
		// bytes.put((byte) '0');
		// bytes.put((byte) 'A');
		// } else {
		// bytes.put(c);
		// }
		// }
		int limit;
		final byte[] CRLF = new byte[] { '%', '0', 'D', '%', '0', 'A' };
		while (mimeData.remaining() > 77) {
			limit = mimeData.limit();
			mimeData.limit(mimeData.position() + 76);
			bytes.put(mimeData);
			bytes.put(CRLF);
			mimeData.limit(limit);
			mimeData.position(mimeData.position() + 2);
		}
		bytes.put(mimeData);
		bytes.put(CRLF);
		data.reset();
	}

	/**
	 * 数值转换16进制字符（大写）
	 */
	static byte digit(int digit, int radix) {
		if ((digit >= radix) || (digit < 0)) {
			return '\0';
		}
		if ((radix < Character.MIN_RADIX) || (radix > Character.MAX_RADIX)) {
			return '\0';
		}
		if (digit < 10) {
			return (byte) ('0' + digit);
		}
		return (byte) ('A' - 10 + digit);
	}

	static boolean putUTF8(ByteBuffer output, int code) {
		// U+0000 ~ U+007F
		// 0xxxxxxx (1Byte)

		// U+0080 ~ U+07FF
		// 110xxxxx 10xxxxxx (2Byte)

		// U+0800 ~ U+FFFF
		// 1110xxxx 10xxxxxx 10xxxxxx (3Byte)

		// U+10000 ~ U+10FFFF
		// 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx (4Byte)

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
			//
		}
		return true;
	}
}