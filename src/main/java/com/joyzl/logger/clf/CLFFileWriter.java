package com.joyzl.logger.clf;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

/**
 * CLF Encode and Decode (UTF-8)
 * 
 * @author ZhangXi 2024年12月2日
 */
public class CLFFileWriter implements CLFCoder, Closeable {

	private final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
	private final ByteBuffer bytes = ByteBuffer.allocateDirect(65536);
	private final CharBuffer chars = CharBuffer.allocate(4096);
	private final ReentrantLock lock = new ReentrantLock();
	private final File file;

	private FileOutputStream output;
	private long begin, end;

	public CLFFileWriter(File file) {
		this.file = file;
		if (!file.exists()) {
			file = file.getParentFile();
			if (file != null && !file.exists()) {
				file.mkdirs();
			}
		}
	}

	/**
	 * 检查并切换应写入的文件
	 */
	private void check(final long timestamp) throws IOException {
		if (timestamp < begin || timestamp > end) {
			if (output != null) {
				output.flush();
				output.close();
				output = null;
			}
			// TODAY
			LocalDate date = LocalDate.now();
			begin = date.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC) * 1000;
			end = date.toEpochSecond(LocalTime.MAX, ZoneOffset.UTC) * 1000 + 999;
			if (timestamp >= begin && timestamp <= end) {
				if (file.exists() && file.length() > 0) {
					final Instant instant = Instant.ofEpochMilli(file.lastModified());
					final LocalDate last = LocalDate.ofInstant(instant, ZoneOffset.UTC);
					if (last.isEqual(date)) {
						// CONTINUE
					} else {
						if (file.renameTo(CLFCoder.dateFile(file, last))) {
							// OK
						} else {
							throw new IOException("文件重命名失败");
						}
					}
				}
				output = new FileOutputStream(file, true);
				return;
			}
			// OTHER DAY
			date = LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
			begin = date.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC) * 1000;
			end = date.toEpochSecond(LocalTime.MAX, ZoneOffset.UTC) * 1000 + 999;
			output = new FileOutputStream(CLFCoder.dateFile(file, date), true);
		}
	}

	public void write(CLFRecord record) {
		// 由三部分组成
		// [Index Pointers | Mandatory Fields | Optional Fields]
		// 所有字段最长不超过4096

		lock.lock();
		bytes.clear();
		try {
			// Index Pointers

			// VERSION 1Byte 'A'
			bytes.put(VERSION);
			// LENGTH 6Byte HEX
			bytes.position(7);
			// 0x2C ','
			bytes.put(COMMA);
			// POINTERS 8~55 56~59
			bytes.position(60);

			// 0x0A '\n'
			bytes.put(LF);
			// Timestamp 14Byte "seconds.millis"
			final long timestamp = record.getTimestamp();
			putTimestamp(timestamp);
			// 0x09 '\t'
			bytes.put(TAB);
			// Flags 5Byte
			bytes.put((byte) record.getType());
			bytes.put((byte) record.getRetransmission());
			bytes.put((byte) record.getDirection());
			bytes.put((byte) record.getTransport());
			bytes.put((byte) record.getEncryption());
			bytes.put(TAB);

			// Mandatory Fields (variable length)

			setPointer(8);
			putMandatory(record.getCSeqNumber());
			bytes.put(SPACE);
			putMandatory(record.getCSeqMethod());
			bytes.put(TAB);
			setPointer(12);
			putMandatory(record.getStatus());
			bytes.put(TAB);
			setPointer(16);
			putMandatory(record.getRURI());
			bytes.put(TAB);
			setPointer(20);
			putMandatory(record.getDestination());
			bytes.put(TAB);
			setPointer(24);
			putMandatory(record.getSource());
			bytes.put(TAB);
			setPointer(28);
			putMandatory(record.getTo());
			bytes.put(TAB);
			setPointer(32);
			putMandatory(record.getToTag());
			bytes.put(TAB);
			setPointer(36);
			putMandatory(record.getFrom());
			bytes.put(TAB);
			setPointer(40);
			putMandatory(record.getFromTag());
			bytes.put(TAB);
			setPointer(44);
			putMandatory(record.getCallId());
			bytes.put(TAB);
			setPointer(48);
			putMandatory(record.getServerTxn());
			bytes.put(TAB);
			setPointer(52);
			putMandatory(record.getClientTxn());

			// Optional Fields

			setPointer(56);
			final Collection<OptionalField> optionalFields = record.getOptionalFields();
			if (optionalFields != null && optionalFields.size() > 0) {
				// Tag@Vendor,Length,BEB,Value
				for (OptionalField optionalField : optionalFields) {
					if (optionalField != null && optionalField.getValue() != null) {
						bytes.put(TAB);
						// TAG 2Byte
						putTag(optionalField.getTag());
						bytes.put(AT);
						// Vendor 8Byte
						putVendor(optionalField.getVendor());
						bytes.put(COMMA);
						// Length 4Byte HEX
						int position = bytes.position();
						bytes.position(bytes.position() + 4);
						bytes.put(COMMA);
						// BEB 1Byte
						if (optionalField.isCharSequence()) {
							bytes.put((byte) '0');
							bytes.put((byte) '0');
						} else {
							bytes.put((byte) '0');
							bytes.put((byte) '1');
						}
						bytes.put(COMMA);
						// Value
						if (optionalField.getTag() == OptionalField.TAG_00) {
							// name: value
							putOptionalName(optionalField.getName());
							bytes.put(COLON);
							bytes.put(SPACE);
							if (optionalField.getValue() instanceof CharSequence) {
								putOptionalValue((CharSequence) optionalField.getValue());
							}
						} else if (optionalField.getTag() == OptionalField.TAG_01) {
							// type body
							putOptionalName(optionalField.getName());
							bytes.put(SPACE);
							if (optionalField.getValue() instanceof CharSequence) {
								putOptionalValue((CharSequence) optionalField.getValue());
							} else if (optionalField.getValue() instanceof ByteBuffer) {
								putOptionalBase64((ByteBuffer) optionalField.getValue());
							}
						} else if (optionalField.getTag() == OptionalField.TAG_02) {
							// data
							if (optionalField.getValue() instanceof CharSequence) {
								putOptionalValue((CharSequence) optionalField.getValue());
							} else if (optionalField.getValue() instanceof ByteBuffer) {
								putOptionalBase64((ByteBuffer) optionalField.getValue());
							}
						} else {
							// data
							if (optionalField.getValue() instanceof CharSequence) {
								putOptionalValue((CharSequence) optionalField.getValue());
							} else if (optionalField.getValue() instanceof ByteBuffer) {
								putOptionalBase64((ByteBuffer) optionalField.getValue());
							}
						}
						setOptionalLength(position, bytes.position() - position - 8);
					}
				}
			}

			// 0x0A '\n'
			bytes.put(LF);
			// 设置长度 6Byte
			setTotalLength(bytes.position());

			// Output
			check(timestamp);
			output.getChannel().write(bytes.flip());
			output.getChannel().force(true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 设置位置指针（固定4字节，十六进制字符）
	 */
	private void setPointer(int index) {
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
	private void setTotalLength(int length) {
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
	private void setOptionalLength(int index, int length) {
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
	private void putTimestamp(long timestamp) {
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
	private void putTag(int tag) {
		tag = tag % 100;
		bytes.put(digit(tag / 10, 10));
		tag = tag % 10;
		bytes.put(digit(tag, 10));
	}

	/**
	 * 10进制商标（固定8字节，十进制字符）
	 */
	private void putVendor(int vender) {
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
	private void putMandatory(int value) {
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
	private void putMandatory(long value) {
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
	private void putMandatory(CharSequence value) {
		if (value == null) {
			// 失败标记为'?'
			bytes.put(QUESTION);
		} else if (value.length() == 0) {
			// 空值则为'-'
			bytes.put(MINUS);
		} else {
			char c;
			chars.clear();
			if (value.length() == 1) {
				c = value.charAt(0);
				if (c == TAB) {
					// '\t'转义空格
					chars.put((char) SPACE);
				} else if (c == MINUS) {
					// 值为单个'-'转义为"%2D"
					chars.put('%');
					chars.put('2');
					chars.put('D');
				} else if (c == QUESTION) {
					// 值为单个'?'转义为"%3F"
					chars.put('%');
					chars.put('3');
					chars.put('F');
				} else {
					chars.put(c);
				}
			} else {
				for (int index = 0; index < value.length(); index++) {
					c = value.charAt(index);
					if (c == TAB) {
						// '\t'转义空格
						chars.put((char) SPACE);
					} else if (c == CR || c == LF) {
						// 忽略回车换行[CRLF]
					} else {
						chars.put(c);
					}
				}
			}
			// 如果字节缓冲区不足将不会编码额外字符
			// 超出字节缓冲区不会抛出异常
			// 未进行额外尝试，超出字符丢弃
			encoder.reset();
			encoder.encode(chars.flip(), bytes, true);
		}
	}

	/**
	 * 可选字段名称，执行转义，ASCII
	 */
	private void putOptionalName(CharSequence value) {
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
	private void putOptionalValue(CharSequence value) {
		char c;
		chars.clear();
		for (int index = 0; index < value.length(); index++) {
			c = value.charAt(index);
			if (c == TAB) {
				// '\t'转义空格
				chars.put((char) SPACE);
			} else if (c == CR) {
				// [CR]转义%0D
				chars.put('%');
				chars.put('0');
				chars.put('D');
			} else if (c == LF) {
				// [LF]转义%0A
				chars.put('%');
				chars.put('0');
				chars.put('A');
			} else {
				chars.put(c);
			}
		}
		encoder.reset();
		encoder.encode(chars.flip(), bytes, true);
	}

	/**
	 * 可选字段字节值，Base64编码
	 */
	private void putOptionalBase64(ByteBuffer data) {
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
	private static byte digit(int digit, int radix) {
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

	@Override
	public void close() throws IOException {
		if (output != null) {
			output.flush();
			output.close();
			output = null;
		}
	}

	public File getCurrentFile() {
		return file;
	}
}