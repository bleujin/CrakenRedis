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
	private final String tplName;
	
	private ParamMap params = new ParamMap(MapUtil.EMPTY);
	private final static String DftTemplatePropertyName = "template";

	TemplateNode(TemplateFac tfac, ReadSession rsession, Fqn fqn, String templateName) {
		this.tfac = tfac ;
		this.rsession = rsession ;
		this.fqn = fqn ;
		this.tplName = StringUtil.defaultString(templateName, "") ;
	}

	public TemplateNode parameters(String query) {
		this.params = ParamMap.create(query) ;
		return this;
	}

	public ReadNode targetNode() {
		return rsession.pathBy(fqn) ;
	}

	public String templateName() {
		return tplName;
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
			if (current.hasRef(tplName)) {
				for(ReadNode rnode : current.refs(tplName).stream()) {
					if (rnode.hasProperty(tplName)) {
						return rnode.asString(tplName) ;
					}
				};
			} else if (current.hasProperty(tplName)) {
				return current.asString(tplName) ;
			} 
			current = current.parent() ;
		}
		return tfac.findTemplate(StringUtil.defaultIfEmpty(tplName, DftTemplatePropertyName)) ;
	}

}
