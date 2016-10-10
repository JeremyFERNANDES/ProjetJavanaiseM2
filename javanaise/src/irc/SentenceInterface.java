package irc;

import annotation.Lock;
import annotation.LockType;

public interface SentenceInterface {
	
	@Lock(type=LockType.write)
	public void write(String text);
	
	@Lock(type=LockType.read)
	public String read();
}
