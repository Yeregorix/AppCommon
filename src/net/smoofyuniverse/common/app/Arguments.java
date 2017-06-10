package net.smoofyuniverse.common.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Arguments {
	private Map<String, String> flags;
	private List<String> args;
	
	private String[] initialArgs;
	
	public Arguments(Map<String, String> flags, List<String> args) {
		this.flags = flags;
		this.args = args;
	}
	
	public int getIntFlag(int defaultV, String... keys) {
		for (String key : keys) {
			String v = this.flags.get(key.toLowerCase());
			if (v != null)
				try {
					return Integer.parseInt(v);
				} catch (NumberFormatException e) {}
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
}
