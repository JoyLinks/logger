/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * 输出日志到网络
 * 
 * @author ZhangXi 2025年6月10日
 */
public class LoggerUDP implements Closeable {

	private final DatagramChannel channel;

	public LoggerUDP(String host, int port) throws IOException {
		channel = DatagramChannel.open();
		channel.configureBlocking(false);
		channel.connect(new InetSocketAddress(host, port));
		LoggerService.register(this);
	}

	public void output(ByteBuffer buffer) throws IOException {
		while (buffer.hasRemaining()) {
			channel.write(buffer);
		}
	}

	@Override
	public void close() throws IOException {
		LoggerService.remove(this);
		if (channel != null && channel.isOpen()) {
			channel.close();
		}
	}
}