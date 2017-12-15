package net.bleujin.rcraken;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.StreamSupport;

public class WriteWalk implements Iterable<WriteNode> {

	private WriteSession wsession;
	private Fqn source;
	private Collection<String> refFqns;

	public WriteWalk(WriteSession rsession, Fqn source, Collection<String> refFqns) {
		this.wsession = rsession;
		this.source = source;
		this.refFqns = refFqns;
	}

	public Fqn source() {
		return source ;
	}
	
	@Override
	public Iterator<WriteNode> iterator() {
		return refFqns.stream().map(path -> wsession.pathBy(Fqn.from(path))).iterator() ;
	}

	public void debugPrint() {
		stream().forEach(rnode -> rnode.debugPrint());
	}

	public WriteStream stream() {
		return new WriteStream(wsession, StreamSupport.stream(this.spliterator(), false)) ;
	}


}
