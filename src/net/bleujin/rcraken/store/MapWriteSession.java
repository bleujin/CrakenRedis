package net.bleujin.rcraken.store;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.WriteNode;
import net.bleujin.rcraken.WriteSession;
import net.bleujin.rcraken.Property.PType;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.SetUtil;

public class MapWriteSession extends WriteSession {

	private HTreeMap<String, String> dataMap;
	private HTreeMap<String, Set<String>> struMap;
	private HTreeMap<String, byte[]> binaryData;
	private MapWorkspace workspace;
	
	protected MapWriteSession(MapWorkspace wspace, ReadSession rsession, DB db) {
		super(wspace, rsession);
		this.workspace = wspace ;
		this.dataMap = db.hashMap(wspace.nodeMapName()).keySerializer(Serializer.STRING).valueSerializer(Serializer.STRING).createOrOpen() ;
		this.struMap = db.hashMap(wspace.struMapName()).keySerializer(Serializer.STRING).valueSerializer(new SerializerPath()).createOrOpen() ;
		this.binaryData = db.hashMap(wspace.lobMapName()).keySerializer(Serializer.STRING).valueSerializer(Serializer.BYTE_ARRAY).createOrOpen() ;
	}
	
	protected void merge(WriteNode wnode, Fqn fqn, JsonObject data) {
		Fqn current = fqn;
		while (!current.isRoot()) {
			if (!dataMap.containsKey(current.absPath())) {
				Set<String> struSet = readStruBy(current.getParent()) ;
				struSet.add(current.name()) ;
				struMap.put(current.getParent().absPath(), struSet);
				dataMap.put(current.absPath(), "{}");
			}
			current = current.getParent();
		}
		dataMap.put(fqn.absPath(), data.toString());
		
		// handle lob
		wnode.properties().filter(p -> PType.Lob.equals(p.type())&& hasAttribute(p.asString())).forEach(p ->{
			if (attrs().get(p.asString()) instanceof InputStream) {
	        	InputStream input = (InputStream) attrs().get(p.asString()) ;
	        	String targetDir = fqn.getParent().isRoot() ? "" : fqn.getParent().absPath() ;
	        	File dir = new File(workspace.workspaceRootDir(), targetDir) ;
	        	if (!dir.exists()) dir.mkdirs() ;
	        	File targetFile = new File(dir, fqn.name() + "." + p.name()) ; // rootDir/wname/fqn's parent/nodename.pname
	        	try {
					IOUtil.copyNCloseSilent(input, new FileOutputStream(targetFile)) ;
				} catch (IOException ex) {
					attrs().put(p.name(), ex.getMessage()) ;
					throw new IllegalStateException(ex) ;
				}
			}
		});
		
	}
	
	public MapWorkspace workspace() {
		return (MapWorkspace)super.workspace() ;
	}
	
	protected void removeChild(WriteNode wnode, Fqn fqn, JsonObject data) {
		Set<String> rs = SetUtil.newSet() ;
		decendant(fqn, rs); 

		Set<String> cnames = wnode.childrenNames() ;
		
		rs.forEach(path -> {
			dataMap.remove(path); 
			struMap.remove(path);
			binaryData.getKeys().forEach( key -> {
				if (key.startsWith(path + "$")) {
					binaryData.remove(key) ;
				}
			});
		});
		Set<String> childSet = struMap.get(fqn.absPath()) ;
		if (childSet == null) return ; // not exist path
		childSet.removeAll(cnames) ;
		struMap.put(fqn.absPath(), childSet) ;
	}

	protected void removeSelf(WriteNode wnode, Fqn fqn, JsonObject data) {
		Set<String> rs = SetUtil.newSet() ;
		decendant(fqn, rs);
		rs.add(fqn.absPath()) ;
		
		rs.forEach(path -> {
			dataMap.remove(path); 
			struMap.remove(path); 
			binaryData.getKeys().forEach( key -> {
				if (key.startsWith(path + "$")) {
					binaryData.remove(key) ;
				}
			});
		});
		Fqn parent = fqn.getParent() ;
		if(! fqn.isRoot()) {
			Set<String> childSet = struMap.get(parent.absPath()) ;
			childSet.remove(fqn.name()) ;
			struMap.put(parent.absPath(), childSet) ;
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
		return struMap.getOrDefault(fqn.absPath(), new HashSet<String>()) ;
	}

	protected JsonObject readDataBy(Fqn fqn) {
		return JsonObject.fromString(dataMap.get(fqn.absPath()));
	}

	public boolean exist(String path) {
		Fqn fqn = Fqn.from(path);
		return fqn.isRoot() || dataMap.containsKey(fqn.absPath());
	}


}
