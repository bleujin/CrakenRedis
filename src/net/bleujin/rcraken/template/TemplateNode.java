package net.bleujin.rcraken.template;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.ReadSession;
import net.ion.framework.mte.Engine;
import net.ion.framework.util.IOUtil;
import net.ion.framework.util.MapUtil;
import net.ion.framework.util.StringUtil;

public class TemplateNode {

	private final TemplateFac tfac;
	private final ReadSession rsession;
	private final Fqn fqn;
	private final String templateName;
	
	private ParamMap params = new ParamMap(MapUtil.EMPTY);
	private final static String DftTemplatePropertyName = "template";

	TemplateNode(TemplateFac tfac, ReadSession rsession, Fqn fqn, String templateName) {
		this.tfac = tfac ;
		this.rsession = rsession ;
		this.fqn = fqn ;
		this.templateName = StringUtil.defaultString(templateName, "") ;
	}

	public TemplateNode parameters(String query) {
		this.params = ParamMap.create(query) ;
		return this;
	}

	public ReadNode targetNode() {
		return rsession.pathBy(fqn) ;
	}

	public String templateName() {
		return templateName;
	}
	
	public ParamMap params() {
		return params ;
	}

	public void transform(Writer writer) {
		try {
			Engine engine = rsession.workspace().parseEngine();
			String transformed = engine.transform(findTemplate(), MapUtil.<String, Object>chainMap().put("self", targetNode()).put("params", params).toMap()) ;
			IOUtil.copy(new StringReader(transformed), writer) ;
			writer.write("\n");
			writer.flush();
		} catch(IOException ex) {
			throw new IllegalStateException(ex) ;
		}
	}
	
	private String findTemplate() {
		ReadNode current = targetNode() ;
		while(! current.isRoot()) {
			if (current.hasProperty(templateName)) {
				return current.asString(templateName) ;
			} else if (templateName.isEmpty() && current.hasProperty(DftTemplatePropertyName)) {
				return current.asString(DftTemplatePropertyName) ;
			} if (current.hasRef(templateName) && current.ref(templateName).hasProperty(DftTemplatePropertyName)) {
				return current.ref(templateName).asString(DftTemplatePropertyName) ;
			}
			current = current.parent() ;
		}
		return tfac.findTemplate(StringUtil.defaultIfEmpty(templateName, DftTemplatePropertyName)) ;
	}

}
