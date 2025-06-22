/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 为所有日志提供基础服务：自动关闭、过期清理
 * 
 * @author ZhangXi 2025年6月8日
 */
public final class LoggerService {

	/** 日志过期天数 */
	private static volatile int EXPIRES = 30;

	/** 设置日志过期天数，过期日志文件将被删除 */
	public static synchronized void setExpires(int value) {
		EXPIRES = value;
	}

	/** 日志过期天数，过期日志文件将被删除 */
	public static int getExpires() {
		return EXPIRES;
	}

	private final static List<Closeable> CLOSEABLES = Collections.synchronizedList(new ArrayList<>());
	private final static Thread SHUTDOWN = new Thread("LOGGER SHUTDOWN") {
		@Override
		public void run() {
			for (Closeable item : CLOSEABLES) {
				try {
					item.close();
				} catch (IOException e) {
					continue;
				}
			}
		}
	};
	static {
		Runtime.getRuntime().addShutdownHook(SHUTDOWN);
	}

	public static LoggerCleaner clean() {
		final LoggerCleaner cleaner = new LoggerCleaner(EXPIRES);
		synchronized (CLOSEABLES) {
			for (Closeable item : CLOSEABLES) {
				if (item instanceof RotatableLogger logger) {
					cleaner.clean(logger);
				}
			}
		}
		return cleaner;
	}

	public static boolean isShutdown() {
		return Thread.currentThread() == SHUTDOWN;
	}

	public static void register(Closeable item) {
		CLOSEABLES.add(item);
	}

	public static void remove(Closeable item) {
		if (Thread.currentThread() != SHUTDOWN) {
			CLOSEABLES.remove(item);
		}
	}
}