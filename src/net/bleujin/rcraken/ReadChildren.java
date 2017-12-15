package net.bleujin.rcraken;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.StreamSupport;

public class ReadChildren implements Iterable<ReadNode> {

	private ReadSession rsession;
	private Fqn parent;
	private Set<String> childNames;

	ReadChildren(ReadSession rsession, Fqn parent, Set<String> childNames) {
		this.rsession = rsession;
		this.parent = parent;
		this.childNames = childNames;
	}

	@Override
	public Iterator<ReadNode> iterator() {
		return childNames.stream().map(childName -> rsession.pathBy(Fqn.from(parent, childName))).iterator() ;
	}

	public void debugPrint() {
		stream().forEach(rnode -> rnode.debugPrint());
	}

	public ReadStream stream() {
		return new ReadStream(rsession, StreamSupport.stream(this.spliterator(), false)) ;
	}

	public ReadSession rsession() {
		return rsession ;
	}

	public long size() {
		return childNames.size();
	}

}
