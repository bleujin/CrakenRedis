package net.bleujin.rcraken.store;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mapdb.DB;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.mapdb.serializer.GroupSerializerObjectArray;
import org.mapdb.serializer.SerializerUtils;

import net.bleujin.rcraken.CrakenNode;
import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.ReadSession;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.util.SetUtil;

public class MapReadSession extends ReadSession {

	private DB db;
	private HTreeMap<String, String> dataMap;
	private HTreeMap<String, Set<String>> struMap;

	protected MapReadSession(CrakenNode cnode, MapWorkspace wspace, DB db) {
		super(wspace);
		this.db = db;
		this.dataMap = db.hashMap(wspace.nodeMapName()).keySerializer(Serializer.STRING).valueSerializer(Serializer.STRING).createOrOpen() ;
		this.struMap = db.hashMap(wspace.struMapName()).keySerializer(Serializer.STRING).valueSerializer(new SerializerPath()).createOrOpen() ;
	}

	public ReadNode pathBy(String path) {
		Fqn fqn = Fqn.from(path);
		return pathBy(fqn);
	}

	public boolean exist(String path) {
		Fqn fqn = Fqn.from(path);
		return fqn.isRoot() || dataMap.containsKey(fqn.absPath());
	}
	
	protected JsonObject readDataBy(Fqn fqn) {
		String jsonString = dataMap.get(fqn.absPath());
		return JsonObject.fromString(jsonString);
	}

	protected Set<String> readStruBy(Fqn fqn) {
		return struMap.getOrDefault(fqn.absPath(), new HashSet<String>()) ;
	}


}


class SerializerPath extends GroupSerializerObjectArray<Set<String>> {

    @Override
    public void serialize(DataOutput2 out, Set<String> value) throws IOException {
        out.packInt(value.size());
        for (String s : value) {
            out.writeUTF(s);
        }
    }

    @Override
    public Set<String> deserialize(DataInput2 in, int available) throws IOException {
        final int size = in.unpackInt();
        Set<String> ret = new HashSet<String>();
        for (int i = 0; i < size; i++) {
            ret.add(in.readUTF());
        }
        return ret;
    }

    @Override
    public boolean isTrusted() {
        return true;
    }

    @Override
    public boolean equals(Set<String> a1, Set<String> a2) {
        return SetUtil.isEqualSet(a1, a2);
    }

    @Override
    public int hashCode(Set<String> bytes, int seed) {
        for (String i : bytes) {
            seed = (-1640531527) * seed + i.hashCode();
        }
        return seed;
    }

    @Override
    public int compare(Set<String> s1, Set<String> s2) {
        if (s1 == s2) return 0;
        
        String[] o1 = s1.toArray(new String[0]) ;
        String[] o2 = s2.toArray(new String[0]) ;
        final int len = Math.min(s1.size(), s2.size());
        for (int i = 0; i < len; i++) {
            if (o1[i].equals(o2[i]))
                continue;
            return o1[i].compareTo(o2[i]);
        }
        return SerializerUtils.compareInt(o1.length, o2.length);
    }

    @Override
    public Set<String> nextValue(Set<String> svalue) {
    	String[] value = svalue.toArray(new String[0]);
        value = value.clone();

        for (int i = value.length-1; ;i--) {
            String b1 = value[i];
            if(b1==null){
                if(i==0)
                    return null;
                value[i]="";
                continue;
            }
            value[i] = b1+1;
            return new HashSet<String>(Arrays.asList(value));
        }
    }

}