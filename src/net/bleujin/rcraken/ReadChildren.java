package net.bleujin.rcraken;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import com.google.common.base.Predicate;

import net.ion.framework.util.ListUtil;

public class ReadChildren implements Iterable<ReadNode> {

	private ReadSession rsession;
	private Fqn parent;
	private Set<String> childNames;
	private List<Predicate<ReadNode>> filters = ListUtil.newList() ;

	ReadChildren(ReadSession rsession, Fqn parent, Set<String> childNames) {
		this.rsession = rsession;
		this.parent = parent;
		this.childNames = childNames;
	}

	@Override
	public Iterator<ReadNode> iterator() {
		return childNames.stream().map(childName -> rsession.pathBy(Fqn.from(parent, childName))).iterator() ;
		// return readChildren().iterator() ;
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
