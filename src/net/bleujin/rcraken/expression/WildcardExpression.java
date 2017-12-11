package net.bleujin.rcraken.expression;

import net.bleujin.rcraken.CommonNode;

public final class WildcardExpression extends ValueObject implements Expression {
	public final QualifiedName owner;

	public WildcardExpression(QualifiedName owner) {
		this.owner = owner;
	}

	@Override
	public Comparable value(CommonNode node) {
		ComparableSet set = new ComparableSet() ;
		
		return null;
	}
	

}

