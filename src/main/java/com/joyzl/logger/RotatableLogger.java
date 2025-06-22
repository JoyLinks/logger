/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;

/**
 * 可轮换的日志，每天产生单个日志文件，文件名自动按日期轮换"access-20241202.log"
 * 
 * @author ZhangXi 2025年6月9日
 */
public abstract class RotatableLogger implements Closeable {

	/** 用户指定的日志路径，可能是文件或文件夹 */
	private final String file;
	/** 从用户路径分解的日志目录，文件名，扩展名 */
	private final String name, ext;
	private final Path dir;

	/**
	 * @param file 日志文件或目录
	 * @param n 日志文件名
	 * @param s 日志文件名分隔符
	 * @param e 日志文件扩展名
	 * @throws IOException 如果文件位置不可用或无效
	 */
	public RotatableLogger(String file, final String n, final String s, String e) throws IOException {
		this.file = file;

		if (e == null) {
			// 防止无扩展名时输出 "null"
			e = "";
		}

		// 从主文件位置拆分：目录，文件名，扩展名
		// 如果指定的路径是文件，则遵循指定文件名和扩展名
		final Path path = Path.of(file).toAbsolutePath();
		if (Files.exists(path)) {
			if (Files.isDirectory(path)) {
				dir = path;
				name = n + s;
				ext = e;
			} else {
				dir = path.getParent();
				file = path.getFileName().toString();
				int i = file.lastIndexOf('.');
				if (i > 0) {
					name = file.substring(0, i) + s;
					ext = file.substring(i);
				} else {
					name = n + s;
					ext = e;
				}
			}
		} else {
			file = path.getFileName().toString();
			int i = file.lastIndexOf('.');
			if (i > 0) {
				dir = Files.createDirectories(path.getParent());
				name = file.substring(0, i) + s;
				ext = file.substring(i);
			} else {
				dir = Files.createDirectories(path);
				name = n + s;
				ext = e;
			}
		}
	}

	/**
	 * 根据指定时间戳切换文件
	 */
	public RotateFile rotate(long timestamp) {
		// 轮换日志文件，将同一天的日志写入相同文件
		final Instant instant = Instant.ofEpochMilli(timestamp);
		final LocalDate date = LocalDate.ofInstant(instant, ZoneOffset.systemDefault());
		long begin = date.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC) * 1000;
		long end = date.toEpochSecond(LocalTime.MAX, ZoneOffset.UTC) * 1000 + 999;
		return new RotateFile(resolve(date), begin, end);
	}

	/**
	 * 获取指定范围的文件，未检查文件是否存在
	 */
	public Path[] rotates(LocalDate begin, LocalDate end) {
		if (begin == null) {
			if (end == null) {
				return new Path[] { resolve(LocalDate.now()) };
			} else {
				return new Path[] { resolve(end) };
			}
		}
		if (end == null) {
			return new Path[] { resolve(begin) };
		}
		if (begin.isEqual(end)) {
			return new Path[] { resolve(begin) };
		}

		Path paths[];
		int i = (int) ChronoUnit.DAYS.between(begin, end);
		if (i < 0) {
			paths = new Path[i * -1 + 1];
			LocalDate t = begin;
			begin = end;
			end = t;
		} else {
			paths = new Path[i + 1];
		}
		i = 0;
		do {
			paths[i++] = resolve(begin);
			begin = begin.plusDays(1);
		} while (begin.isBefore(end));
		paths[i] = resolve(end);
		return paths;
	}

	/**
	 * 获取指定范围的文件，未检查文件是否存在
	 */
	public RotateFile[] rotates(LocalDateTime begin, LocalDateTime end) {
		Path file;
		long b, e;
		if (begin == null) {
			if (end == null) {
				// 默认当天
				return new RotateFile[] { rotate(System.currentTimeMillis()) };
			} else {
				// 仅结束天
				file = resolve(end);
				e = end.toInstant(ZoneOffset.UTC).toEpochMilli();
				b = e - (e % LoggerBuilder.DAY_MILLISECOND);
				return new RotateFile[] { new RotateFile(file, b, e) };
			}
		} else if (end == null) {
			// 仅开始天
			file = resolve(begin);
			b = begin.toInstant(ZoneOffset.UTC).toEpochMilli();
			e = b + (LoggerBuilder.DAY_MILLISECOND - (b % LoggerBuilder.DAY_MILLISECOND));
			return new RotateFile[] { new RotateFile(file, b, e) };
		}

		int days = (int) begin.toLocalDate().until(end.toLocalDate(), ChronoUnit.DAYS) + 1;
		if (days > 1) {
			final RotateFile[] files = new RotateFile[days];
			days = 0;
			// FIRST
			file = resolve(begin);
			b = begin.toInstant(ZoneOffset.UTC).toEpochMilli();
			e = b + (LoggerBuilder.DAY_MILLISECOND - (b % LoggerBuilder.DAY_MILLISECOND));
			files[days++] = new RotateFile(file, b, e);

			LocalDate date = begin.toLocalDate().plusDays(1);
			for (; days < files.length - 1; days++) {
				file = resolve(date);
				b = LocalDateTime.of(date, LocalTime.MIN).toInstant(ZoneOffset.UTC).toEpochMilli();
				b = LocalDateTime.of(date, LocalTime.MAX).toInstant(ZoneOffset.UTC).toEpochMilli();
				files[days] = new RotateFile(file, b, e);
			}

			// LAST
			file = resolve(end);
			e = end.toInstant(ZoneOffset.UTC).toEpochMilli();
			b = e - (e % LoggerBuilder.DAY_MILLISECOND);
			files[days] = new RotateFile(file, b, e);

			return files;
		} else {
			file = resolve(begin);
			b = begin.toInstant(ZoneOffset.UTC).toEpochMilli();
			e = end.toInstant(ZoneOffset.UTC).toEpochMilli();
			return new RotateFile[] { new RotateFile(file, b, e) };
		}
	}

	private Path resolve(TemporalAccessor t) {
		return dir.resolve(name + DateTimeFormatter.BASIC_ISO_DATE.format(t) + ext);
	}

	/**
	 * 获取日志目录
	 */
	public Path getDirectory() {
		return dir;
	}

	/**
	 * 获取日志文件名，不含扩展名
	 */
	public String getFilename() {
		return name;
	}

	/**
	 * 获取日志文件扩展名
	 */
	public String getFileExtension() {
		return ext;
	}

	/**
	 * 获取用户指定的日志目录和文件
	 */
	public String getFile() {
		return file;
	}
}