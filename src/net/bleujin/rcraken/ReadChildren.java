package net.bleujin.rcraken;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.bleujin.rcraken.convert.AdNodeRows;
import net.bleujin.rcraken.convert.FieldDefinition;
import net.ion.framework.db.Page;
import net.ion.framework.db.Rows;

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
		Iterator<String> citer = childNames.iterator();
		return new Iterator<ReadNode>() {
			@Override
			public boolean hasNext() {
				return citer.hasNext();
			}

			@Override
			public ReadNode next() {
				return rsession.pathBy(Fqn.from(parent, citer.next()));
			}
		};
	}

	public void debugPrint() {
		stream().forEach(rnode -> rnode.debugPrint());
	}

	public StreamChildren stream() {
		return new StreamChildren(rsession, StreamSupport.stream(this.spliterator(), false)) ;
	}

	public ReadSession rsession() {
		return rsession ;
	}

	public long size() {
		return childNames.size();
	}

}
