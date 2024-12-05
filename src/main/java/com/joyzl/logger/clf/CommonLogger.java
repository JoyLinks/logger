package com.joyzl.logger.clf;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.joyzl.logger.ShutdownHook;

/**
 * Common Log Format (CLF)
 * 
 * <pre>
 * RFC6872: The Common Log Format (CLF) for the Session Initiation Protocol (SIP):Framework and Information Model
 * RFC6873: Format for the Session Initiation Protocol (SIP) Common Log Format (CLF)
 * </pre>
 * 
 * @author ZhangXi 2024年12月2日
 */
public class CommonLogger {

	private final File file;
	private final CLFFileWriter writer;
	private final CLFFileReader reader;

	public CommonLogger(File file) {
		this.file = file;
		writer = new CLFFileWriter(file);
		reader = new CLFFileReader(file);
		ShutdownHook.register(writer);
	}

	/**
	 * 记录日志到文件，文件名自动切分，当前：access.log，历史：access-2024-12-02.log
	 */
	public void record(CLFRecord record) {
		writer.write(record);
	}

	/**
	 * 搜索日志从文件
	 */
	public List<CLFRecord> search(LocalDateTime begin, LocalDateTime end) throws IOException {
		return reader.search(begin, end);
	}

	public void close() {
		ShutdownHook.remove(writer);
		try {
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public File getFile() {
		return file;
	}
}