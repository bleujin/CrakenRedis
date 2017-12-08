package net.bleujin.rcraken.extend;

import java.util.List;
import java.util.stream.Stream;

import javax.sql.RowSet;

import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.extend.rows.FieldDefinition;
import net.ion.framework.db.Page;
import net.ion.framework.db.Rows;
import net.ion.framework.util.ListUtil;

public class CRows {

	private Stream<ReadNode> stream;
	private Page page;
	private List<FieldDefinition> cols = ListUtil.newList() ;
	
	public CRows(Stream<ReadNode> stream) {
		this.stream = stream ;
	}

	public static CRows create(Stream<ReadNode> stream) {
		return new CRows(stream);
	}

	public CRows page(Page page) {
		this.page = page ;
		return this;
	}

	public CRows columns(String... columns) {
		
		return this ;
	}

	public Rows toRows(String fields, FieldDefinition fieldDefinition) {
		return null;
	}

}
