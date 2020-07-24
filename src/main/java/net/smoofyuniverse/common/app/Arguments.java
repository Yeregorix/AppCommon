/*
 * Copyright (c) 2017-2020 Hugo Dupanloup (Yeregorix)
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

import net.smoofyuniverse.common.util.StringUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

public final class Arguments {
	private final List<String> parameters;
	private final Map<String, String> flags;

	private Arguments(List<String> parameters, Map<String, String> flags) {
		this.parameters = new ArrayList<>(parameters);
		this.flags = new LinkedHashMap<>(flags);
	}

	public int getParametersCount() {
		return this.parameters.size();
	}

	public Optional<String> getParameter(int index) {
		if (index < 0 || index >= this.parameters.size())
			return Optional.empty();
		return Optional.of(this.parameters.get(index));
	}

	public boolean getBoolean(String... keys) {
		Optional<String> opt = getString(keys);
		return opt.isPresent() && !opt.get().toLowerCase(Locale.ROOT).equals("false");
	}

	public Optional<String> getString(String... keys) {
		for (String key : keys) {
			String v = this.flags.get(key.toLowerCase(Locale.ROOT));
			if (v != null)
				return Optional.of(v);
		}
		return Optional.empty();
	}

	public OptionalInt getInt(String... keys) {
		for (String key : keys) {
			String v = this.flags.get(key.toLowerCase(Locale.ROOT));
			if (v != null) {
				try {
					return OptionalInt.of(Integer.parseInt(v));
				} catch (NumberFormatException ignored) {
				}
			}
		}
		return OptionalInt.empty();
	}

	public OptionalLong getLong(String... keys) {
		for (String key : keys) {
			String v = this.flags.get(key.toLowerCase(Locale.ROOT));
			if (v != null) {
				try {
					return OptionalLong.of(Long.parseLong(v));
				} catch (NumberFormatException ignored) {
				}
			}
		}
		return OptionalLong.empty();
	}

	public OptionalDouble getDouble(String... keys) {
		for (String key : keys) {
			String v = this.flags.get(key.toLowerCase(Locale.ROOT));
			if (v != null) {
				try {
					return OptionalDouble.of(Double.parseDouble(v));
				} catch (NumberFormatException ignored) {
				}
			}
		}
		return OptionalDouble.empty();
	}

	@Override
	public String toString() {
		return StringUtil.toCommandLine(export());
	}

	public List<String> export() {
		List<String> l = new ArrayList<>();
		export(l::add);
		return l;
	}

	public void export(Consumer<String> c) {
		for (String p : this.parameters)
			c.accept(p);

		for (Entry<String, String> e : this.flags.entrySet()) {
			c.accept("--" + e.getKey());
			if (!e.getValue().isEmpty())
				c.accept(e.getValue());
		}
	}

	public Builder toBuilder() {
		return new Builder().add(this);
	}

	public static Arguments empty() {
		return new Builder().build();
	}

	public static Arguments parse(String... rawArguments) {
		return new Builder().parse(rawArguments).build();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final List<String> parameters = new ArrayList<>();
		private final Map<String, String> flags = new HashMap<>();

		private Builder() {}

		public Builder reset() {
			this.parameters.clear();
			this.flags.clear();
			return this;
		}

		public Builder addParameter(String value) {
			if (value == null)
				throw new IllegalArgumentException("value");
			this.parameters.add(value);
			return this;
		}

		public Builder removeParameter(String value) {
			if (value == null)
				throw new IllegalArgumentException("value");
			this.parameters.remove(value);
			return this;
		}

		public Builder setFlag(String key, String value) {
			if (key == null)
				throw new IllegalArgumentException("key");
			if (value == null)
				throw new IllegalArgumentException("value");
			this.flags.put(key.toLowerCase(Locale.ROOT), value);
			return this;
		}

		public Builder unsetFlag(String key) {
			if (key == null)
				throw new IllegalArgumentException("key");
			this.flags.remove(key.toLowerCase(Locale.ROOT));
			return this;
		}

		public Builder add(Arguments arguments) {
			this.parameters.addAll(arguments.parameters);
			this.flags.putAll(arguments.flags);
			return this;
		}

		public Builder parse(String... rawArguments) {
			return parse(Arrays.asList(rawArguments));
		}

		public Builder parse(Iterable<String> rawArguments) {
			String key = null;
			int i = 0;

			for (String arg : rawArguments) {
				if (arg == null)
					throw new IllegalArgumentException("argument: " + i);
				i++;

				if (arg.startsWith("--")) {
					if (key != null)
						this.flags.put(key, "");
					key = arg.substring(2).toLowerCase(Locale.ROOT);
				} else {
					if (key == null)
						this.parameters.add(arg);
					else {
						this.flags.put(key, arg);
						key = null;
					}
				}
			}

			if (key != null)
				this.flags.put(key, "");

			return this;
		}

		public Builder parse(String commandLine) {
			return parse(StringUtil.parseCommandLine(commandLine));
		}

		public Arguments build() {
			return new Arguments(this.parameters, this.flags);
		}
	}
}
