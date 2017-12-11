package net.bleujin.rcraken.expression;

import net.bleujin.rcraken.CommonNode;

public final class NullExpression implements Expression {
	private NullExpression() {
	}

	public static final NullExpression instance = new NullExpression();

	@Override
	public Comparable value(CommonNode node) {
		return null;
	}


}
