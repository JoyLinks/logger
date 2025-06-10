package com.joyzl.logger;

import java.nio.file.Path;

/**
 * 日志文件，包含轮换判断所需的时间戳
 * 
 * @author ZhangXi 2025年6月9日
 */
public class RotateFile {
	private final Path file;
	private final long begin, end;

	public final static RotateFile EMPTY = new RotateFile(null, 0, 0);

	public RotateFile(Path file, long begin, long end) {
		this.file = file;
		this.begin = begin;
		this.end = end;
	}

	@Override
	public String toString() {
		return file.toString();
	}

	public boolean beyond(long timestamp) {
		return timestamp < begin || timestamp > end;
	}

	public Path path() {
		return file;
	}

	public long end() {
		return end;
	}

	public long begin() {
		return begin;
	}
}