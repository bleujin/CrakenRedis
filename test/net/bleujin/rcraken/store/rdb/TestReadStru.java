package net.bleujin.rcraken.store.rdb;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.plaf.ListUI;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.Fqn;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.SetUtil;

public class TestReadStru {
	
	@Test
	public void readStru() {
		
		Fqn a = Fqn.from("/a") ;
		Fqn c = Fqn.from("/a/b/c") ;
		Fqn e = Fqn.from("/a/b/c/d/e") ;
		Fqn f = Fqn.from("/a/b/c/d/g") ;
		
		List<Fqn> list = ListUtil.toList(c,e,f) ;
		
		SortedSet<String> childs = new TreeSet<String>();
		list.stream().forEach(fqn ->{
			Fqn current = fqn ;
			do{
				// childs.add(current.absPath()) ; 
				childs.add( current.getSubFqn(1, current.size()).absPath().substring(1) ) ;

				current = current.getParent() ;
			} while (! current.equals(a)) ;
		}) ;
		
		Debug.line(childs);
		
	}

}
