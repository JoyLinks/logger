/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger.common;

/**
 * 可选整个消息
 * 
 * @author ZhangXi 2024年12月4日
 */
public class MessageEntire extends OptionalField {

	private Object entire;

	public MessageEntire() {
		super(TAG_02, VENDOR_DEFAULT);
	}

	public Object getEntire() {
		return entire;
	}

	/**
	 * 可以指定CharSequence和ByteBuffer对象，ByteBuffer将输出为Base64编码
	 */
	public void setEntire(Object value) {
		entire = value;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Object getValue() {
		return entire;
	}
}