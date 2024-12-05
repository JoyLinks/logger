package com.joyzl.logger.clf;

/**
 * 可选字段
 * 
 * @author ZhangXi 2024年12月3日
 */
public abstract class OptionalField {

	/** DEFAULT VENDOR 0 */
	public final static int VENDOR_DEFAULT = 00000000;
	/** Header Field or Reason-Phrase */
	public final static int TAG_00 = 00;
	/** Message body */
	public final static int TAG_01 = 01;
	/** Entire message */
	public final static int TAG_02 = 02;

	private final int tag;
	private final int vendor;

	public OptionalField(int tag, int vendor) {
		this.vendor = vendor;
		this.tag = tag;
	}

	public int getVendor() {
		return vendor;
	}

	public int getTag() {
		return tag;
	}

	/**
	 * Header name / ContentType
	 */
	public abstract String getName();

	/**
	 * 可以指定CharSequence和ByteBuffer对象，ByteBuffer将输出为Base64编码
	 */
	public abstract Object getValue();

	protected boolean isCharSequence() {
		return getValue() instanceof CharSequence;
	}
}