/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger.common;

/**
 * Common Log Format (CLF)
 * 
 * <pre>
 * RFC6872: The Common Log Format (CLF) for the Session Initiation Protocol (SIP):Framework and Information Model
 * RFC6873: Format for the Session Initiation Protocol (SIP) Common Log Format (CLF)
 * </pre>
 * 
 * @author ZhangXi 2024年12月2日
 */
public interface CommonCodes {

	final static byte VERSION = 'A';
	final static byte COMMA = ',';
	final static byte DOT = '.';
	final static byte TAB = '\t';
	final static byte CR = '\r';
	final static byte LF = '\n';

	final static byte SPACE = ' ';
	final static byte COLON = ':';
	final static byte MINUS = '-';
	final static byte QUESTION = '?';
	final static byte AT = '@';

	/** type */
	final static char REQUEST = 'R';
	/** type */
	final static char RESPONSE = 'r';
	/** direction */
	final static char SENT = 'S';
	/** direction */
	final static char RECEIVED = 'R';
	/** retransmission */
	final static char ORIGINAL = 'O';
	/** retransmission */
	final static char DUPLICATE = 'D';
	/** retransmission */
	final static char SERVER = 'S';
	/** transport */
	final static char UDP = 'U';
	/** transport */
	final static char TCP = 'T';
	/** transport */
	final static char SCTP = 'S';
	/** encryption */
	final static char ENCRYPTED = 'E';
	/** encryption */
	final static char UNENCRYPTED = 'U';

}