/*
 * www.joyzl.com
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 日志
 * 
 * <p>
 * 输出级别：ERROR=1,INFO=2,DEBUG=3<br>
 * 字符编码：UTF-8
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年1月28日
 */
public final class Logger {

	final static String INFOM = "INFOM";
	final static String DEBUG = "DEBUG";
	final static String ERROR = "ERROR";

	/** ERROR=1,INFO=2,DEBUG=3 */
	static volatile int LEVEL = 3;

	/** 设置日志级别 ERROR=1,INFO=2,DEBUG=3 */
	public static synchronized void setLevel(int value) {
		LEVEL = value;
	}

	/** 日志级别 */
	public static int getLevel() {
		return LEVEL;
	}

	/** 缩进(异常堆栈输出时) */
	static volatile String INDENT = "            ";
	/** 分隔符 */
	static volatile char TAB = '\t';
	/** 换行符 */
	static volatile char LINE = '\n';

	/** 设置输出异常时的缩进 */
	public static synchronized void setIndent(String value) {
		INDENT = value;
	}

	/** 输出异常时的缩进 */
	public static String getIndent() {
		return INDENT;
	}

	/** 设置分隔符 */
	public static synchronized void setTab(char value) {
		TAB = value;
	}

	/** 固定字段分隔符 */
	public static char getTab() {
		return TAB;
	}

	/** 设置换行符 */
	public static synchronized void setLine(char value) {
		LINE = value;
	}

	/** 换行符 */
	public static char getLine() {
		return LINE;
	}

	/** 控制台输出 */
	static volatile boolean CONSOLE = true;

	/** 设置控制台输出 */
	public static synchronized void setConsole(boolean value) {
		CONSOLE = value;
	}

	/** 控制台输出 */
	public static boolean isConsole() {
		return CONSOLE;
	}

	/** 日志目录 */
	static volatile String FILE_FOLDER = "log";
	/** 日志文件名 */
	static volatile String FILE_NAME = "";
	/** 日志文件扩展名 */
	static volatile String FILE_EXTENSION = ".log";

	/** 设置日志目录和文件名 */
	public static synchronized void setFile(String folder, String name, String ext) throws IOException {
		FILE_FOLDER = folder;
		FILE_NAME = name;
		FILE_EXTENSION = ext;
		if (file != null) {
			file.close();
			file = null;
		}
		if (FILE_FOLDER != null) {
			if (FILE_NAME == null || FILE_NAME.length() == 0) {
				file = new LoggerFile(FILE_FOLDER, "", "", FILE_EXTENSION);
			} else {
				file = new LoggerFile(FILE_FOLDER, FILE_NAME, "-", FILE_EXTENSION);
			}
		}
	}

	/** 日志目录 */
	public static String getFileFolder() {
		return FILE_FOLDER;
	}

	/** 日志文件名 */
	public static String getFileName() {
		return FILE_NAME;
	}

	/** 日志文件扩展名 */
	public static String getFileExtension() {
		return FILE_EXTENSION;
	}

	/** UDP主机 */
	static volatile String DUP_HOST = null;
	/** UDP端口 */
	static volatile int DUP_PORT = 0;

	/** 设置日志输出的网络目标(UDP) */
	public static synchronized void setUDP(String host, int port) throws IOException {
		DUP_HOST = host;
		DUP_PORT = port;
		if (udp != null) {
			udp.close();
			udp = null;
		}
		if (host != null && port > 0) {
			udp = new LoggerUDP(DUP_HOST, DUP_PORT);
		}
	}

	/** 网络输出目标主机 */
	public static String getDUPHost() {
		return DUP_HOST;
	}

	/** 网络输出目标端口 */
	public static int getDUPPort() {
		return DUP_PORT;
	}

	private static volatile LoggerUDP udp;
	private static volatile LoggerFile file;

	static {
		try {
			setFile(FILE_FOLDER, FILE_NAME, FILE_EXTENSION);
			setUDP(DUP_HOST, DUP_PORT);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Logger() {
		// 不要实例
	}

	public static void info(Object... messages) {
		if (LEVEL > 1) {
			final LoggerBuilder builder = LoggerBuilder.instance();
			builder.timestamp = System.currentTimeMillis();
			LoggerBuilder.encodeTime(builder.builder(), builder.timestamp);
			build(builder.builder(), INFOM, messages);
			output(builder, INFOM);
		}
	}

	public static void debug(Object... messages) {
		if (LEVEL > 2) {
			final LoggerBuilder builder = LoggerBuilder.instance();
			builder.timestamp = System.currentTimeMillis();
			LoggerBuilder.encodeTime(builder.builder(), builder.timestamp);
			build(builder.builder(), DEBUG, messages);
			output(builder, DEBUG);
		}
	}

	public static void error(Object... messages) {
		if (LEVEL > 0) {
			final LoggerBuilder builder = LoggerBuilder.instance();
			builder.timestamp = System.currentTimeMillis();
			LoggerBuilder.encodeTime(builder.builder(), builder.timestamp);
			build(builder.builder(), ERROR, messages);
			output(builder, ERROR);
		}
	}

	public static void error(Throwable e) {
		if (LEVEL > 0) {
			final LoggerBuilder builder = LoggerBuilder.instance();
			builder.timestamp = System.currentTimeMillis();
			LoggerBuilder.encodeTime(builder.builder(), builder.timestamp);
			build(builder.builder(), ERROR, e);
			output(builder, ERROR);
		}
	}

	private final static ReentrantLock lock = new ReentrantLock();
	private final static ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

	private static void output(LoggerBuilder builder, String type) {
		if (CONSOLE) {
			if (type == ERROR) {
				System.err.append(builder.builder());
			} else {
				System.out.append(builder.builder());
			}
		}

		lock.lock();
		try {
			while (!builder.encodeUTF8(buffer)) {
				buffer.flip();
				if (file != null) {
					file.output(buffer, builder.timestamp);
				}
				if (udp != null) {
					buffer.position(0);
					udp.output(buffer);
				}
				buffer.clear();
			}
			buffer.flip();
			if (file != null) {
				file.output(buffer, builder.timestamp);
			}
			if (udp != null) {
				buffer.position(0);
				udp.output(buffer);
			}
			buffer.clear();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
			builder.release();
		}
	}

	/**
	 * <pre>
	 * 2021-02-02 15:17:17 main INFO fields
	 * </pre>
	 */
	private static void build(StringBuilder builder, String type, Object[] fields) {
		// builder.append(TIME_FORMATTER.format(LocalTime.now()));
		builder.append(TAB);
		builder.append(Thread.currentThread().getName());
		builder.append(TAB);
		builder.append(type);
		builder.append(TAB);

		for (int index = 0; index < fields.length; index++) {
			if (fields[index] != null) {
				builder.append(fields[index]);
			}
		}
		builder.append(LINE);
	}

	private static void build(StringBuilder builder, String type, Throwable exception) {
		// builder.append(TIME_FORMATTER.format(LocalTime.now()));
		builder.append(TAB);
		builder.append(Thread.currentThread().getName());
		builder.append(TAB);
		builder.append(type);
		builder.append(TAB);

		if (exception != null) {
			builder.append(exception.getClass().getName());
			if (exception.getLocalizedMessage() != null) {
				builder.append(':');
				builder.append(exception.getLocalizedMessage());
			} else if (exception.getMessage() != null) {
				builder.append(':');
				builder.append(exception.getMessage());
			}

			StackTraceElement trace;
			final StackTraceElement[] traces = exception.getStackTrace();
			for (int index = 0; index < traces.length; index++) {
				trace = traces[index];
				builder.append(INDENT);
				builder.append(TAB);
				if (trace.getClassLoaderName() != null) {
					builder.append(trace.getClassLoaderName());
					builder.append('/');
				}
				if (trace.getModuleName() != null) {
					if (trace.getModuleVersion() != null) {
						builder.append(trace.getModuleName());
						builder.append('@');
						builder.append(trace.getModuleVersion());
						builder.append('/');
					} else {
						builder.append(trace.getModuleName());
						builder.append('/');
					}
				}
				builder.append(trace.getClassName());
				builder.append('.');
				builder.append(trace.getMethodName());
				builder.append('(');
				if (trace.isNativeMethod()) {
					builder.append("Native Method)");
					builder.append(LINE);
				} else if (trace.getFileName() != null) {
					if (trace.getLineNumber() >= 0) {
						builder.append(trace.getFileName());
						builder.append(':');
						builder.append(trace.getLineNumber());
						builder.append(')');
						builder.append(LINE);
					} else {
						builder.append(trace.getFileName());
						builder.append(')');
						builder.append(LINE);
					}
				} else {
					builder.append("Unknown Source)");
					builder.append(LINE);
				}
			}
		} else {
			builder.append(LINE);
		}
	}
}
