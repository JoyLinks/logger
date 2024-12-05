package com.joyzl.logger.clf;

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