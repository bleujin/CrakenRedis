package net.bleujin.rcraken;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class WalkReadChildren implements Iterable<ReadNode> {

	private ReadSession rsession;
	private Fqn source;
	private Collection<String> refFqns;

	WalkReadChildren(ReadSession rsession, Fqn source, Collection<String> refFqns) {
		this.rsession = rsession;
		this.source = source;
		this.refFqns = refFqns;
	}

	public Fqn source() {
		return source ;
	}
	
	@Override
	public Iterator<ReadNode> iterator() {
		Iterator<String> citer = refFqns.iterator();
		return new Iterator<ReadNode>() {
			@Override
			public boolean hasNext() {
				return citer.hasNext();
			}

			@Override
			public ReadNode next() {
				return rsession.pathBy(Fqn.from(citer.next()));
			}
		};
	}

	public void debugPrint() {
		stream().forEach(rnode -> rnode.debugPrint());
	}

	public StreamChildren stream() {
		return new StreamChildren(rsession, StreamSupport.stream(this.spliterator(), false)) ;
	}

}
