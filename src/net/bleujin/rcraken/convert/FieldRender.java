package net.bleujin.rcraken.convert;

import net.bleujin.rcraken.ReadNode;

public interface FieldRender<T> {

	public abstract T render(FieldContext fcontext, ReadNode current) ; 
}
