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

public class ReadStream implements Stream<ReadNode> {

	private ReadSession rsession ;
	private Stream<ReadNode> stream;
	
	ReadStream(ReadSession rsession, Stream<ReadNode> stream) {
		this.rsession = rsession ;
		this.stream = stream;
	}

	@Override
	public Iterator<ReadNode> iterator() {
		return stream.iterator();
	}

	@Override
	public Spliterator<ReadNode> spliterator() {
		return stream.spliterator() ;
	}

	@Override
	public boolean isParallel() {
		return stream.isParallel() ;
	}

	@Override
	public ReadStream sequential() {
		stream = stream.sequential() ;
		return this ;
	}

	@Override
	public ReadStream parallel() {
		stream = stream.parallel() ;
		return this ;
	}

	@Override
	public ReadStream unordered() {
		stream = stream.unordered() ;
		return this ;
	}

	@Override
	public ReadStream onClose(Runnable closeHandler) {
		stream = stream.onClose(closeHandler) ;
		return this ;
	}

	@Override
	public void close() {
		stream.close(); 
	}

	@Override
	public ReadStream filter(Predicate<? super ReadNode> predicate) {
		stream = stream.filter(predicate) ;
		return this ;
	}

	@Override
	public <R> Stream<R> map(Function<? super ReadNode, ? extends R> mapper) {
		return stream.map(mapper) ;
	}

	@Override
	public IntStream mapToInt(ToIntFunction<? super ReadNode> mapper) {
		return stream.mapToInt(mapper) ;
	}

	@Override
	public LongStream mapToLong(ToLongFunction<? super ReadNode> mapper) {
		return stream.mapToLong(mapper) ;
	}

	@Override
	public DoubleStream mapToDouble(ToDoubleFunction<? super ReadNode> mapper) {
		return stream.mapToDouble(mapper) ;
	}

	@Override
	public <R> Stream<R> flatMap(Function<? super ReadNode, ? extends Stream<? extends R>> mapper) {
		return stream.flatMap(mapper) ;
	}

	@Override
	public IntStream flatMapToInt(Function<? super ReadNode, ? extends IntStream> mapper) {
		return stream.flatMapToInt(mapper) ;
	}

	@Override
	public LongStream flatMapToLong(Function<? super ReadNode, ? extends LongStream> mapper) {
		return stream.flatMapToLong(mapper) ;
	}

	@Override
	public DoubleStream flatMapToDouble(Function<? super ReadNode, ? extends DoubleStream> mapper) {
		return stream.flatMapToDouble(mapper);
	}

	@Override
	public ReadStream distinct() {
		stream = stream.distinct() ;
		return this ;
	}

	@Override
	public ReadStream sorted() {
		stream = stream.sorted() ;
		return this ;
	}

	@Override
	public Stream<ReadNode> sorted(Comparator<? super ReadNode> comparator) {
		return stream.sorted(comparator) ;
	}

	@Override
	public ReadStream peek(Consumer<? super ReadNode> action) {
		stream = stream.peek(action) ;
		return this ;
	}

	@Override
	public ReadStream limit(long maxSize) {
		stream = stream.limit(maxSize) ;
		return this ;
	}

	@Override
	public ReadStream skip(long n) {
		stream = stream.skip(n) ;
		return this ;
	}

	@Override
	public void forEach(Consumer<? super ReadNode> action) {
		stream.forEach(action);
	}

	@Override
	public void forEachOrdered(Consumer<? super ReadNode> action) {
		stream.forEachOrdered(action);
	}

	@Override
	public Object[] toArray() {
		return stream.toArray() ;
	}

	@Override
	public <A> A[] toArray(IntFunction<A[]> generator) {
		return stream.toArray(generator) ;
	}

	@Override
	public ReadNode reduce(ReadNode identity, BinaryOperator<ReadNode> accumulator) {
		return stream.reduce(identity, accumulator) ;
	}

	@Override
	public Optional<ReadNode> reduce(BinaryOperator<ReadNode> accumulator) {
		return stream.reduce(accumulator) ;
	}

	@Override
	public <U> U reduce(U identity, BiFunction<U, ? super ReadNode, U> accumulator, BinaryOperator<U> combiner) {
		return stream.reduce(identity, accumulator, combiner) ;
	}

	@Override
	public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super ReadNode> accumulator, BiConsumer<R, R> combiner) {
		return stream.collect(supplier, accumulator, combiner) ;
	}

	@Override
	public <R, A> R collect(Collector<? super ReadNode, A, R> collector) {
		return stream.collect(collector) ;
	}

	@Override
	public Optional<ReadNode> min(Comparator<? super ReadNode> comparator) {
		return stream.min(comparator) ;
	}

	@Override
	public Optional<ReadNode> max(Comparator<? super ReadNode> comparator) {
		return stream.max(comparator) ;
	}

	@Override
	public long count() {
		return stream.count() ;
	}

	@Override
	public boolean anyMatch(Predicate<? super ReadNode> predicate) {
		return stream.anyMatch(predicate) ;
	}

	@Override
	public boolean allMatch(Predicate<? super ReadNode> predicate) {
		return stream.allMatch(predicate) ;
	}

	@Override
	public boolean noneMatch(Predicate<? super ReadNode> predicate) {
		return stream.noneMatch(predicate) ;
	}

	@Override
	public Optional<ReadNode> findFirst() {
		return stream.findFirst() ;
	}

	@Override
	public Optional<ReadNode> findAny() {
		return stream.findAny() ;
	}

	
	public Rows toRows(String expr, FieldDefinition... fds) throws SQLException {
		return new AdNodeRows().init(rsession, this, expr, fds) ;
	}

}
