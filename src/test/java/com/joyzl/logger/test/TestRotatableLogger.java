package com.joyzl.logger.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.joyzl.logger.RotatableLogger;
import com.joyzl.logger.RotateFile;

class TestRotatableLogger {

	@Test
	void testDateTime() throws Exception {
		final RotatableLogger logger = new RotatableLogger("", "a", "-", ".log") {
			@Override
			public void close() throws IOException {
			}
		};

		RotateFile file;
		RotateFile[] files;
		final LocalDateTime dt1 = LocalDateTime.parse("2007-12-03T10:15:30");
		final LocalDateTime dt2 = LocalDateTime.parse("2007-12-05T12:15:30");

		file = logger.rotate(dt1.toInstant(ZoneOffset.UTC).toEpochMilli());
		assertEquals(file.path().getFileName().toString(), "a-20071203.log");
		assertTrue(file.begin() < file.end());
		assertEquals(file.end() - file.begin(), 86399999);

		file = logger.rotate(dt2.toInstant(ZoneOffset.UTC).toEpochMilli());
		assertEquals(file.path().getFileName().toString(), "a-20071205.log");
		assertTrue(file.begin() < file.end());
		assertEquals(file.end() - file.begin(), 86399999);

		files = logger.rotates(dt1, dt1);
		assertEquals(files.length, 1);
		file = files[0];
		assertEquals(file.path().getFileName().toString(), "a-20071203.log");
		assertEquals(file.begin(), file.end());

		files = logger.rotates(dt1, null);
		assertEquals(files.length, 1);
		file = files[0];
		assertEquals(file.path().getFileName().toString(), "a-20071203.log");
		assertTrue(file.begin() < file.end());

		files = logger.rotates(null, dt1);
		assertEquals(files.length, 1);
		file = files[0];
		assertEquals(file.path().getFileName().toString(), "a-20071203.log");
		assertTrue(file.begin() < file.end());

		files = logger.rotates(dt1, dt2);
		assertEquals(files.length, 3);
		assertEquals(files[0].path().getFileName().toString(), "a-20071203.log");
		assertEquals(files[1].path().getFileName().toString(), "a-20071204.log");
		assertEquals(files[2].path().getFileName().toString(), "a-20071205.log");

		logger.close();
	}

	@Test
	void testDate() throws Exception {
		final RotatableLogger logger = new RotatableLogger("", "a", "-", ".log") {
			@Override
			public void close() throws IOException {
			}
		};

		Path[] files;
		final LocalDate begin = LocalDate.parse("2007-12-03");
		final LocalDate end = LocalDate.parse("2007-12-05");

		files = logger.rotates((LocalDate) null, null);
		assertEquals(files.length, 1);

		files = logger.rotates(begin, null);
		assertEquals(files.length, 1);

		files = logger.rotates(null, end);
		assertEquals(files.length, 1);

		files = logger.rotates(begin, end);
		assertEquals(files.length, 3);
		assertEquals(files[0].getFileName().toString(), "a-20071203.log");
		assertEquals(files[1].getFileName().toString(), "a-20071204.log");
		assertEquals(files[2].getFileName().toString(), "a-20071205.log");

		files = logger.rotates(end, begin);
		assertEquals(files.length, 3);
		assertEquals(files[0].getFileName().toString(), "a-20071203.log");
		assertEquals(files[1].getFileName().toString(), "a-20071204.log");
		assertEquals(files[2].getFileName().toString(), "a-20071205.log");

		logger.close();
	}
}
