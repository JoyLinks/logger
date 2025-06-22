/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger.common;

/**
 * 可选标头
 * 
 * @author ZhangXi 2024年12月4日
 */
public class MessageHeader extends OptionalField {

	/** Status Text */
	public final static String Reason_Phrase = "Reason-Phrase";

	private String name;
	private String value;

	public MessageHeader() {
		super(TAG_00, VENDOR_DEFAULT);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}