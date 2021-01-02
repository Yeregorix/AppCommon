package net.smoofyuniverse.common.util;

import org.junit.jupiter.api.Test;

import static net.smoofyuniverse.common.util.StringUtil.toHexString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilTest {

	@Test
	public void test_toHexString() {
		assertEquals("", toHexString(new byte[0]));
		assertEquals("1c", toHexString(new byte[]{28}));
		assertEquals("0ce77f42", toHexString(new byte[]{12, -25, 127, 66}));
		assertEquals("882041384130fb00fa", toHexString(new byte[]{-120, 32, 65, 56, 65, 48, -5, 0, -6}));
		assertEquals("20dc2dcec4", toHexString(new byte[]{32, -36, 45, -50, -60}));
	}
}
