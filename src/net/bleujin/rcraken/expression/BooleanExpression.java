package net.bleujin.rcraken.expression;

import org.apache.lucene.search.Query;

import net.bleujin.rcraken.CommonNode;
import net.bleujin.rcraken.convert.Filters;

public class BooleanExpression extends ValueObject implements Expression , ConstantExpression{
	public final Boolean bvalue;

	public BooleanExpression(boolean bvalue) {
		this.bvalue = bvalue;
	}

	@Override
	public Comparable value(CommonNode node) {
		return bvalue;
	}
	
	public Object constantValue(){
		return bvalue ;
	}
	
	@Override
	public Query filter(Op operand, QualifiedNameExpression qne) {
		String field = qne.lastName();
		if( operand == Op.EQ){
			return Filters.eq(qne.lastName(), bvalue.toString()) ;
		} else if (operand == Op.CONTAIN) {
			return Filters.eq(qne.lastName(), bvalue.toString()) ;
		} else if (operand == Op.GT){
			return Filters.gt(field, bvalue.toString()) ;
		} else if (operand == Op.GE) {
			return Filters.gte(field, bvalue.toString()) ;
		} else if (operand == Op.LT) {
			return Filters.lt(field, bvalue.toString()) ;
		} else if (operand == Op.LE){
			return Filters.lte(field, bvalue.toString()) ;
		} else {
			throw new IllegalArgumentException("operand :" + operand) ;
		}
	}
}
