/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger.common;

/**
 * 可选其它
 * 
 * @author ZhangXi 2024年12月4日
 */
public class MessageOther extends OptionalField {

	private Object value;

	public MessageOther(int vendor, int tag) {
		super(tag, vendor);
	}

	@Override
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String getName() {
		return null;
	}
}