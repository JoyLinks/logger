/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
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