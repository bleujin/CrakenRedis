package net.bleujin.rcraken.extend.rows;

import net.bleujin.rcraken.ReadNode;

public interface FieldRender<T> {

	public abstract T render(FieldContext fcontext, ReadNode current) ; 
}
