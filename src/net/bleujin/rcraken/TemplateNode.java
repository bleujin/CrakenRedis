package net.bleujin.rcraken;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.ion.framework.util.MapUtil;

public class TemplateNode {

	private final ReadSession rsession;
	private final Fqn fqn;
	private Map<String, List<String>> params = MapUtil.newMap();

	TemplateNode(ReadSession rsession, Fqn fqn) {
		this.rsession = rsession ;
		this.fqn = fqn ;
	}

	public TemplateNode parameters(String query) {
		this.params = Pattern.compile("&")
				.splitAsStream(query)
				.map(s -> Arrays.copyOf(s.split("="), 2))
				.collect(Collectors.groupingBy(s -> s[0], Collectors.mapping(s -> s[1], Collectors.toList()))) ;
		return this;
	}

	public ReadNode targetNode() {
		return null ;
	}

	public ReadNode templateNode() {
		return null ;
	}

	public StringBuilder template() {
		return null ;
	}

}
