package net.bleujin.rcraken.extend;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.ecs.xml.XML;

import com.google.common.base.Function;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.ReadSession;
import net.bleujin.rcraken.StreamChildren;
import net.bleujin.rcraken.WalkReadChildren;
import net.bleujin.rcraken.convert.AdNodeRows;
import net.bleujin.rcraken.expression.ExpressionParser;
import net.bleujin.rcraken.expression.SelectProjection;
import net.bleujin.rosetta.Parser;
import net.ion.framework.db.Rows;
import net.ion.framework.util.Debug;
import net.ion.framework.util.ListUtil;
import net.ion.nsearcher.common.IKeywordField;
import net.ion.nsearcher.common.ReadDocument;
import net.ion.nsearcher.search.SearchRequest;
import net.ion.nsearcher.search.SearchResponse;

public class ChildQueryResponse {

	private SearchResponse response;
	private List<String> found ;
	private ReadSession session ;
	private static Parser<SelectProjection> parser = ExpressionParser.selectProjection();

	public ChildQueryResponse(ReadSession session, SearchResponse response) {
		this.session = session ;
		this.response = response ;
	}

	
	private List<String> found() {
		if (found == null){
			found = ListUtil.newList() ;
			try {
				
				for (ReadDocument doc : response.getDocument()){
					found.add(doc.reserved(IKeywordField.DocKey)) ;
				};
			} catch(IOException ex){
				throw new IllegalStateException(ex); 
			}
		}
		return found ;
	}
	
	
	public static ChildQueryResponse create(ReadSession session, SearchResponse response) {
		return new ChildQueryResponse(session, response);
	}

	public ReadNode first() {
		return found().size() == 0 ? null : toList().get(0);
	}
	
	public List<ReadNode> toList(){
		List<ReadNode> list = ListUtil.newList() ;
		for (String fqn : found()) {
			list.add(session.pathBy(fqn)) ;
		}
		return list ;
	}
	
	public List<Fqn> toFqns(){
		return found().stream().map(Fqn::from).collect(Collectors.toList()) ;
	}

	public int size() {
		return found().size();
	}

	public void debugPrint() throws IOException {
		found().stream().forEach(fqn -> Debug.line(session.pathBy(fqn)));
	}

	public int totalCount() {
		return response.totalCount();
	}
	public long elapsedTime() {
		return response.elapsedTime();
	}
	
	public long startTime() {
		return response.startTime();
	}

	public XML toXML() {
		return response.toXML() ;
	}

	public void awaitPostFuture() throws InterruptedException, ExecutionException {
		response.awaitPostFuture() ;
	}

	public <T> T transformer(Function<ChildQueryResponse, T> function) {
		return function.apply(this) ;
	}

	
	public Rows toRows(String expr) throws SQLException {
		StreamChildren schildren = new WalkReadChildren(session, (Fqn)null, new ArrayList(found())).stream() ;
		return new AdNodeRows(session).init(session, schildren, expr) ;
	}

	public SearchRequest request() {
		return response.request() ;
	}

}
