/*
 * www.joyzl.com
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.logger.LogChannel.LogTextChannel;

/**
 * UDP日志输出
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年2月3日
 */
public final class UDPChannel extends LogTextChannel {

	private final DatagramChannel channel;

	private final SocketAddress OUT_BROADCAST;
	private final CharBuffer outChars = CharBuffer.allocate(1024);
	private final ByteBuffer outBytes = ByteBuffer.allocate(1514);
	private final CharsetEncoder outEncoder = LogSetting.CHARSET.newEncoder();
	private final ReentrantLock outLock = new ReentrantLock();

	private final SocketAddress ERR_BROADCAST;
	private final CharBuffer errChars = CharBuffer.allocate(1024);
	private final ByteBuffer errBytes = ByteBuffer.allocate(1514);
	private final CharsetEncoder errEncoder = LogSetting.CHARSET.newEncoder();
	private final ReentrantLock errLock = new ReentrantLock();

	public UDPChannel() {
		OUT_BROADCAST = new InetSocketAddress(LogSetting.DUP_BROADCAST_OUT, LogSetting.DUP_BROADCAST_OUT_PORT);
		ERR_BROADCAST = new InetSocketAddress(LogSetting.DUP_BROADCAST_ERR, LogSetting.DUP_BROADCAST_ERR_PORT);
		try {
			channel = DatagramChannel.open();
			channel.configureBlocking(true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	void beginOut(LocalDateTime ldt) {
		outLock.lock();
	}

	@Override
	void writeOut(String text, char suffix) throws IOException {
		int size = 0;
		int length = text.length();
		while (outChars.remaining() <= length - size) {
			while (outChars.hasRemaining()) {
				outChars.put(text.charAt(size++));
			}
			send(outChars.flip(), outBytes, outEncoder, OUT_BROADCAST);
		}
		while (size < length) {
			outChars.put(text.charAt(size++));
		}
		if (suffix > 0) {
			outChars.put(suffix);
		}
	}

	@Override
	void endOut() {
		if (outChars.flip().hasRemaining()) {
			try {
				send(outChars, outBytes, outEncoder, OUT_BROADCAST);
			} catch (IOException e) {
				outLock.unlock();
			}
		} else {
			outChars.clear();
		}
		outLock.unlock();
	}

	@Override
	void beginErr(LocalDateTime ldt) {
		errLock.lock();
	}

	@Override
	void writeErr(String text, char suffix) throws IOException {
		int size = 0;
		int length = text.length();
		while (errChars.remaining() <= length - size) {
			while (errChars.hasRemaining()) {
				errChars.put(text.charAt(size++));
			}
			send(errChars.flip(), errBytes, errEncoder, ERR_BROADCAST);
		}
		while (size < length) {
			errChars.put(text.charAt(size++));
		}
		if (suffix > 0) {
			errChars.put(suffix);
		}
	}

	@Override
	void endErr() {
		if (errChars.flip().hasRemaining()) {
			try {
				send(errChars, errBytes, errEncoder, ERR_BROADCAST);
			} catch (IOException e) {
				errLock.unlock();
			}
		} else {
			errChars.clear();
		}
		errLock.unlock();
	}

	void send(CharBuffer chars, ByteBuffer bytes, CharsetEncoder encoder, SocketAddress address) throws IOException {
		CoderResult result;
		do {
			result = encoder.reset().encode(chars, bytes.clear(), true);
			bytes.flip();
			while (bytes.hasRemaining()) {
				channel.send(bytes, address);
			}
		} while (result == CoderResult.OVERFLOW);
		chars.clear();
	}
}
