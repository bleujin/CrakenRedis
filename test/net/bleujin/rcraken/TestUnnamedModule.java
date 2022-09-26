package net.bleujin.rcraken;

import org.junit.jupiter.api.Test;

import net.bleujin.rcraken.expression.Expression;
import net.bleujin.rcraken.expression.ExpressionParser;
import net.bleujin.rcraken.expression.TerminalParser;
import net.bleujin.rosetta.Parser;

public class TestUnnamedModule {

	@Test
	public void testExpression() throws Exception {
		Parser<Expression> parser = ExpressionParser.expression();
		//final Expression result = TerminalParser.parse(parser, "this.age >= 20 and this.name = 'bleujin'");

	}
}
