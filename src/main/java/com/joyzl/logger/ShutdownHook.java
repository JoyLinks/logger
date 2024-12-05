package com.joyzl.logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * for Runtime.getRuntime().addShutdownHook();
 * 
 * @author ZhangXi 2024年12月5日
 */
public class ShutdownHook extends Thread {

	private final static ShutdownHook INSTANCE = new ShutdownHook();
	static {
		Runtime.getRuntime().addShutdownHook(INSTANCE);
	}

	public static void register(Closeable item) {
		INSTANCE.add(item);
	}

	public static void remove(Closeable item) {
		INSTANCE.off(item);
	}

	private final List<Closeable> CLOSEABLES = new ArrayList<>();

	private ShutdownHook() {
	}

	public void run() {
		synchronized (CLOSEABLES) {
			for (Closeable item : CLOSEABLES) {
				try {
					item.close();
				} catch (IOException e) {
					// 忽略
				}
			}
		}
	}

	private void add(Closeable item) {
		synchronized (CLOSEABLES) {
			CLOSEABLES.add(item);
		}
	}

	private void off(Closeable item) {
		synchronized (CLOSEABLES) {
			CLOSEABLES.remove(item);
		}
	}
}