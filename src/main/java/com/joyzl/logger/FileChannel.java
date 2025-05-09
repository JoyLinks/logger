/*
 * www.joyzl.com
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.logger;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.logger.LogChannel.LogTextChannel;

/**
 * 文件日志输出
 * 
 * <p>
 * 每天生成一份文件，自动按日期命名
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年1月28日
 */
public final class FileChannel extends LogTextChannel {

	private LocalDateTime outTime;
	private FileOutputStream outStream;
	private final CharBuffer outChars = CharBuffer.allocate(1024);
	private final ByteBuffer outBytes = ByteBuffer.allocate(2048);
	private final CharsetEncoder outEncoder = LogSetting.CHARSET.newEncoder();
	private final ReentrantLock outLock = new ReentrantLock();

	private LocalDateTime errTime;
	private FileOutputStream errStream;
	private final CharBuffer errChars = CharBuffer.allocate(1024);
	private final ByteBuffer errBytes = ByteBuffer.allocate(2048);
	private final CharsetEncoder errEncoder = LogSetting.CHARSET.newEncoder();
	private final ReentrantLock errLock = new ReentrantLock();

	protected final static String PATH;
	static {
		File dir = new File("");
		PATH = dir.getAbsolutePath();
	}

	public FileChannel() {
		ShutdownHook.register(new Closeable() {
			@Override
			public void close() throws IOException {
				try {
					if (outStream != null) {
						outLock.lock();
						try {
							outStream.flush();
							outStream.close();
							outStream = null;
						} finally {
							outLock.unlock();
						}
					}
					if (errStream != null) {
						errLock.lock();
						try {
							errStream.flush();
							errStream.close();
							errStream = null;
						} finally {
							errLock.unlock();
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

			}
		});
	}

	@Override
	void beginOut(LocalDateTime ldt) {
		outLock.lock();

		if (outTime == null || outTime.getDayOfMonth() != ldt.getDayOfMonth() || outTime.getMonthValue() != ldt.getMonthValue() || outTime.getYear() != ldt.getYear()) {
			outTime = ldt;

			Path path = Paths.get(PATH, LogSetting.FOLDER, LogSetting.FILE_NAME_MESSAGE + LogSetting.FILENAME_FORMATTER.format(outTime) + LogSetting.FILE_EXTENSION);
			File file = path.getParent().toFile();
			if (file.exists()) {
				// 日志文件夹存在
			} else {
				file.mkdirs();
			}
			file = path.toFile();
			try {
				if (outStream != null) {
					outStream.flush();
					outStream.close();
					outStream = null;
				}
				outStream = new FileOutputStream(file, true);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	void writeOut(String text, char suffix) throws IOException {
		int size = 0;
		int length = text.length();
		while (outChars.remaining() <= length - size) {
			while (outChars.hasRemaining()) {
				outChars.put(text.charAt(size++));
			}
			write(outChars.flip(), outBytes, outEncoder, outStream);
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
		try {
			if (outChars.flip().hasRemaining()) {
				write(outChars, outBytes, outEncoder, outStream);
			} else {
				outChars.clear();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			outLock.unlock();
		}
	}

	@Override
	void beginErr(LocalDateTime ldt) {
		errLock.lock();

		if (errTime == null || errTime.getDayOfMonth() != ldt.getDayOfMonth() || errTime.getMonthValue() != ldt.getMonthValue() || errTime.getYear() != ldt.getYear()) {
			errTime = ldt;

			Path path = Paths.get(PATH, LogSetting.FOLDER, LogSetting.FILE_NAME_ERROR + LogSetting.FILENAME_FORMATTER.format(errTime) + LogSetting.FILE_EXTENSION);
			File file = path.getParent().toFile();
			if (file.exists()) {
				// 日志文件夹存在
			} else {
				file.mkdirs();
			}
			file = path.toFile();
			try {
				if (errStream != null) {
					errStream.flush();
					errStream.close();
					errStream = null;
				}
				errStream = new FileOutputStream(file, true);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	void writeErr(String text, char suffix) throws IOException {
		int size = 0;
		int length = text.length();
		while (errChars.remaining() <= length - size) {
			while (errChars.hasRemaining()) {
				errChars.put(text.charAt(size++));
			}
			write(errChars.flip(), errBytes, errEncoder, errStream);
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
		try {
			if (errChars.flip().hasRemaining()) {
				write(errChars, errBytes, errEncoder, errStream);
			} else {
				errChars.clear();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			errLock.unlock();
		}
	}

	/**
	 * 将CharBuffer中的字符转码后写入文件
	 * 
	 * @param chars CharBuffer并且执行过flip()有可读字符，写入后其中的内容会被清空
	 * @param bytes ByteBuffer重复使用的缓存，其中的内容会被清空
	 * @param encoder 字符编码器
	 * @param out 文件输出流
	 * @throws IOException
	 */
	void write(CharBuffer chars, ByteBuffer bytes, CharsetEncoder encoder, FileOutputStream out) throws IOException {
		if (out == null || chars == null || bytes == null) {
			return;
		}
		CoderResult result;
		do {
			result = encoder.reset().encode(chars, bytes.clear(), true);
			bytes.flip();
			while (bytes.hasRemaining()) {
				out.getChannel().write(bytes);
			}
		} while (result == CoderResult.OVERFLOW);
		chars.clear();
	}

}