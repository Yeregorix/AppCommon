/*
 * Copyright (c) 2017 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.common.app;

import java.util.*;

public final class Arguments {
	private Map<String, String> flags;
	private List<String> args;
	
	private String[] initialArgs;
	
	public Arguments(Map<String, String> flags, List<String> args) {
		this.flags = flags;
		this.args = args;
	}
	
	public static Arguments parse(String[] rawArgs) {
		Map<String, String> flags = new HashMap<>();
		List<String> args = new ArrayList<>();

		String key = null;
		for (String arg : rawArgs) {
			if (arg.startsWith("--")) {
				if (key != null)
					flags.put(key, "");
				key = arg.substring(2).toLowerCase();
			} else {
				if (key == null)
					args.add(arg);
				else {
					flags.put(key, arg);
					key = null;
				}
			}
		}

		if (key != null)
			flags.put(key, "");

		Arguments a = new Arguments(flags, args);
		a.initialArgs = rawArgs;
		return a;
	}

	public int getIntFlag(int defaultV, String... keys) {
		for (String key : keys) {
			String v = this.flags.get(key.toLowerCase());
			if (v != null)
				try {
					return Integer.parseInt(v);
				} catch (NumberFormatException ignored) {
				}
		}
		return defaultV;
	}

	public Optional<String> getFlag(String... keys) {
		for (String key : keys) {
			String v = this.flags.get(key.toLowerCase());
			if (v != null)
				return Optional.of(v);
		}
		return Optional.empty();
	}

	public Optional<String> getArgument(int index) {
		if (index < 0 || index >= this.args.size())
			return Optional.empty();
		return Optional.of(this.args.get(index));
	}

	public String[] getInitialArguments() {
		return this.initialArgs;
	}
}
