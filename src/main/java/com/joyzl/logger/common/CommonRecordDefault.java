package com.joyzl.logger.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Common Log Record
 * 
 * @author ZhangXi 2024年12月2日
 */
public class CommonRecordDefault implements CommonRecord {

	/** Unix epoch milliseconds */
	private long timestamp;
	/** 'R':request,'r':response */
	private char type;
	/** 's'(sent) 'r'(received) */
	private char direction;
	/** UDP/TCP/STCP */
	private char transport;
	/** E:Encrypted,U:Unencrypted */
	private char encryption;
	/** O:Original,D:Duplicate,S:Server */
	private char retransmission;
	/** sender IP:Port */
	private String source;
	/** recipient IP:Port */
	private String destination;
	/** From URI */
	private String from;
	/** From TAG */
	private String fromTag;
	/** To URI */
	private String to;
	/** To TAG */
	private String toTag;
	private String callId;
	private String cSeqMethod;
	private long cSeqNumber;
	/** Request-URI */
	private String RURI;
	private int status;
	private String serverTxn;
	private String clientTxn;

	@Override
	public String getClientTxn() {
		return clientTxn;
	}

	public void setClientTxn(String value) {
		clientTxn = value;
	}

	@Override
	public String getServerTxn() {
		return serverTxn;
	}

	public void setServerTxn(String value) {
		serverTxn = value;
	}

	@Override
	public int getStatus() {
		return status;
	}

	public void setStatus(int value) {
		status = value;
	}

	@Override
	public String getRURI() {
		return RURI;
	}

	public void setRURI(String value) {
		RURI = value;
	}

	@Override
	public long getCSeqNumber() {
		return cSeqNumber;
	}

	public void setCSeqNumber(long value) {
		cSeqNumber = value;
	}

	@Override
	public String getCSeqMethod() {
		return cSeqMethod;
	}

	public void setCSeqMethod(String value) {
		cSeqMethod = value;
	}

	@Override
	public String getCallId() {
		return callId;
	}

	public void setCallId(String value) {
		callId = value;
	}

	@Override
	public String getToTag() {
		return toTag;
	}

	public void setToTag(String value) {
		toTag = value;
	}

	@Override
	public String getTo() {
		return to;
	}

	public void setTo(String value) {
		to = value;
	}

	@Override
	public String getFromTag() {
		return fromTag;
	}

	public void setFromTag(String value) {
		fromTag = value;
	}

	@Override
	public String getFrom() {
		return from;
	}

	public void setFrom(String value) {
		from = value;
	}

	@Override
	public String getDestination() {
		return destination;
	}

	public void setDestination(String value) {
		destination = value;
	}

	@Override
	public String getSource() {
		return source;
	}

	public void setSource(String value) {
		source = value;
	}

	@Override
	public char getRetransmission() {
		return retransmission;
	}

	public void setRetransmission(char value) {
		retransmission = value;
	}

	@Override
	public char getEncryption() {
		return encryption;
	}

	public void setEncryption(char value) {
		encryption = value;
	}

	@Override
	public char getTransport() {
		return transport;
	}

	public void setTransport(char value) {
		transport = value;
	}

	@Override
	public char getDirection() {
		return direction;
	}

	public void setDirection(char value) {
		direction = value;
	}

	@Override
	public char getType() {
		return type;
	}

	public void setType(char value) {
		type = value;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long value) {
		timestamp = value;
	}

	private List<OptionalField> optionalFields;

	@Override
	public List<OptionalField> getOptionalFields() {
		return optionalFields;
	}

	public void addOptionalField(OptionalField value) {
		if (optionalFields == null) {
			optionalFields = new ArrayList<>();
		}
		optionalFields.add(value);
	}
}