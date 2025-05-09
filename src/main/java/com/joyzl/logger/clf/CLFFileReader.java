package com.joyzl.logger.clf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

/**
 * CLF Encode and Decode (UTF-8)
 * 
 * @author ZhangXi 2024年12月2日
 */
public class CLFFileReader implements CLFCoder {

	private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
	private final ByteBuffer bytes = ByteBuffer.allocateDirect(65536);
	private final CharBuffer chars = CharBuffer.allocate(4096);
	private final File file;

	public CLFFileReader(File file) {
		this.file = file;
	}

	public List<CLFRecord> search(LocalDateTime begin, LocalDateTime end) throws IOException {
		final List<CLFRecord> records = new ArrayList<>(128);
		if (begin == null) {
			if (end == null) {
				if (file.exists()) {
					read(file, records, Long.MIN_VALUE, Long.MAX_VALUE);
				}
				return records;
			} else {
				begin = end.minusDays(1);
			}
		} else if (end == null) {
			end = begin.plusDays(1);
		}

		if (begin.isBefore(end)) {
			long b = begin.toEpochSecond(ZoneOffset.UTC) * 1000 + begin.getNano() / 1000000;
			long e = end.toEpochSecond(ZoneOffset.UTC) * 1000 + end.getNano() / 1000000;
			File f;
			do {
				f = CLFCoder.dateFile(file, end.toLocalDate());
				if (f.exists()) {
					read(f, records, b, e);
				}
				end = end.minusDays(1);
			} while (begin.isBefore(end));
		}
		return records;
	}

	public List<CLFRecord> read(File file) throws IOException {
		if (file.exists()) {
			final List<CLFRecord> records = new ArrayList<>(128);
			read(file, records, Long.MIN_VALUE, Long.MAX_VALUE);
			return records;
		}
		return null;
	}

	private void read(File file, Collection<CLFRecord> records, long begin, long end) throws IOException {
		try (final FileInputStream input = new FileInputStream(file);
			final FileChannel channel = input.getChannel();) {
			CLFRecordDefault record;
			while (channel.isOpen()) {
				bytes.limit(76);
				bytes.position(0);
				if (channel.read(bytes) == 76) {
					bytes.flip();

					// 由三部分组成
					// [Index Pointers | Mandatory Fields | OptionalFields]
					// 首先读取前76字节，包含Index Pointers和Timestamp

					// VERSION 1Byte 'A'
					if (bytes.get() == VERSION) {
						// Length 6Byte HEX
						int length = getHEX(6);
						// 0x2C ','
						bytes.get();
						// POINTERS 8~55 56~59
						// CSeq Pointer (Hex)
						int cSeq = getHEX(4);
						// Response Status-Code Pointer (Hex)
						int status = getHEX(4);
						// R-URI Pointer (Hex)
						int RURI = getHEX(4);
						// Destination IP address:port Pointer (Hex)
						int destination = getHEX(4);
						// Source IP address:port Pointer (Hex)
						int source = getHEX(4);
						// To URI Pointer (Hex)
						int to = getHEX(4);
						// To Tag Pointer (Hex)
						int toTag = getHEX(4);
						// From URI Pointer (Hex)
						int from = getHEX(4);
						// From Tag Pointer (Hex)
						int fromTag = getHEX(4);
						// Call-Id Pointer (Hex)
						int callId = getHEX(4);
						// Server-Txn Pointer (Hex)
						int serverTxn = getHEX(4);
						// Client-Txn Pointer (Hex)
						int clientTxn = getHEX(4);
						// Optional Fields Start Pointer (Hex)
						int optional = getHEX(4);
						// 0x0A '\n'
						bytes.get();
						// Timestamp
						long timestamp = getTimestamp();
						// 0x09 '\t'
						bytes.get();

						// 剩余字节
						length -= 76;
						if (timestamp >= begin && timestamp <= end) {
							bytes.position(0);
							bytes.limit(length);
							if (channel.read(bytes) == length) {
								bytes.flip();

								record = new CLFRecordDefault();
								record.setTimestamp(timestamp);

								// Flags 5Byte
								record.setType((char) bytes.get());
								record.setRetransmission((char) bytes.get());
								record.setDirection((char) bytes.get());
								record.setTransport((char) bytes.get());
								record.setEncryption((char) bytes.get());
								// 0x09 '\t'
								bytes.get();

								// Mandatory Fields (variable length)

								getMandatory(status - cSeq - 1);
								cSeq = 0;
								while (cSeq < chars.limit()) {
									if (chars.get(cSeq) == SPACE) {
										record.setCSeqNumber(Long.parseLong(chars, 0, cSeq, 10));
										chars.position(cSeq + 1);
										break;
									}
									cSeq++;
								}
								record.setCSeqMethod(chars.toString());

								bytes.get();// TAB

								getMandatory(RURI - status - 1);
								if (chars.remaining() > 0) {
									record.setStatus(Integer.parseInt(chars, 0, chars.position(), 10));
								}

								bytes.get();// TAB

								getMandatory(destination - RURI - 1);
								record.setRURI(chars.toString());
								bytes.get();// TAB
								getMandatory(source - destination - 1);
								record.setDestination(chars.toString());
								bytes.get();// TAB
								getMandatory(to - source - 1);
								record.setSource(chars.toString());
								bytes.get();// TAB
								getMandatory(toTag - to - 1);
								record.setTo(chars.toString());
								bytes.get();// TAB
								getMandatory(from - toTag - 1);
								record.setToTag(chars.toString());
								bytes.get();// TAB
								getMandatory(fromTag - from - 1);
								record.setFrom(chars.toString());
								bytes.get();// TAB
								getMandatory(callId - fromTag - 1);
								record.setFromTag(chars.toString());
								bytes.get();// TAB
								getMandatory(serverTxn - callId - 1);
								record.setCallId(chars.toString());
								bytes.get();// TAB
								getMandatory(clientTxn - serverTxn - 1);
								record.setServerTxn(chars.toString());
								bytes.get();// TAB
								// 此处计算获取数量时不减一
								getMandatory(optional - clientTxn);
								record.setClientTxn(chars.toString());

								// Optional Fields

								while (bytes.remaining() > 1) {
									bytes.get();// TAB
									// Tag@
									to = getTag();
									bytes.get();// '@'
									// Vendor,
									status = getVendor();
									bytes.get();// ','
									// Length,
									length = getHEX(4);
									bytes.get();// ','
									// BEB,
									from = getHEX(2);
									bytes.get();// ','
									if (status == OptionalField.VENDOR_DEFAULT) {
										if (to == OptionalField.TAG_00) {
											final MessageHeader header = new MessageHeader();
											// name
											source = getOptionalName(COLON);
											header.setName(chars.toString());
											bytes.get();// SPACE
											length -= source + 1;
											// value
											if (from == 0) {
												header.setValue(getOptionalValue(length));
											} else {
												bytes.position(bytes.position() + length);
											}
											record.addOptionalField(header);
											continue;
										} else if (to == OptionalField.TAG_01) {
											final MessageBody body = new MessageBody();
											// type
											source = getOptionalName(SPACE);
											body.setContentType(chars.toString());
											length -= source;
											// data
											if (from > 0) {
												body.setValue(getOptionalBytes(length));
											} else {
												body.setValue(getOptionalChars(length));
											}
											record.addOptionalField(body);
											continue;
										} else if (to == OptionalField.TAG_02) {
											final MessageEntire entire = new MessageEntire();
											if (from > 0) {
												entire.setEntire(getOptionalBytes(length));
											} else {
												entire.setEntire(getOptionalChars(length));
											}
											record.addOptionalField(entire);
											continue;
										}
									}
									final MessageOther other = new MessageOther(to, status);
									if (from > 0) {
										other.setValue(getOptionalBytes(length));
									} else {
										other.setValue(getOptionalChars(length));
									}
									record.addOptionalField(other);
								}
								// LF
								bytes.get();
								// 得呢
								records.add(record);
							} else {
								// 意外结束
								break;
							}
						} else {
							// 忽略此记录
							channel.position(channel.position() + length);
						}
					} else {
						// 版本不对也，咋搞
						throw new IOException("日志版本标识不匹配，应为'A'");
					}
				} else {
					break;
				}
			}
		}
	}

	/**
	 * 获取指定字节数量的十六进制值
	 */
	private int getHEX(int size) {
		int value = 0;
		while (size-- > 0) {
			value *= 16;
			value += digit(bytes.get(), 16);
		}
		return value;
	}

	/**
	 * 10进制时间戳 (固定14字节，十进制字符，"seconds(10).millis(3)")
	 */
	private long getTimestamp() {
		long timestamp = 0;
		// Seconds 10Byte
		timestamp = digit(bytes.get(), 10);
		timestamp *= 10;
		timestamp += digit(bytes.get(), 10);
		timestamp *= 10;
		timestamp += digit(bytes.get(), 10);
		timestamp *= 10;
		timestamp += digit(bytes.get(), 10);
		timestamp *= 10;
		timestamp += digit(bytes.get(), 10);
		timestamp *= 10;
		timestamp += digit(bytes.get(), 10);
		timestamp *= 10;
		timestamp += digit(bytes.get(), 10);
		timestamp *= 10;
		timestamp += digit(bytes.get(), 10);
		timestamp *= 10;
		timestamp += digit(bytes.get(), 10);
		timestamp *= 10;
		timestamp += digit(bytes.get(), 10);
		// 0x2E '.'
		bytes.get();
		// Millisecond 3Byte
		timestamp *= 10;
		timestamp += digit(bytes.get(), 10);
		timestamp *= 10;
		timestamp += digit(bytes.get(), 10);
		timestamp *= 10;
		timestamp += digit(bytes.get(), 10);
		return timestamp;
	}

	/**
	 * 10进制标记（固定2字节，十进制字符）
	 */
	private int getTag() {
		int tag = digit(bytes.get(), 10);
		tag *= 10;
		tag += digit(bytes.get(), 10);
		return tag;
	}

	/**
	 * 10进制商标（固定8字节，十进制字符）
	 */
	private int getVendor() {
		int vender = digit(bytes.get(), 10);
		vender *= 10;
		vender += digit(bytes.get(), 10);
		vender *= 10;
		vender += digit(bytes.get(), 10);
		vender *= 10;
		vender += digit(bytes.get(), 10);
		vender *= 10;
		vender += digit(bytes.get(), 10);
		vender *= 10;
		vender += digit(bytes.get(), 10);
		vender *= 10;
		vender += digit(bytes.get(), 10);
		vender *= 10;
		vender += digit(bytes.get(), 10);
		return vender;
	}

	/**
	 * 获取指定长度字符串
	 */
	private void getMandatory(int size) {
		// '\t'转义空格，已无法转回
		if (size == 1) {
			bytes.mark();
			if (bytes.get() == MINUS) {
				chars.position(0);
				chars.limit(0);
				return;
			}
			if (bytes.get() == QUESTION) {
				chars.position(0);
				chars.limit(0);
				return;
			}
			bytes.reset();
		} else if (size == 3) {
			// 值为单个'-'转义为"%2D"
			// 值为单个'?'转义为"%3F"
			bytes.mark();
			if (bytes.get() == '%') {
				int c = bytes.get();
				if (c == '2') {
					if (bytes.get() == 'D') {
						chars.position(0);
						chars.limit(0);
						return;
					}
				} else if (c == '3') {
					if (bytes.get() == 'F') {
						chars.position(0);
						chars.limit(0);
						return;
					}
				}
			}
			bytes.reset();
		}

		final int limit = bytes.limit();
		bytes.limit(bytes.position() + size);
		chars.clear();
		decoder.reset();
		decoder.decode(bytes, chars, true);
		bytes.limit(limit);
		chars.flip();
	}

	/**
	 * 获取指定标记字符串，用于可选字段 Header name 或 Content-Type 获取，仅ASCII字符无任何转义
	 */
	private int getOptionalName(byte flag) {
		byte c;
		int size = 0;
		chars.clear();
		while (bytes.hasRemaining()) {
			c = bytes.get();
			size++;
			if (c == flag) {
				break;
			}
			chars.put((char) c);
		}
		chars.flip();
		return size;
	}

	/**
	 * 获取指定字节数的字符串，用于可选字段 Header value 获取，仅ASCII字符无任何转义
	 */
	private String getOptionalValue(int size) {
		chars.clear();
		while (size-- > 0 && bytes.hasRemaining()) {
			chars.put((char) bytes.get());
		}
		chars.flip();
		return chars.toString();
	}

	/**
	 * 获取指定长度字符串，用于可选字段文本内容获取
	 */
	private String getOptionalChars(int size) {
		// TAB 已被替换为 SPACE 此转义不可逆
		// CRLF 已转义为 %0D%0A

		// 逐字节检查
		// 遇到'%'时将之前的字节解析为UTF-8字符
		// 判断是否为转义并处理
		// 重复此过程

		final int limit = bytes.limit();
		bytes.limit(bytes.position() + size);
		decoder.reset();
		chars.clear();

		byte c;
		bytes.mark();
		while (bytes.hasRemaining()) {
			c = bytes.get();
			if (c == '%') {
				c = bytes.get(bytes.position());
				if (c == '0') {
					c = bytes.get(bytes.position() + 1);
					if (c == 'D') {
						size = bytes.limit();
						bytes.limit(bytes.position() - 1);
						bytes.reset();

						decoder.decode(bytes, chars, true);
						chars.put((char) CR);

						bytes.limit(size);
						bytes.position(bytes.position() + 3);
						bytes.mark();
						continue;
					}
					if (c == 'A') {
						size = bytes.limit();
						bytes.limit(bytes.position() - 1);
						bytes.reset();

						decoder.decode(bytes, chars, true);
						chars.put((char) LF);

						bytes.limit(size);
						bytes.position(bytes.position() + 3);
						bytes.mark();
						continue;
					}
				}
			}
		}
		bytes.reset();
		decoder.decode(bytes, chars, true);
		bytes.limit(limit);
		chars.flip();
		return chars.toString();
	}

	/**
	 * 获取可选字段，Base64编码的字节数据
	 */
	private ByteBuffer getOptionalBytes(int size) {
		final int limit = bytes.limit();
		bytes.limit(bytes.position() + size);

		// 检查是否具有 %0D%0A 转义
		// '%'不属于Base64有效字符
		// 将转义字符0D0A覆盖为无效字符
		// MimeDecoder时将忽略无效字符
		// 可考虑优化重写Base64编码方式

		bytes.mark();
		while (bytes.hasRemaining()) {
			if (bytes.get() == '%') {
				if (bytes.get() == '0') {
					size = bytes.get();
					if (size == 'D' || size == 'A') {
						bytes.put(bytes.position() - 1, (byte) '%');
						bytes.put(bytes.position() - 2, (byte) '%');
					}
				}
			}
		}
		bytes.reset();
		final ByteBuffer data = Base64.getMimeDecoder().decode(bytes);
		bytes.limit(limit);
		return data;
	}

	/**
	 * 字符转换指定进制数值
	 */
	private static int digit(byte digit, int radix) {
		return Character.digit(digit, radix);
	}
}