/*
 * www.joyzl.com
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.logger;

import java.io.File;
import java.time.LocalTime;

/**
 * 清理超过指定时间的日志文件
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2022年3月3日
 */
public final class FileCleaner extends Thread {

	private static FileCleaner cleaner;

	public final static void initialize() {
		if (LogSetting.EXPIRES > 0) {
			cleaner = new FileCleaner();
			cleaner.start();
		}
	}

	public final static void destory() {
		if (cleaner != null) {
			cleaner.interrupt();
			cleaner = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////////

	private long time;
	private long space;
	private long expires;
	private int count;

	FileCleaner() {
		super("CLEANER");
	}

	@Override
	public void run() {
		try {
			// 启动时执行一次
			clean();

			int tick = 0;
			while (true) {
				// 等待到每日凌晨执行
				tick = LocalTime.now().toSecondOfDay();
				tick = (24 * 60 * 60) - tick;
				tick = tick * 1000;
				sleep(tick);
				clean();
			}
		} catch (InterruptedException e) {
		}
	}

	void clean() {
		space = 0;
		count = 0;
		expires = LogSetting.EXPIRES * 24L * 60 * 60 * 1000;
		time = System.currentTimeMillis();
		try {
			final File directory = new File(FileChannel.PATH);
			clean(directory.listFiles());
		} catch (Exception e) {
			Logger.error(e);
		}

		time = System.currentTimeMillis() - time;
		Logger.info("CLEAN Log ", Long.toString(count), " files ", byteSizeText(space), " space ", Long.toString(time),
				" ms.");
	}

	void clean(File[] files) {
		File file;
		long length;
		for (int index = 0; index < files.length; index++) {
			file = files[index];
			if (file.isFile()) {
				if (file.getPath().endsWith(LogSetting.FILE_EXTENSION)) {
					if (time - file.lastModified() > expires) {
						length = file.length();
						if (file.delete()) {
							count++;
							space += length;
						}
					}
				}
			} else {
				clean(file.listFiles());
			}
		}
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
			return Math.floor(size / (1024.0 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024 * 1024) * 100) / 100
					+ " DB";// 刀字节
		} else {
			return size + "B";
		}

		// Math.floor 向下舍去小数部分
		// 舍去之前先 乘 10或100把要保留的小数变成整数，然后在除10或100获得最终小数
	}

}