/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
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

	/** 时间戳不属于当前文件范围 */
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