package net.bleujin.rcraken.expression;

import net.bleujin.rcraken.CommonNode;

public final class RelationNameExpression extends ValueObject implements Expression {
	
	public final RelationName qname;

	public RelationNameExpression(RelationName qname) {
		this.qname = qname;
	}

	public static RelationNameExpression of(String... names) {
		return new RelationNameExpression(RelationName.of(names));
	}

	@Override
	public Comparable value(CommonNode node) {
		return null;
	}

}
