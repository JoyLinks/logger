package com.joyzl.logger.common;

import java.util.List;

/**
 * CLF Record
 * 
 * @author ZhangXi 2024年12月2日
 */
public interface CommonRecord extends CommonCodes {

	/** Unix epoch milliseconds */
	long getTimestamp();

	/** 'R':request,'r':response */
	char getType();

	/** O:Original,D:Duplicate,S:Server */
	char getRetransmission();

	/** 's'(sent) 'r'(received) */
	char getDirection();

	/** UDP/TCP/SCTP */
	char getTransport();

	/** E:Encrypted,U:Unencrypted */
	char getEncryption();

	/** sender IP:Port */
	String getSource();

	/** recipient IP:Port */
	String getDestination();

	/** From URI */
	String getFrom();

	/** From TAG */
	String getFromTag();

	/** To URI */
	String getTo();

	/** To TAG */
	String getToTag();

	long getCSeqNumber();

	String getCSeqMethod();

	String getCallId();

	/** Request-URI */
	String getRURI();

	int getStatus();

	String getServerTxn();

	String getClientTxn();

	List<OptionalField> getOptionalFields();
}