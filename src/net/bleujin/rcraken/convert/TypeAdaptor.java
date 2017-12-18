package net.bleujin.rcraken.convert;

import java.lang.reflect.Field;

import net.bleujin.rcraken.ReadNode;

public abstract class TypeAdaptor<T> {

	public abstract T read(TypeStrategy ts, Field field, ReadNode node)  ;

}



