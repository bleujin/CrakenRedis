package net.bleujin.rcraken.template;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParamMap {
	private Map<String, List<String>> params = null ;

	ParamMap(Map<String, List<String>> params) {
		this.params = params ;
	}

	public static ParamMap create(String query) {
		Map<String, List<String>> params  = Pattern.compile("&")
			.splitAsStream(query)
			.map(s -> Arrays.copyOf(s.split("="), 2))
			.collect(Collectors.groupingBy(s -> s[0], Collectors.mapping(s -> s[1], Collectors.toList()))) ;
		return new ParamMap(params);
	}

	
	public String asString(String name) {
		return params.containsKey(name) ? params.get(name).get(0) : null ;
	}

	public String[] asStrings(String name) {
		return params.containsKey(name) ? params.get(name).toArray(new String[0]) : new String[0];
	}
	
	public boolean contains(String name) {
		return params.containsKey(name) ;
	}
	

}
