package net.bleujin.rcraken.expression;

import org.apache.lucene.search.Query;

public interface ConstantExpression {
	public Query  filter(Op op, QualifiedNameExpression qne) ; 
	public Object constantValue() ;
}
