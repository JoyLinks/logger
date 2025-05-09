/*
 * www.joyzl.com
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.logger.LogChannel.LogTextChannel;

/**
 * 控制台日志输出
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年1月28日
 */
public final class ConsoleChannel extends LogTextChannel {

	private final StringBuilder outBuilder = new StringBuilder();
	private final ReentrantLock outLock = new ReentrantLock();

	private final StringBuilder errBuilder = new StringBuilder();
	private final ReentrantLock errLock = new ReentrantLock();

	public ConsoleChannel() {
	}

	@Override
	void beginOut(LocalDateTime ldt) {
		outLock.lock();
		outBuilder.setLength(0);
	}

	@Override
	void writeOut(String text, char suffix) throws IOException {
		if (text.length() > 0) {
			outBuilder.append(text);
		}
		if (suffix > 0) {
			outBuilder.append(suffix);
		}
	}

	@Override
	void endOut() {
		System.out.append(outBuilder);
		outLock.unlock();
	}

	@Override
	void beginErr(LocalDateTime ldt) {
		errLock.lock();
		errBuilder.setLength(0);
	}

	@Override
	void writeErr(String text, char suffix) throws IOException {
		if (text.length() > 0) {
			errBuilder.append(text);
		}
		if (suffix > 0) {
			errBuilder.append(suffix);
		}
	}

	@Override
	void endErr() {
		System.err.append(errBuilder);
		errLock.unlock();
	}
}