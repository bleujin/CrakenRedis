package net.bleujin.rcraken.template;

import java.io.IOException;
import java.util.Map;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadSession;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.StringUtil;

public class TemplateFac {

	private Map<String, String> dftTemplate = MapUtil.newMap();

	public TemplateFac() {
		dftTemplate.put(TemplateNode.DftTemplatePropertyName, defaultTemplate()) ;
		dftTemplate.put("children", "[${foreach self.children() child ,}${child.toFlatJson()}${end}]") ;
		dftTemplate.put("json", "${self.toFlatJson()}") ;
	}
	
	public TemplateNode newNode(ReadSession rsession, Fqn fqn, String templateName) {
		return new TemplateNode(this, rsession, fqn, templateName) ;
	}

	public TemplateFac addTemplate(String name, String content) {
		dftTemplate.put(name, content) ;
		return this ;
	}
	
	public String findTemplate(String templateName) {
		return StringUtil.coalesce(dftTemplate.get(templateName), dftTemplate.get(TemplateNode.DftTemplatePropertyName)) ;
	}
	
	private String defaultTemplate() {
		try {
			return IOUtil.toStringWithClose(getClass().getResourceAsStream("./craken.tpl")) ;
		} catch (NullPointerException | IOException ex) {
			return "${self}" ;
		}
	}
}
