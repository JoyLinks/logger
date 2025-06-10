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