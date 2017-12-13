package net.bleujin.rcraken.expression;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.lucene.search.Filter;

import net.bleujin.rcraken.CommonNode;
import net.bleujin.rcraken.convert.Filters;

public final class UnaryExpression extends ValueObject implements Expression {
	public final Expression operand;
	public final Op operator;

	public UnaryExpression(Op operator, Expression operand) {
		this.operand = operand;
		this.operator = operator;
	}

	@Override
	public Comparable value(CommonNode node) {
		return operator.compute(operand.value(node));
	}
	
	
	public Filter filter(){
		try {
			Filter filter = (Filter) MethodUtils.invokeMethod(operand, "filter", new Object[0]);
			return Filters.not(filter) ;
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("can't make filter : " + operand) ;
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("can't make filter : " + operand) ;
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("can't make filter : " + operand) ;
		}
	}

}