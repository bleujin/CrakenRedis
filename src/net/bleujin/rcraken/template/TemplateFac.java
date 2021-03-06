package net.bleujin.rcraken.template;

import java.util.Map;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.StringUtil;

public class TemplateFac {

	private Map<String, String> dftTemplate = MapUtil.newMap();

	public TemplateFac() {
		dftTemplate.put("children", "[${foreach self.children() child ,}${child.toJson()}${end}]") ;
		dftTemplate.put("json", "${self.toJson()}") ;
	}
	
	public TemplateNode newNode(ReadSession rsession, Fqn fqn, String templateName) {
		return new TemplateNode(this, rsession, fqn, templateName) ;
	}

	public String findTemplate(String templateName) {
		return StringUtil.coalesce(dftTemplate.get(templateName), "") ;
	}
}
