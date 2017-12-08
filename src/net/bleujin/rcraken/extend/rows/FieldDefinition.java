package net.bleujin.rcraken.extend.rows;

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

}
