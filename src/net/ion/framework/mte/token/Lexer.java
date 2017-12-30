package net.ion.framework.mte.token;

import static net.ion.framework.mte.util.NestedParser.access;

import java.util.Arrays;
import java.util.List;

import net.ion.framework.mte.util.Util;
import net.ion.framework.util.ArrayUtil;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.StringUtil;

public class Lexer {

	public AbstractToken nextToken(final char[] template, final int start, final int end) {
		String input = new String(template, start, end - start);
		if (input.startsWith("--")) {
			// comment
			return null;
		}
		AbstractToken token = innerNextToken(input);
		token.setText(template, start, end);
		token.setLine(template, start, end);
		token.setColumn(template, start, end);
		return token;
	}

	private String unescapeAccess(List<? extends Object> arr, int index) {
		String val = access(arr, index);
		if (val != null && val.trim().length() > 0) {
			val = Util.NO_QUOTE_MINI_PARSER.unescape(val);
		}
		return val;
	}

	private AbstractToken innerNextToken(final String untrimmedInput) {
		final String input = Util.trimFront(untrimmedInput);
		// annotation
		if (input.length() > 0 && input.charAt(0) == '@') {
			final List<String> split = Util.RAW_MINI_PARSER.splitOnWhitespace(input.substring(1), 2);
			String receiver = access(split, 0);
			String arguments = access(split, 1);
			AnnotationToken annotationToken = new AnnotationToken(receiver, arguments);
			return annotationToken;
		}

		final List<String> split = Util.RAW_MINI_PARSER.splitOnWhitespace(input);
		
		// LENGTH 0
		if (split.size() == 0) {
			// empty expression like ${}
			return new StringToken();
		}

		if (split.size() >= 2) {
			// LENGTH 2..n

			final String cmd = split.get(0);
			final String objectExpression = split.get(1);

			if (cmd.equalsIgnoreCase(IfToken.IF)) {
				final boolean negated;
				final String ifExpression;
				// TODO: Both '!' and '=' work only if there are no white space
				// separators
				if (objectExpression.startsWith("!")) {
					negated = true;
					ifExpression = objectExpression.substring(1);
				} else {
					negated = false;
					ifExpression = objectExpression;
				}
				if (!ifExpression.contains("=")) {
					return new IfToken(ifExpression, negated);
				} else {
					final String[] ifSplit = ifExpression.split("=");
					final String variable = ifSplit[0];
					String operand = ifSplit[1];
					// remove optional quotations
					if (operand.startsWith("'") || operand.startsWith("\"")) {
						operand = operand.substring(1, operand.length() - 1);
					}
					return new IfCmpToken(variable, operand, negated);
				}
			}
			if (cmd.equalsIgnoreCase(ForEachToken.FOREACH)) {
				String remain = StringUtil.substringAfter(input, ForEachToken.FOREACH) ;
				List<String> result = ListUtil.newList();
				parseRemain(result, remain);
				// TODO : we need to do this smarter

				return new ForEachToken(result.get(0).trim(), (result.size() > 1) ? result.get(1).trim() : "", (result.size() > 2) ? result.get(2) : null);
			}
		}

		final String objectExpression = split.get(0);
		// ${
		// } which might be used for silent line breaks
		if (objectExpression.equals("")) {
			return new StringToken();
		}
		final String cmd = objectExpression;
		if (cmd.equalsIgnoreCase(ElseToken.ELSE)) {
			return new ElseToken();
		}
		if (cmd.equalsIgnoreCase(EndToken.END)) {
			return new EndToken();
		}

		// ${<h1>,address(NIX),</h1>;long(full)}
		String variableName = null; // address
		String defaultValue = null; // NIX
		String prefix = null; // <h1>
		String suffix = null; // </h1>
		String rendererName = null; // long
		String parameters = null; // full

		// be sure to use the raw input as we might have to preserve
		// whitespace for prefix and postfix
		// only innermost parsers are allowed to unescape
		final List<String> strings = Util.RAW_OUTPUT_MINI_PARSER.split(untrimmedInput, ';', 2);
		// <h1>,address(NIX),</h1>
		final String complexVariable = strings.get(0);
		// only innermost parsers are allowed to unescape
		final List<String> wrappedStrings = Util.RAW_OUTPUT_MINI_PARSER.split(complexVariable, ',', 3);
		// <h1>
		prefix = wrappedStrings.size() == 3 ? unescapeAccess(wrappedStrings, 0) : null;
		// </h1>
		suffix = wrappedStrings.size() == 3 ? unescapeAccess(wrappedStrings, 2) : null;

		// address(NIX)
		final String completeDefaultString = (wrappedStrings.size() == 3 ? unescapeAccess(wrappedStrings, 1) : complexVariable).trim();
		final List<String> defaultStrings = Util.MINI_PARSER.greedyScan(completeDefaultString, "[", "]");
		// address
		variableName = unescapeAccess(defaultStrings, 0);
		// NIX
		defaultValue = unescapeAccess(defaultStrings, 1);

		// long(full)
		final String format = access(strings, 1);
		final List<String> scannedFormat = Util.MINI_PARSER.greedyScan(format, "[", "]");
		// long
		rendererName = access(scannedFormat, 0);
		// full
		parameters = access(scannedFormat, 1);

		// this is not a well formed variable name
		if (variableName.contains(" ")) {
			return new InvalidToken();
		}

		final StringToken stringToken = new StringToken(untrimmedInput, variableName, defaultValue, prefix, suffix, rendererName, parameters);
		return stringToken;

	}

	private void parseRemain(List<String> result, String input) {
	    char[] START_PAREN = new char[] {'('};
	    char[] END_PAREN   = new char[] {')'};
	    
	    int point = 0 ;
	    int cpos = 0 ;
	    StringBuilder expression = new StringBuilder() ;
		for(char c : input.toCharArray()) {
			if (ArrayUtil.contains(START_PAREN, c)) point++ ;
			else if (ArrayUtil.contains(END_PAREN, c)) point-- ;
			else if (cpos == input.length()-1) {
				result.add(input);
			} else if (point == 0 && Character.isWhitespace(c) && (!expression.toString().trim().isEmpty())) {
				result.add(expression.toString()) ;
				parseRemain(result, input.substring(cpos));
				break ;
			} 
			expression.append(c) ;
			cpos++ ;
		}
	}

}
