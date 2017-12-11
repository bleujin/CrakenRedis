package net.bleujin.rcraken.convert;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.RowSetMetaData;

import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.StreamChildren;
import net.bleujin.rcraken.expression.ExpressionParser;
import net.bleujin.rcraken.expression.SelectProjection;
import net.bleujin.rcraken.expression.TerminalParser;
import net.bleujin.rosetta.Parser;
import net.ion.framework.db.RepositoryException;
import net.ion.framework.db.RowsImpl;
import net.ion.framework.db.procedure.Queryable;

public class AdNodeRows extends RowsImpl {

	private static final long serialVersionUID = 6352864291571907347L;

	public AdNodeRows(ReadSession session) throws SQLException {
		super(Queryable.Fake);
	}

	public AdNodeRows init(ReadSession rsession, StreamChildren schildren, String expr, FieldDefinition... fds) throws SQLException {
		SelectProjection projection = makeSelectProjection(expr);

		FieldContext fcontext = new FieldContext();
		projection.add(fcontext, fds);

		populate(rsession, schildren, projection);
		beforeFirst();
		return this;
	}

	private void populate(ReadSession session, StreamChildren schildren, SelectProjection projection) throws SQLException {

		AtomicBoolean isFirst = new AtomicBoolean(true);
		AtomicBoolean isEmpty = new AtomicBoolean(true);
		
		schildren.forEach(rnode -> {
			try {
				if (isFirst.get()) {
					RowSetMetaData meta = makeMetaData(rnode, projection);
					setMetaData(meta);
					appendRow(this, projection, rnode);
					isFirst.set(false);
					isEmpty.set(false);
				} else {
					appendRow(this, projection, rnode);
				}
			} catch (SQLException ex) {
				throw RepositoryException.throwIt(ex);
			}
		});
		
		if (isEmpty.get()) {
			setMetaData(makeMetaData(null, projection));	
		}
		
	}

	private RowSetMetaData makeMetaData(ReadNode node, SelectProjection projection) throws SQLException {
		return projection.getMetaType(node, projection.size());
	}

	private static Parser<SelectProjection> parser = ExpressionParser.selectProjection(); // too long time...

	public  SelectProjection makeSelectProjection(String expr) {
		SelectProjection sp = TerminalParser.parse(parser, expr);
		return sp;
	}

	public void appendRow(AdNodeRows rows, SelectProjection projection, ReadNode rnode) throws SQLException {
		rows.afterLast();
		rows.moveToInsertRow();
		projection.updateObject(rows, rnode);

		rows.insertRow();
		rows.moveToCurrentRow();
	}

}
