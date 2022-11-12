package net.bleujin.rcraken;

import java.sql.SQLException;
import java.util.stream.Stream;

import net.bleujin.rcraken.convert.AdNodeRows;
import net.bleujin.rcraken.convert.FieldDefinition;
import net.ion.framework.db.Rows;
import net.ion.framework.parse.gson.JsonArray;

public class ReadStream extends AbstractStream<ReadNode, ReadStream> implements Stream<ReadNode> {

	private ReadSession rsession ;
	
	ReadStream(ReadSession rsession, Stream<ReadNode> stream) {
		super(stream) ;
		this.rsession = rsession ;
	}
	
	public Rows toRows(String expr, FieldDefinition... fds) throws SQLException {
		return new AdNodeRows().init(rsession, this, expr, fds) ;
	}

	public JsonArray toJsonArray() {
		JsonArray result = new JsonArray() ;
		this.forEach(rn -> result.add(rn.toJson()));
		return result ;
	}

}
