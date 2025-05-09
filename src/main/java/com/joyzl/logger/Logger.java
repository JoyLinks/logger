/*
 * www.joyzl.com
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.logger;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 日志
 * 
 * <p>
 * 输出级别：ERROR INFO DEBUG
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年1月28日
 */
public final class Logger {

	private static LogChannel[] channels;

	static {
		Logger.addChannel(new ConsoleChannel());
		Logger.addChannel(new FileChannel());
	}

	public static void addChannel(LogChannel channel) {
		if (channels == null) {
			channels = new LogChannel[] { channel };
		} else {
			channels = Arrays.copyOf(channels, channels.length + 1);
			channels[channels.length - 1] = channel;
		}
	}

	public static void info(Object... messages) {
		if (LogSetting.LEVEL > 1) {
			out(LocalDateTime.now(), Thread.currentThread().getName(), LogSetting.INFOM, messages);
		}
	}

	public static void debug(Object... messages) {
		if (LogSetting.LEVEL > 2) {
			out(LocalDateTime.now(), Thread.currentThread().getName(), LogSetting.DEBUG, messages);
		}
	}

	public static void error(Object... messages) {
		if (LogSetting.LEVEL > 0) {
			out(LocalDateTime.now(), Thread.currentThread().getName(), LogSetting.ERROR, messages);
		}
	}

	private static void out(LocalDateTime ldt, String thread, String type, Object... messages) {
		for (int index = 0; index < channels.length; index++) {
			channels[index].write(ldt, thread, type, messages);
		}
	}

	public static void error(Throwable message) {
		if (LogSetting.LEVEL > 0) {
			final LocalDateTime ldt = LocalDateTime.now();
			for (int index = 0; index < channels.length; index++) {
				channels[index].write(ldt, Thread.currentThread().getName(), LogSetting.ERROR, message);
			}
		}
	}
}
