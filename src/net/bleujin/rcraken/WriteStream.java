package net.bleujin.rcraken;

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

public class WriteStream implements Stream<WriteNode> {

	private WriteSession wsession ;
	private Stream<WriteNode> stream;
	
	WriteStream(WriteSession wsession, Stream<WriteNode> stream) {
		this.wsession = wsession ;
		this.stream = stream;
	}

	@Override
	public Iterator<WriteNode> iterator() {
		return stream.iterator();
	}

	@Override
	public Spliterator<WriteNode> spliterator() {
		return stream.spliterator() ;
	}

	@Override
	public boolean isParallel() {
		return stream.isParallel() ;
	}

	@Override
	public WriteStream sequential() {
		stream = stream.sequential() ;
		return this ;
	}

	@Override
	public WriteStream parallel() {
		stream = stream.parallel() ;
		return this ;
	}

	@Override
	public WriteStream unordered() {
		stream = stream.unordered() ;
		return this ;
	}

	@Override
	public WriteStream onClose(Runnable closeHandler) {
		stream = stream.onClose(closeHandler) ;
		return this ;
	}

	@Override
	public void close() {
		stream.close(); 
	}

	@Override
	public WriteStream filter(Predicate<? super WriteNode> predicate) {
		stream = stream.filter(predicate) ;
		return this ;
	}

	@Override
	public <R> Stream<R> map(Function<? super WriteNode, ? extends R> mapper) {
		return stream.map(mapper) ;
	}

	@Override
	public IntStream mapToInt(ToIntFunction<? super WriteNode> mapper) {
		return stream.mapToInt(mapper) ;
	}

	@Override
	public LongStream mapToLong(ToLongFunction<? super WriteNode> mapper) {
		return stream.mapToLong(mapper) ;
	}

	@Override
	public DoubleStream mapToDouble(ToDoubleFunction<? super WriteNode> mapper) {
		return stream.mapToDouble(mapper) ;
	}

	@Override
	public <R> Stream<R> flatMap(Function<? super WriteNode, ? extends Stream<? extends R>> mapper) {
		return stream.flatMap(mapper) ;
	}

	@Override
	public IntStream flatMapToInt(Function<? super WriteNode, ? extends IntStream> mapper) {
		return stream.flatMapToInt(mapper) ;
	}

	@Override
	public LongStream flatMapToLong(Function<? super WriteNode, ? extends LongStream> mapper) {
		return stream.flatMapToLong(mapper) ;
	}

	@Override
	public DoubleStream flatMapToDouble(Function<? super WriteNode, ? extends DoubleStream> mapper) {
		return stream.flatMapToDouble(mapper);
	}

	@Override
	public WriteStream distinct() {
		stream = stream.distinct() ;
		return this ;
	}

	@Override
	public WriteStream sorted() {
		stream = stream.sorted() ;
		return this ;
	}

	@Override
	public Stream<WriteNode> sorted(Comparator<? super WriteNode> comparator) {
		return stream.sorted(comparator) ;
	}

	@Override
	public WriteStream peek(Consumer<? super WriteNode> action) {
		stream = stream.peek(action) ;
		return this ;
	}

	@Override
	public WriteStream limit(long maxSize) {
		stream = stream.limit(maxSize) ;
		return this ;
	}

	@Override
	public WriteStream skip(long n) {
		stream = stream.skip(n) ;
		return this ;
	}

	@Override
	public void forEach(Consumer<? super WriteNode> action) {
		stream.forEach(action);
	}

	@Override
	public void forEachOrdered(Consumer<? super WriteNode> action) {
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
	public WriteNode reduce(WriteNode identity, BinaryOperator<WriteNode> accumulator) {
		return stream.reduce(identity, accumulator) ;
	}

	@Override
	public Optional<WriteNode> reduce(BinaryOperator<WriteNode> accumulator) {
		return stream.reduce(accumulator) ;
	}

	@Override
	public <U> U reduce(U identity, BiFunction<U, ? super WriteNode, U> accumulator, BinaryOperator<U> combiner) {
		return stream.reduce(identity, accumulator, combiner) ;
	}

	@Override
	public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super WriteNode> accumulator, BiConsumer<R, R> combiner) {
		return stream.collect(supplier, accumulator, combiner) ;
	}

	@Override
	public <R, A> R collect(Collector<? super WriteNode, A, R> collector) {
		return stream.collect(collector) ;
	}

	@Override
	public Optional<WriteNode> min(Comparator<? super WriteNode> comparator) {
		return stream.min(comparator) ;
	}

	@Override
	public Optional<WriteNode> max(Comparator<? super WriteNode> comparator) {
		return stream.max(comparator) ;
	}

	@Override
	public long count() {
		return stream.count() ;
	}

	@Override
	public boolean anyMatch(Predicate<? super WriteNode> predicate) {
		return stream.anyMatch(predicate) ;
	}

	@Override
	public boolean allMatch(Predicate<? super WriteNode> predicate) {
		return stream.allMatch(predicate) ;
	}

	@Override
	public boolean noneMatch(Predicate<? super WriteNode> predicate) {
		return stream.noneMatch(predicate) ;
	}

	@Override
	public Optional<WriteNode> findFirst() {
		return stream.findFirst() ;
	}

	@Override
	public Optional<WriteNode> findAny() {
		return stream.findAny() ;
	}



}
