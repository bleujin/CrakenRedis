package net.bleujin.rcraken;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class WriteChildren implements Iterable<WriteNode> {

	private WriteSession wsession;
	private Fqn parent;
	private Set<String> childNames;

	WriteChildren(WriteSession wsession, Fqn parent, Set<String> childNames) {
		this.wsession = wsession;
		this.parent = parent;
		this.childNames = childNames;
	}

	@Override
	public Iterator<WriteNode> iterator() {
		return childNames.stream().map(path -> wsession.pathBy(Fqn.from(parent, path))).iterator() ;
	}

	public void debugPrint() {
		stream().forEach(rnode -> rnode.debugPrint());
	}

	public WriteStream stream() {
		return new WriteStream(wsession, StreamSupport.stream(this.spliterator(), false));
	}
	
	public long size() {
		return childNames.size();
	}

}
