package com.joyzl.logger.clf;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
public interface CLFCoder {

	byte VERSION = 'A';
	byte COMMA = ',';
	byte DOT = '.';
	byte TAB = '\t';
	byte CR = '\r';
	byte LF = '\n';

	byte SPACE = ' ';
	byte COLON = ':';
	byte MINUS = '-';
	byte QUESTION = '?';
	byte AT = '@';

	/** type */
	char REQUEST = 'R';
	/** type */
	char RESPONSE = 'r';
	/** direction */
	char SENT = 'S';
	/** direction */
	char RECEIVED = 'R';
	/** retransmission */
	char ORIGINAL = 'O';
	/** retransmission */
	char DUPLICATE = 'D';
	/** retransmission */
	char SERVER = 'S';
	/** transport */
	char UDP = 'U';
	/** transport */
	char TCP = 'T';
	/** transport */
	char SCTP = 'S';
	/** encryption */
	char ENCRYPTED = 'E';
	/** encryption */
	char UNENCRYPTED = 'U';

	/**
	 * access.log<br>
	 * access-20241202.log
	 */
	static File dateFile(File file, LocalDate date) {
		String name = file.getPath();
		final int index = name.lastIndexOf('.');
		if (index > 0) {
			name = name.substring(0, index) + '-' + date.format(DateTimeFormatter.BASIC_ISO_DATE) + name.substring(index);
		} else {
			name = name + '-' + date.format(DateTimeFormatter.BASIC_ISO_DATE);
		}
		return new File(name);
	}
}