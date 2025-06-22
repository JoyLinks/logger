/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger.common;

/**
 * 可选消息体
 * 
 * @author ZhangXi 2024年12月4日
 */
public class MessageBody extends OptionalField {

	private String contentType;
	private Object body;

	public MessageBody() {
		super(TAG_01, VENDOR_DEFAULT);
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String value) {
		contentType = value;
	}

	public void setValue(Object value) {
		this.body = value;
	}

	@Override
	public Object getValue() {
		return body;
	}

	@Override
	public String getName() {
		return contentType;
	}
}