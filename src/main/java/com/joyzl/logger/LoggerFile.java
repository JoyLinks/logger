/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class LoggerFile extends RotatableLogger {

	private RotateFile file = RotateFile.EMPTY;
	private FileChannel channel;

	public LoggerFile(String file, String n, String s, String e) throws IOException {
		super(file, n, s, e);
		LoggerService.register(this);
	}

	public void output(ByteBuffer buffer, long timestamp) throws IOException {
		if (file.beyond(timestamp)) {
			if (channel != null && channel.isOpen()) {
				channel.force(false);
				channel.close();
			}
			file = rotate(timestamp);
			channel = FileChannel.open(file.path(), //
				StandardOpenOption.CREATE, //
				StandardOpenOption.WRITE, //
				StandardOpenOption.APPEND);
		}

		while (buffer.hasRemaining()) {
			channel.write(buffer);
		}
		channel.force(false);
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