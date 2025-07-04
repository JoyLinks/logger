/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * 日志网络接收
 * 
 * @author ZhangXi 2025年6月10日
 */
public class LoggerReceiver implements Closeable {

	private final DatagramChannel channel;
	private final Thread thread;

	public LoggerReceiver(int port) throws IOException {
		channel = DatagramChannel.open();
		channel.configureBlocking(true);
		channel.bind(new InetSocketAddress(port));

		thread = new Thread(RECEIVER, "LOGGER RECEIVER");
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public void close() throws IOException {
		if (channel != null && channel.isOpen()) {
			channel.close();
			thread.interrupt();
		}
	}

	/**
	 * 接收字节串，重载此方法实现字节解码，默认行为解码为UTF8字符串
	 * 
	 * @param address 发送方地址
	 * @param buffer 接收的字节串
	 */
	protected void receive(SocketAddress address, ByteBuffer buffer) {
		final LoggerBuilder builder = LoggerBuilder.instance();
		builder.decodeUTF8(buffer);
		receive(address, builder.builder());
		builder.release();
	}

	/**
	 * 接收字符串，重载此方法实现字符串输出，默认行为输出控制台
	 * 
	 * @param address 发送方地址
	 * @param chars 接收的字节串
	 */
	protected void receive(SocketAddress address, CharSequence chars) {
		System.out.append(chars);
	}

	private final Runnable RECEIVER = new Runnable() {
		private final static ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

		@Override
		public void run() {
			SocketAddress address;
			try {
				while (channel.isOpen()) {
					address = channel.receive(buffer.clear());
					if (address != null) {
						receive(address, buffer.flip());
					}
				}
			} catch (IOException e) {
				return;
			}
		}
	};
}