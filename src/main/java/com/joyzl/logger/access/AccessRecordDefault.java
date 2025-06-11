package com.joyzl.logger.access;

import java.net.InetSocketAddress;

public class AccessRecordDefault implements AccessRecord {

	/** 服务端口 */
	private int serverPort;
	/** 请求的客户端地址 */
	private InetSocketAddress remoteAddress;
	/** 请求主机名 */
	private String host;
	/** 请求接收时间戳 */
	private long requestTimestamp;
	/** 请求的方法 */
	private String requestMethod;
	/** 请求的资源路径 */
	private String requestURI;
	/** 请求的版本 */
	private String requestVersion;
	/** 请求体大小 */
	private int requestBodySize;
	/** 处理程序 */
	private String servletName;
	/** 处理用时(毫秒) */
	private int servletSpend;
	/** 响应状态 */
	private int responseStatus;
	/** 响应体大小 */
	private int responseBodySize;

	@Override
	public int getServerPort() {
		return serverPort;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public long getRequestTimestamp() {
		return requestTimestamp;
	}

	@Override
	public String getRequestMethod() {
		return requestMethod;
	}

	@Override
	public String getRequestURI() {
		return requestURI;
	}

	@Override
	public String getRequestVersion() {
		return requestVersion;
	}

	@Override
	public int getRequestBodySize() {
		return requestBodySize;
	}

	@Override
	public String getServletName() {
		return servletName;
	}

	@Override
	public int getServletSpend() {
		return servletSpend;
	}

	@Override
	public int getResponseStatus() {
		return responseStatus;
	}

	@Override
	public int getResponseBodySize() {
		return responseBodySize;
	}

	/** @see #getServerPort() */
	public void setServerPort(int value) {
		serverPort = value;
	}

	/** @see #getRemoteAddress() */
	public void setRemoteAddress(InetSocketAddress value) {
		remoteAddress = value;
	}

	/** @see #getHost() */
	public void setHost(String value) {
		host = value;
	}

	/** @see #getRequestTimestamp() */
	public void setRequestTimestamp(long value) {
		requestTimestamp = value;
	}

	/** @see #getRequestMethod() */
	public void setRequestMethod(String value) {
		requestMethod = value;
	}

	/** @see #getRequestURI() */
	public void setRequestURI(String value) {
		requestURI = value;
	}

	/** @see #getRequestVersion() */
	public void setRequestVersion(String value) {
		requestVersion = value;
	}

	/** @see #getRequestBodySize() */
	public void setRequestBodySize(int value) {
		requestBodySize = value;
	}

	/** @see #getServletName() */
	public void setServletName(String value) {
		servletName = value;
	}

	/** @see #getServletSpend() */
	public void setServletSpend(int value) {
		servletSpend = value;
	}

	/** @see #getResponseStatus() */
	public void setResponseStatus(int value) {
		responseStatus = value;
	}

	/** @see #getResponseBodySize() */
	public void setResponseBodySize(int value) {
		responseBodySize = value;
	}
}
