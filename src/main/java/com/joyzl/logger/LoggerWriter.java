package com.joyzl.logger;

import java.io.Closeable;

/**
 * 异步日志写入
 * 
 * @author ZhangXi 2025年6月10日
 */
public abstract class LoggerWriter<T> extends Thread implements Closeable {

	public LoggerWriter(String name) {
		super(name);
	}

	public abstract void put(T t);
}