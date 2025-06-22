/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;

/**
 * 清理超过指定时间的日志文件
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2022年3月3日
 */
public final class LoggerCleaner {

	/** 过期天数的毫秒值 */
	private final int expires;
	/** 释放空间(Byte) */
	private long space;
	/** 清理文件数 */
	private int files;
	/** 耗时(毫秒) */
	private long time;

	public LoggerCleaner(int dayExpires) {
		expires = dayExpires * LoggerBuilder.DAY_MILLISECOND;
		space = 0;
		files = 0;
		time = 0;
	}

	public void clean(RotatableLogger logger) {
		clean(logger.getDirectory(), logger.getFileExtension());
	}

	public void clean(Path dir, String ext) {
		if (Files.exists(dir)) {
			long t = System.currentTimeMillis();
			try {
				Files.walkFileTree(dir, Collections.emptySet(), 1, new SimpleFileVisitor<>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						if (t - attrs.creationTime().toMillis() > expires) {
							if (match(file, ext)) {
								try {
									Files.delete(file);
									space += attrs.size();
									files++;
								} catch (IOException e) {
									Logger.error(e);
								}
							}
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException e) {
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (Exception e) {
				Logger.error(e);
			}
			time += System.currentTimeMillis() - t;
		}
	}

	private boolean match(Path file, String ext) {
		file = file.getFileName();
		if (file == null) {
			return false;
		}
		if (ext == null || ext.length() == 0) {
			return file.toString().indexOf('.') < 0;
		}
		return file.toString().endsWith(ext);
	}

	@Override
	public String toString() {
		return "CLEAN:" + files + " file free " + byteSizeText(space) + " " + time + " ms.";
	}

	/**
	 * 获取应清理的过期天数
	 */
	public int getDayExpires() {
		return expires / LoggerBuilder.DAY_MILLISECOND;
	}

	/**
	 * 获取清理累计耗时(毫秒)
	 */
	public long getTime() {
		return time;
	}

	/**
	 * 获取清理的文件数量
	 */
	public int getFiles() {
		return files;
	}

	/**
	 * 获取释放的空间(Byte);
	 */
	public long getSpace() {
		return space;
	}

	/**
	 * 字节数转换为可读文本
	 *
	 * @param size
	 * @return n B/KB/MB/GB/TB/PB/ZB/YB/NB/DB
	 */
	public final static String byteSizeText(long size) {
		if (size < 1024) {
			return size + " B";// 字节
		} else if (size < 1024L * 1024) {
			return Math.floor(size / 1024.0 * 10) / 10 + " KB";// 千字节
		} else if (size < 1024L * 1024 * 1024) {
			return Math.floor(size / (1024.0 * 1024) * 100) / 100 + " MB";// 兆字节
		} else if (size < 1024L * 1024 * 1024 * 1024) {
			return Math.floor(size / (1024.0 * 1024 * 1024) * 100) / 100 + " GB";// 吉字节
		} else if (size < 1024L * 1024 * 1024 * 1024 * 1024) {
			return Math.floor(size / (1024.0 * 1024 * 1024 * 1024) * 100) / 100 + " TB";// 太字节
		} else if (size < 1024L * 1024 * 1024 * 1024 * 1024 * 1024) {
			return Math.floor(size / (1024.0 * 1024 * 1024 * 1024 * 1024) * 100) / 100 + " PB";// 拍字节
		} else if (size < 1024L * 1024 * 1024 * 1024 * 1024 * 1024 * 1024) {
			return Math.floor(size / (1024.0 * 1024 * 1024 * 1024 * 1024 * 1024) * 100) / 100 + " ZB";// 艾字节
		} else if (size < 1024L * 1024 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024) {
			return Math.floor(size / (1024.0 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024) * 100) / 100 + " YB";// 佑字节
		} else if (size < 1024L * 1024 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024) {
			return Math.floor(size / (1024.0 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024) * 100) / 100 + " NB";// 诺字节
		} else if (size < 1024L * 1024 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024) {
			return Math.floor(size / (1024.0 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024) * 100) / 100 + " DB";// 刀字节
		} else {
			return size + "B";
		}

		// Math.floor 向下舍去小数部分
		// 舍去之前先 乘 10或100把要保留的小数变成整数，然后在除10或100获得最终小数
	}
}