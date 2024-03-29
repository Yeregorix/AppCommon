/*
 * Copyright (c) 2023 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
