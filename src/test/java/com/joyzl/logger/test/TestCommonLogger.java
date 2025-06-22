/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.logger.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.joyzl.logger.common.CommonCodes;
import com.joyzl.logger.common.CommonLogger;
import com.joyzl.logger.common.CommonRecord;
import com.joyzl.logger.common.CommonRecordDefault;
import com.joyzl.logger.common.MessageBody;
import com.joyzl.logger.common.MessageHeader;
import com.joyzl.logger.common.MessageOther;
import com.joyzl.logger.common.OptionalField;

class TestCommonLogger {

	@Test
	void testFormat() throws IOException {
		final CommonLogger logger = new CommonLogger("common.log");
		final CommonRecordDefault record = new CommonRecordDefault();
		record.setTimestamp(1328821153010L);
		record.setType(CommonCodes.REQUEST);
		record.setRetransmission(CommonCodes.ORIGINAL);
		record.setDirection(CommonCodes.RECEIVED);
		record.setTransport(CommonCodes.UDP);
		record.setEncryption(CommonCodes.UNENCRYPTED);
		record.setCSeqNumber(1);
		record.setCSeqMethod("INVITE");
		record.setStatus(0);
		record.setRURI("sip:192.0.2.10");
		record.setDestination("192.0.2.10:5060");
		record.setSource("192.0.2.200:56485");
		record.setTo("sip:192.0.2.10");
		record.setToTag("");
		record.setFrom("sip:1001@example.com:5060");
		record.setFromTag("DL88360fa5fc");
		record.setCallId("DL70dff590c1-1079051554@example.com");
		record.setServerTxn("S1781761-88");
		record.setClientTxn("C67651-11");

		final MessageHeader optionalField1 = new MessageHeader();
		optionalField1.setName("Contact");
		optionalField1.setValue("<sip:bob@192.0.2.4>");
		record.addOptionalField(optionalField1);

		final MessageHeader optionalField2 = new MessageHeader();
		optionalField2.setName(MessageHeader.Reason_Phrase);
		optionalField2.setValue("Ringing");
		record.addOptionalField(optionalField2);

		final MessageBody optionalField3 = new MessageBody();
		optionalField3.setContentType("application/sdp");
		optionalField3.setValue("v=0\r\no=alice 2890844526 2890844526 IN IP4 host.example.com\r\ns=-\r\nc=IN IP4 host.example.com\r\nt=0 0\r\nm=audio 49170 RTP/AVP 0 8 97\r\n");
		record.addOptionalField(optionalField3);

		final MessageBody optionalField4 = new MessageBody();
		optionalField4.setContentType("multipart/mixed;boundary=7a9cbec02ceef655");
		byte[] data = Base64.getMimeDecoder().decode("MIIBUgYJKoZIhvcNAQcCoIIBQzCCAT8CAQExCTAHBgUrDgMCGjALBgkqhkiG9w0BBwExggEgMIIB\r\nHAIBATB8MHAxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMREwDwYDVQQHEwhTYW4g\r\nSm9zZTEOMAwGA1UEChMFc2lwaXQxKTAnBgNVBAsTIFNpcGl0IFRlc3QgQ2VydGlmaWNhdGUgQXV0\r\naG9yaXR5AggBlQBxAjMBEzAHBgUrDgMCGjANBgkqhkiG9w0BAQEFAASBgI70ZvlI8FIt0uWXjp2V\r\nquny/hWgZllxYpLo2iqo2DUKaM7/rjy9K/8Wdd3VZI5ZPdZHKPJiIPfpQXSeMw2aFe2r25PRDEIQ\r\nLntyidKcwMmuLvvHwM/5Fy87An5PwCfhVG3ktqo6uz5mzMtd1sZLg4MUnLjm/xgtlE/le2W8mdAF\r\n");
		optionalField4.setValue(ByteBuffer.wrap(data));
		record.addOptionalField(optionalField4);

		final MessageOther optionalField5 = new MessageOther(32473, 3);
		optionalField5.setValue("a=rtpmap:0 PCMU/8000");
		record.addOptionalField(optionalField5);

		final MessageOther optionalField6 = new MessageOther(32473, 7);
		optionalField6.setValue("1877 example.com");
		record.addOptionalField(optionalField6);

		logger.record(record);

		/*-
		 * EXAMPLE RFC6873
		 * 
		 * A000100,0053005C005E006D007D008F009E00A000BA00C700EB00F70100
		 * 1328821153.010	RORUU	1 INVITE	-	sip:192.0.2.10	192.0.2.10:5060	192.0.2.200:56485	sip:192.0.2.10	-	sip:1001@example.com:5060	DL88360fa5fc	DL70dff590c1-1079051554@example.com	S1781761-88	C67651-11
		 * 
		 * TEST RESULT
		 * 
		 * A000100,0053005C005E006D007D008F009E00A000BA00C700EB00F70100
		 * 1328821153.010	RORUU	1 INVITE	-	sip:192.0.2.10	192.0.2.10:5060	192.0.2.200:56485	sip:192.0.2.10	-	sip:1001@example.com:5060	DL88360fa5fc	DL70dff590c1-1079051554@example.com	S1781761-88	C67651-11
		 * 
		 * 00@00000000,001D,00,Contact: <sip:bob@192.0.2.4>
		 * 00@00000000,0017,00,Reason-Phrase: Ringing
		 * 01@00000000,00AA,00,application/sdp v=0%0D%0Ao=alice 2890844526 2890844526 IN IP4 host.example.com%0D%0As=-%0D%0Ac=IN IP4 host.example.com%0D%0At=0 0%0D%0Am=audio 49170 RTP/AVP 0 8 97%0D%0A
		 * 01@00000000,002C,01,multipart/mixed;boundary=7a9cbec02ceef655 ?
		 *
		 */

		OptionalField o;
		final LocalDate date = LocalDate.ofInstant(Instant.ofEpochMilli(record.getTimestamp()), ZoneOffset.UTC);
		final List<CommonRecord> records = logger.search(date.atTime(LocalTime.MIN), date.atTime(LocalTime.MAX));
		assertTrue(records.size() > 0);
		for (CommonRecord r : records) {
			assertEquals(r.getTimestamp(), record.getTimestamp());
			assertEquals(r.getType(), record.getType());
			assertEquals(r.getRetransmission(), record.getRetransmission());
			assertEquals(r.getDirection(), record.getDirection());
			assertEquals(r.getTransport(), record.getTransport());
			assertEquals(r.getEncryption(), record.getEncryption());
			assertEquals(r.getCSeqNumber(), record.getCSeqNumber());
			assertEquals(r.getCSeqMethod(), record.getCSeqMethod());
			assertEquals(r.getStatus(), record.getStatus());
			assertEquals(r.getRURI(), record.getRURI());
			assertEquals(r.getDestination(), record.getDestination());
			assertEquals(r.getSource(), record.getSource());
			assertEquals(r.getTo(), record.getTo());
			assertEquals(r.getToTag(), record.getToTag());
			assertEquals(r.getFrom(), record.getFrom());
			assertEquals(r.getFromTag(), record.getFromTag());
			assertEquals(r.getCallId(), record.getCallId());
			assertEquals(r.getServerTxn(), record.getServerTxn());
			assertEquals(r.getClientTxn(), record.getClientTxn());

			assertEquals(r.getOptionalFields().size(), 6);

			o = r.getOptionalFields().get(0);
			assertEquals(o.getName(), optionalField1.getName());
			assertEquals(o.getValue(), optionalField1.getValue());

			o = r.getOptionalFields().get(1);
			assertEquals(o.getName(), optionalField2.getName());
			assertEquals(o.getValue(), optionalField2.getValue());

			o = r.getOptionalFields().get(2);
			assertEquals(o.getName(), optionalField3.getName());
			assertEquals(o.getValue(), optionalField3.getValue());

			o = r.getOptionalFields().get(3);
			assertEquals(o.getName(), optionalField4.getName());
			assertEquals(o.getValue(), optionalField4.getValue());

			o = r.getOptionalFields().get(4);
			assertEquals(o.getName(), optionalField5.getName());
			assertEquals(o.getValue(), optionalField5.getValue());

			o = r.getOptionalFields().get(5);
			assertEquals(o.getName(), optionalField6.getName());
			assertEquals(o.getValue(), optionalField6.getValue());
		}

		logger.close();
	}
}