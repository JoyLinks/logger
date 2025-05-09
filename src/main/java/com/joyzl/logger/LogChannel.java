/*
 * www.joyzl.com
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.logger;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 日志输出通道
 * 
 * <pre>
 * 2021-01-28 17:10:05 THREAD INFO NAME MESSAGE
 * </pre>
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年1月28日
 */
public abstract class LogChannel {

	protected final static String NULL = "NULL";
	protected final static String EMPTY = "";
	protected final static char EMPTY_CHAR = '\0';

	public abstract void write(LocalDateTime ldt, String thread, String type, Object... messages);

	public abstract void write(LocalDateTime ldt, String thread, String type, Throwable messages);

	////////////////////////////////////////////////////////////////////////////////

	/**
	 * 实现基于文本日志的基础格式化
	 * <p>
	 * 基于文本流的日志通道可继承此类,例如写入文本文件
	 * 
	 * @author simon (ZhangXi TEL:13883833982)
	 * @date 2021年2月3日
	 */
	static abstract class LogTextChannel extends LogChannel {

		@Override
		public final void write(LocalDateTime ldt, String thread, String type, Object... messages) {
			beginOut(ldt);
			// 2021-02-02 15:17:17 main INFO BOOT
			try {
				writeOut(LogSetting.DATETIME_FORMATTER.format(ldt), LogSetting.TAB);
				writeOut(thread, LogSetting.TAB);
				writeOut(type, LogSetting.TAB);
				for (int index = 0; index < messages.length; index++) {
					if (messages[index] == null) {
					} else {
						type = messages[index].toString();
						writeOut(type == null ? NULL : type, EMPTY_CHAR);
					}
				}
				writeOut(EMPTY, LogSetting.LINE);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				endOut();
			}
		}

		abstract void beginOut(LocalDateTime ldt);

		abstract void writeOut(String text, char suffix) throws IOException;

		abstract void endOut();

		@Override
		public final void write(LocalDateTime ldt, String thread, String type, Throwable messages) {
			beginErr(ldt);
			// 2021-02-02 15:17:17 main INFO BOOT
			try {
				writeErr(LogSetting.DATETIME_FORMATTER.format(ldt), LogSetting.TAB);
				writeErr(thread, LogSetting.TAB);
				writeErr(type, LogSetting.TAB);
				if (messages == null) {
					writeErr(NULL, LogSetting.LINE);
				} else {
					if (messages.getLocalizedMessage() == null) {
						writeErr(messages.getClass().getName(), LogSetting.LINE);
					} else {
						writeErr(messages.getClass().getName(), ':');
						writeErr(messages.getLocalizedMessage(), LogSetting.LINE);
					}

					// com.foo.loader/foo@9.0/com.foo.Main.run(Main.java:101)
					StackTraceElement element;
					final StackTraceElement[] elements = messages.getStackTrace();
					for (int index = 0; index < elements.length; index++) {
						element = elements[index];
						writeErr(LogSetting.INDENT, LogSetting.TAB);
						if (element.getClassLoaderName() != null) {
							writeErr(element.getClassLoaderName(), '/');
						}
						if (element.getModuleName() != null) {
							if (element.getModuleVersion() != null) {
								writeErr(element.getModuleName(), '@');
								writeErr(element.getModuleVersion(), '/');
							} else {
								writeErr(element.getModuleName(), '/');
							}
						}
						writeErr(element.getClassName(), '.');
						writeErr(element.getMethodName(), '(');
						if (element.isNativeMethod()) {
							writeErr("Native Method)", LogSetting.LINE);
						} else if (element.getFileName() != null) {
							if (element.getLineNumber() >= 0) {
								writeErr(element.getFileName(), ':');
								writeErr(Integer.toString(element.getLineNumber()), ')');
								writeErr(EMPTY, LogSetting.LINE);
							} else {
								writeErr(element.getFileName(), ')');
								writeErr(EMPTY, LogSetting.LINE);
							}
						} else {
							writeErr("Unknown Source)", LogSetting.LINE);
						}
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				endErr();
			}
		}

		abstract void beginErr(LocalDateTime ldt);

		abstract void writeErr(String text, char suffix) throws IOException;

		abstract void endErr();
	}
}
