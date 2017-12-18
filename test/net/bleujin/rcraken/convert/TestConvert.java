package net.bleujin.rcraken.convert;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import net.bleujin.rcraken.TestBaseCrakenRedis;
import net.ion.framework.util.Debug;

public class TestConvert extends TestBaseCrakenRedis{

	public void testSimple() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/property").property("name", "bleujin").property("male", true).property("birth", Calendar.getInstance()).property("items", Long.MAX_VALUE).property("age", 20L)
				.property("names", "hero", "bleu", "jin").merge() ;
			return null ;
		}) ;
		PriBean bean = rsession.pathBy("/property").toBean(PriBean.class) ;
		
		assertEquals("bleujin", bean.name());
		assertEquals(true, bean.male);
		assertEquals(Calendar.getInstance().get(Calendar.DATE), bean.birth.get(Calendar.DATE));
		assertEquals(Long.MAX_VALUE, bean.items);
		assertEquals(20, bean.age);
		assertEquals("bleu", bean.names[1]);
	}
	
	public void testChild() throws Exception {
		rsession.tran(wsession -> {
			wsession.pathBy("/bleujin").property("name", "bleujin").child("mychild").property("name", "bleujin").property("male", true).property("birth", Calendar.getInstance()).property("items", Long.MAX_VALUE).property("age", 20L)
				.property("names", "hero", "bleu", "jin").merge() ;
			return null ;
		}) ;
		
		ParentBean parent = rsession.pathBy("/bleujin").toBean(ParentBean.class) ;
		assertEquals("bleujin", parent.name());

		PriBean bean = parent.mychild() ;
		assertEquals("bleujin", bean.name());
		assertEquals(true, bean.male);
		assertEquals(Calendar.getInstance().get(Calendar.DATE), bean.birth.get(Calendar.DATE));
		assertEquals(Long.MAX_VALUE, bean.items);
		assertEquals(20, bean.age);
		assertEquals("bleu", bean.names[1]);

	}
	
	public void testRef() throws Exception {
		
	}
	
}


class PriBean implements Serializable {
	String name ;
	boolean male ;
	Calendar birth ;
	long items ;
	int age ;
	String[] names ;
	
	
	public String name() {
		return name ;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this) ;
	}
}

class ParentBean {
	String name ;
	PriBean mychild ;
	
	public String name() {
		return name ;
	}

	public PriBean mychild() {
		return mychild;
	}
}