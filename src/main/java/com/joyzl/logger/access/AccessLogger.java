package com.joyzl.logger.access;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;

import com.joyzl.logger.LoggerBuilder;
import com.joyzl.logger.LoggerService;
import com.joyzl.logger.LoggerWriter;
import com.joyzl.logger.RotatableLogger;
import com.joyzl.logger.RotateFile;

/**
 * 访问日志
 * 
 * @author ZhangXi 2025年6月8日
 */
public class AccessLogger extends RotatableLogger {

	final static char SPACE = ' ';
	final static char MINUS = '-';
	final static char LINE = '\n';

	private final AsynchronousWriter writer;

	/**
	 * access-20241202.log
	 */
	public AccessLogger(String file) throws IOException {
		this(file, "access", "-", ".log");
	}

	/**
	 * [Name][Split]20241202[Extension]
	 */
	public AccessLogger(String file, final String n, final String s, final String e) throws IOException {
		super(file, n, s, e);
		LoggerService.register(this);

		writer = new AsynchronousWriter();
		writer.setDaemon(true);
		writer.start();
	}

	/**
	 * 记录日志到文件，文件名自动按日期轮换"access-20241202.log"
	 */
	public void record(AccessRecord record) {
		final LoggerBuilder builder = LoggerBuilder.instance();
		builder.timestamp = record.requestTimestamp();
		build(builder.builder(), record);
		writer.put(builder);
	}

	/**
	 * 搜索日志从文件
	 */
	public List<AccessRecord> search(LocalDateTime begin, LocalDateTime end) throws IOException {
		final List<AccessRecord> records = new ArrayList<>();
		final RotateFile[] files = rotates(begin, end);
		// FileChannel channel;
		for (RotateFile file : files) {
			if (Files.exists(file.path())) {
				// channel = FileChannel.open(file.path(),
				// StandardOpenOption.READ);
				// 1749459420945 16:57:0.945 80 192.168.0.1 GET /web HTTP/1.1 0
				// TEST(中华人民共和国) 0 200 0
				// channel.read(null)
			}
		}
		return records;
	}

	@Override
	public void close() throws IOException {
		LoggerService.remove(this);
		writer.close();
	}

	/** 构造日志字符串 */
	private void build(StringBuilder builder, AccessRecord record) {
		builder.append(record.requestTimestamp());
		builder.append(SPACE);

		LoggerBuilder.encodeTime(builder, record.requestTimestamp());
		builder.append(SPACE);

		builder.append(record.serverPort());
		builder.append(SPACE);
		if (record.remoteAddress() != null) {
			builder.append(record.remoteAddress().getHostString());
		} else {
			builder.append(MINUS);
		}
		builder.append(SPACE);

		builder.append(record.requestMethod());
		builder.append(SPACE);
		builder.append(record.requestURI());
		builder.append(SPACE);
		builder.append(record.requestVersion());
		builder.append(SPACE);
		builder.append(record.requestBodySize());
		builder.append(SPACE);

		builder.append(record.servletName());
		builder.append(SPACE);
		builder.append(record.servletSpend());
		builder.append(SPACE);
		builder.append(record.responseStatus());
		builder.append(SPACE);
		builder.append(record.responseBodySize());
		builder.append(LINE);
	}

	/** 异步写 */
	private class AsynchronousWriter extends LoggerWriter<LoggerBuilder> {
		/** 待写的日志记录 */
		private final LinkedTransferQueue<LoggerBuilder> RECORDS = new LinkedTransferQueue<>();
		private final ByteBuffer buffer = ByteBuffer.allocateDirect(65536);
		private RotateFile file = RotateFile.EMPTY;
		private volatile boolean end;

		public AsynchronousWriter() {
			super("ACCESS LOGGER");
		}

		@Override
		public void put(LoggerBuilder item) {
			RECORDS.offer(item);
		}

		@Override
		public void run() {
			FileChannel channel = null;
			LoggerBuilder builder;

			// 可能的中断点：take write force

			while (true) {
				try {
					builder = RECORDS.take();
					do {
						if (file.beyond(builder.timestamp)) {
							if (channel != null) {
								channel.force(false);
								channel.close();
							}

							file = rotate(builder.timestamp);
							channel = FileChannel.open(file.path(), //
								StandardOpenOption.CREATE, //
								StandardOpenOption.WRITE, //
								StandardOpenOption.APPEND);
						}

						while (true) {
							if (builder.encodeUTF8(buffer)) {
								builder.release();
								break;
							} else {
								buffer.flip();
								while (buffer.hasRemaining()) {
									channel.write(buffer);
								}
								channel.force(false);
								buffer.clear();
							}
						}

						// NEXT
						builder = RECORDS.poll();
					} while (builder != null);

					if (buffer.position() > 0) {
						buffer.flip();
						while (buffer.hasRemaining()) {
							channel.write(buffer);
						}
						channel.force(false);
						buffer.clear();
					}
				} catch (InterruptedException e) {
					end = true;
					return;
				} catch (ClosedByInterruptException e) {
					end = true;
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void close() throws IOException {
			if (!LoggerService.isShutdown()) {
				// 如果不是程序终止则应当是用户关闭
				// 应触发中断任务先
				interrupt();
			}

			while (!end)
				;

			if (file != RotateFile.EMPTY) {
				FileChannel channel = null;
				// TODO 此判断有概率误判全空的缓存
				if (buffer.hasRemaining()) {
					channel = FileChannel.open(file.path(), //
						StandardOpenOption.WRITE, //
						StandardOpenOption.APPEND);
					while (buffer.hasRemaining()) {
						channel.write(buffer);
					}
					channel.force(false);
					buffer.clear();
				}
				LoggerBuilder builder = RECORDS.poll();
				while (builder != null) {
					if (file.beyond(builder.timestamp)) {
						if (channel != null) {
							channel.force(false);
							channel.close();
						}

						file = rotate(builder.timestamp);
						channel = FileChannel.open(file.path(), //
							StandardOpenOption.CREATE, //
							StandardOpenOption.WRITE, //
							StandardOpenOption.APPEND);
					}
					while (true) {
						if (builder.encodeUTF8(buffer)) {
							buffer.flip();
							while (buffer.hasRemaining()) {
								channel.write(buffer);
							}
							buffer.clear();
							break;
						}
					}
					buffer.flip();
					while (buffer.hasRemaining()) {
						channel.write(buffer);
					}
					buffer.clear();

					// NEXT
					builder = RECORDS.poll();
				}
				if (channel != null) {
					channel.force(false);
					channel.close();
				}
			}
		}
	};
}