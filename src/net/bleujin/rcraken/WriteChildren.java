package net.bleujin.rcraken;

import java.util.Iterator;
import java.util.Set;
import java.util.Spliterators;
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
		Iterator<String> citer = childNames.iterator();
		return new Iterator<WriteNode>() {
			@Override
			public boolean hasNext() {
				return citer.hasNext();
			}

			@Override
			public WriteNode next() {
				return wsession.pathBy(Fqn.from(parent, citer.next()));
			}
		};
	}

	public void debugPrint() {
		stream().forEach(rnode -> rnode.debugPrint());
	}

	public Stream<WriteNode> stream() {
		return StreamSupport.stream(Spliterators.spliterator(iterator(), childNames.size(), 0), false);
	}

}
