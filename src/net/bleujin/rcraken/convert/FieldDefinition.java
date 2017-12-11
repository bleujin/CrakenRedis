package net.bleujin.rcraken.convert;

import net.bleujin.rcraken.CommonNode;
import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.expression.Expression;
import net.bleujin.rcraken.expression.Projection;


public class FieldDefinition {

	private String fieldName;
	private FieldContext fcontext;
	private FieldRender frender;

	public FieldDefinition(String fieldName, FieldRender frender){
		this.fieldName = fieldName ;
		this.frender = frender ;
	}

	public FieldDefinition fieldContext(FieldContext fcontext){
		this.fcontext = fcontext ;
		return this ;
	}

	public Projection createProjection() {
		return new Projection(new Expression(){
			@Override
			public Comparable value(CommonNode node) {
				return (Comparable) frender.render(fcontext, (ReadNode)node) ;
			}
		}, fieldName);
	}

}
