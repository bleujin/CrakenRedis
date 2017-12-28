package net.bleujin.rcraken;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import net.bleujin.rcraken.convert.AdNodeRows;
import net.bleujin.rcraken.convert.FieldDefinition;
import net.ion.framework.db.Rows;
import net.ion.framework.parse.gson.JsonArray;

public class ReadStream extends AbstractStream<ReadNode, ReadStream> implements Stream<ReadNode> {

	private ReadSession rsession ;
	
	ReadStream(ReadSession rsession, Stream<ReadNode> stream) {
		super(stream) ;
		this.rsession = rsession ;
	}
	
	public Rows toRows(String expr, FieldDefinition... fds) throws SQLException {
		return new AdNodeRows().init(rsession, this, expr, fds) ;
	}

	public JsonArray toJsonArray() {
		JsonArray result = new JsonArray() ;
		this.forEach(rn -> result.add(rn.toJson()));
		return result ;
	}


}
