package net.bleujin.rcraken.expression;

import net.bleujin.rcraken.CommonNode;

public final class BetweenExpression extends ValueObject implements Expression {
	public final Expression expression;
	public final boolean between; // between or not between
	public final Expression from;
	public final Expression to;

	public BetweenExpression(Expression expression, boolean between, Expression from, Expression to) {
		this.expression = expression;
		this.between = between;
		this.from = from;
		this.to = to;
	}
	
	public Comparable value(CommonNode node) {
		return (Boolean)(Op.GE.compute(expression.value(node), from.value(node))) && (Boolean)(Op.LE.compute(expression.value(node), to.value(node))) ; 
	}
	
}

