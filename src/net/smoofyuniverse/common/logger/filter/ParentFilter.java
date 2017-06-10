package net.smoofyuniverse.common.logger.filter;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import net.smoofyuniverse.common.logger.core.LogMessage;

public class ParentFilter implements LogFilter {
	private Collection<LogFilter> childs;
	private boolean dominantValue = false;
	
	public ParentFilter() {
		this(new CopyOnWriteArrayList<>());
	}
	
	public ParentFilter(Collection<LogFilter> childs) {
		this.childs = childs;
	}
	
	public Collection<LogFilter> getChilds() {
		return this.childs;
	}
	
	public boolean getDominantValue() {
		return this.dominantValue;
	}
	
	public void setDominantValue(boolean v) {
		this.dominantValue = v;
	}

	@Override
	public boolean allow(LogMessage msg) {
		for (LogFilter f : this.childs) {
			if (f.allow(msg) == this.dominantValue)
				return this.dominantValue;
		}
		return !this.dominantValue;
	}
}
