package net.bleujin.rcraken.store.infinispan;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.infinispan.Cache;
import org.mapdb.HTreeMap;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.Property.PType;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.WriteNode;
import net.bleujin.rcraken.WriteSession;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.SetUtil;
import net.ion.framework.util.StringUtil;

public class ISpanWriteSession extends WriteSession {

	private HTreeMap<String, byte[]> binaryData;
	private ISpanReadSession irs;
	private Cache<String, String> nmap;
	
	protected ISpanWriteSession(ISpanWorkspace wspace, ReadSession rsession, Cache<String, String> nmap) {
		super(wspace, rsession);
		this.irs = (ISpanReadSession)rsession ;
		this.nmap = nmap ;
	}
	
	protected void merge(WriteNode wnode, Fqn fqn, JsonObject data) {
		Fqn current = fqn;
		while (!current.isRoot()) {
			if (!nmap.containsKey(current.absPath())) {
				Set<String> struSet = readStruBy(current.getParent()) ;
				struSet.add(current.name()) ;
				struJson(struSet) ;
				nmap.put(current.getParent().struPath(), StringUtil.join(struSet, '/'));
				nmap.put(current.absPath(), "{}");
			}
			current = current.getParent();
		}
		nmap.put(fqn.absPath(), data.toString());
		
		// handle lob
		wnode.properties().filter(p -> PType.Lob.equals(p.type())&& hasAttribute(p.asString())).forEach(p ->{
			if (attrs().get(p.asString()) instanceof InputStream) {
		        try {
		        	InputStream input = (InputStream) attrs().get(p.asString()) ;
		        	OutputStream output = workspace().outputStream(p.asString());
					IOUtil.copyNClose(input, output);
				} catch (IOException ex) {
					attrs().put(p.asString(), ex.getMessage()) ;
				}
			}
		});
		
	}

	private JsonArray struJson(Set<String> struSet) {
		return new JsonArray().addCollection(struSet);
	}
	
	public ISpanWorkspace workspace() {
		return (ISpanWorkspace)super.workspace() ;
	}
	
	
	protected void removeChild(WriteNode wnode, Fqn fqn, JsonObject data) {
		Set<String> rs = SetUtil.newSet() ;
		decendant(fqn, rs); 

		Set<String> cnames = wnode.childrenNames() ;
		
		rs.forEach(path -> {
			nmap.remove(path); 
			nmap.remove(path + "/");
//			@TODO
//			binaryData.getKeys().forEach( key -> { 
//				if (key.startsWith(path + "$")) {
//					binaryData.remove(key) ;
//				}
//			});
		});
		Set<String> childSet = readStruBy(fqn) ;
		childSet.removeAll(cnames) ;
		nmap.put(fqn.struPath(), StringUtil.join(childSet, '/')) ;
	}

	protected void removeSelf(WriteNode wnode, Fqn fqn, JsonObject data) {
		Set<String> rs = SetUtil.newSet() ;
		decendant(fqn, rs);
		rs.add(fqn.absPath()) ;
		
		rs.forEach(path -> {
			nmap.remove(path); 
			nmap.remove(path + "/"); 
//			binaryData.getKeys().forEach( key -> {
//				if (key.startsWith(path + "$")) {
//					binaryData.remove(key) ;
//				}
//			});
		});
		Fqn parent = fqn.getParent() ;
		if(! fqn.isRoot()) {
			Set<String> childSet = readStruBy(parent) ;
			childSet.remove(fqn.name()) ;
			nmap.put(parent.struPath(), StringUtil.join(childSet, '/')) ;
		}
	}

	void decendant(Fqn parent, Set<String> rs) {
		Set<String> children = readStruBy(parent) ;
		for (String child : children) {
			Fqn childFqn = Fqn.from(parent, child);
			rs.add(childFqn.absPath()) ;
			decendant(childFqn, rs);
		}
	}

	protected Set<String> readStruBy(Fqn fqn) {
		return irs.readStruBy(fqn) ;
	}

	protected JsonObject readDataBy(Fqn fqn) {
		return irs.readDataBy(fqn) ;
	}

	public boolean exist(String path) {
		return irs.exist(path) ;
	}


}
