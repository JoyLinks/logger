/*
 * www.joyzl.com
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.logger;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * 日志设置
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年1月28日
 */
public final class LogSetting {

	public final static Charset CHARSET = StandardCharsets.UTF_8;

	public final static String INFOM = "INFOM";
	public final static String DEBUG = "DEBUG";
	public final static String ERROR = "ERROR";

	public static DateTimeFormatter FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
	public static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

	public static String INDENT = "            ";
	public static char TAB = '\t';
	public static char LINE = '\n';

	// 文件相关

	public static String FOLDER = "log";
	public static String FILE_NAME_MESSAGE = "message-";
	public static String FILE_NAME_ERROR = "error-";
	public static String FILE_EXTENSION = ".log";
	/** 日志文件过期时间(天)，0为不过期 */
	public static int EXPIRES = 30;

	// 网络相关

	public static String DUP_BROADCAST_OUT = "255.255.255.255";
	public static String DUP_BROADCAST_ERR = "255.255.255.255";
	public static int DUP_BROADCAST_OUT_PORT = 10001;
	public static int DUP_BROADCAST_ERR_PORT = 10002;

	// 日志级别 ERROR=1,INFO=2,DEBUG=3

	public static int LEVEL = 3;
}