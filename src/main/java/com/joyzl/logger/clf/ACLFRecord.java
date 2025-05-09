package com.joyzl.logger.clf;

/**
 * Apache Common Log Format
 * 
 * @author ZhangXi 2024年12月12日
 */
public interface ACLFRecord {

	String remoteHost();

	String logname();

	String username();

	String requestMethod();

	String requestUrl();

	String requestVersion();

	int status();

	int bytes();
}