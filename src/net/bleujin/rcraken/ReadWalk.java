package net.bleujin.rcraken;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.StreamSupport;

public class ReadWalk implements Iterable<ReadNode> {

	private ReadSession rsession;
	private Fqn source;
	private Collection<String> absPaths;

	public ReadWalk(ReadSession rsession, Fqn source, Collection<String> absPaths) {
		this.rsession = rsession;
		this.source = source;
		this.absPaths = absPaths;
	}

	public Fqn source() {
		return source ;
	}
	
	@Override
	public Iterator<ReadNode> iterator() {
		return absPaths.stream().map(path -> rsession.pathBy(Fqn.from(path))).iterator() ;
	}

	public void debugPrint() {
		stream().forEach(rnode -> rnode.debugPrint());
	}

	public ReadStream stream() {
		return new ReadStream(rsession, StreamSupport.stream(this.spliterator(), false)) ;
	}

	public WriteStream stream(WriteSession wsession) {
		return new WriteWalk(wsession, source, absPaths).stream() ;
	}

}
