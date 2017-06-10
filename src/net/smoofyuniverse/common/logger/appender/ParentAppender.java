package net.smoofyuniverse.common.logger.appender;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import net.smoofyuniverse.common.logger.core.LogMessage;

public class ParentAppender implements LogAppender {
	private Collection<LogAppender> childs;
	
	public ParentAppender() {
		this(new CopyOnWriteArrayList<>());
	}
	
	public ParentAppender(Collection<LogAppender> childs) {
		this.childs = childs;
	}
	
	public Collection<LogAppender> getChilds() {
		return this.childs;
	}
	
	@Override
	public void append(LogMessage msg) {
		for (LogAppender a : this.childs)
			a.append(msg);
	}
	
	@Override
	public void appendRaw(String msg) {
		for (LogAppender a : this.childs)
			a.appendRaw(msg);
	}

	@Override
	public void close() {
		for (LogAppender a : this.childs)
			a.close();
	}
}
