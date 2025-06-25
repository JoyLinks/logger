/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger.common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.logger.LoggerService;
import com.joyzl.logger.RotatableLogger;
import com.joyzl.logger.RotateFile;

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
public final class CommonLogger extends RotatableLogger {

	/** 4096 UTF-8(Char MAX 4Byte) */
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(4096 * 4);
	private final ReentrantLock lock = new ReentrantLock();
	RotateFile file = RotateFile.EMPTY;
	FileChannel channel;

	/*
	 * clf-20241202.log
	 */
	public CommonLogger(String file) throws IOException {
		this(file, "clf", "-", ".log");
	}

	/**
	 * [Name][Split]20241202[Extension]
	 */
	public CommonLogger(String file, final String n, final String s, final String e) throws IOException {
		super(file, n, s, e);
		LoggerService.register(this);
	}

	/**
	 * 记录日志到文件，文件名自动轮换"clf-20241202.log"
	 */
	public void record(CommonRecord record) throws IOException {
		lock.lock();
		try {
			if (file.beyond(record.getTimestamp())) {
				if (channel != null && channel.isOpen()) {
					channel.force(false);
					channel.close();
				}
				file = rotate(record.getTimestamp());
				channel = FileChannel.open(file.path(), //
					StandardOpenOption.CREATE, //
					StandardOpenOption.WRITE, //
					StandardOpenOption.APPEND);
			}

			CommonWriter.encode(record, buffer);
			buffer.flip();
			while (buffer.hasRemaining()) {
				channel.write(buffer);
			}
			channel.force(false);
			buffer.clear();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 搜索日志从文件
	 */
	public List<CommonRecord> search(LocalDateTime begin, LocalDateTime end) throws IOException {
		final List<CommonRecord> records = new ArrayList<>();
		final RotateFile[] files = rotates(begin, end);
		if (files.length > 0) {
			final CommonReader reader = new CommonReader();
			for (RotateFile file : files) {
				if (Files.exists(file.path())) {
					reader.read(file, records);
				}
			}
		}
		return records;
	}

	@Override
	public void close() throws IOException {
		LoggerService.remove(this);
		if (channel != null && channel.isOpen()) {
			channel.force(false);
			channel.close();
		}
	}
}