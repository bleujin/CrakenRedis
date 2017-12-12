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

	public WalkReadChildren(ReadSession rsession, Fqn source, Collection<String> refFqns) {
		this.rsession = rsession;
		this.source = source;
		this.refFqns = refFqns;
	}

	public Fqn source() {
		return source ;
	}
	
	@Override
	public Iterator<ReadNode> iterator() {
		return refFqns.stream().map(path -> rsession.pathBy(Fqn.from(path))).iterator() ;
	}

	public void debugPrint() {
		stream().forEach(rnode -> rnode.debugPrint());
	}

	public StreamChildren stream() {
		return new StreamChildren(rsession, StreamSupport.stream(this.spliterator(), false)) ;
	}

}
