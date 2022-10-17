package net.bleujin.rcraken;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
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
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import net.bleujin.rcraken.expression.Expression;
import net.bleujin.rcraken.expression.ExpressionParser;
import net.bleujin.rcraken.expression.TerminalParser;
import net.bleujin.rosetta.Parser;
import net.ion.framework.util.ArrayUtil;
import net.ion.framework.util.Debug;

public abstract class AbstractStream<T extends CommonNode, RT> implements Iterable<T> {

	private Stream<T> stream;

	protected AbstractStream(Stream<T> stream) {
		this.stream = stream;
	}

	public Iterator<T> iterator() {
		return stream.iterator();
	}

	public Spliterator<T> spliterator() {
		return stream.spliterator();
	}

	public boolean isParallel() {
		return stream.isParallel();
	}

	public RT sequential() {
		stream = stream.sequential();
		return (RT) this;
	}

	public RT parallel() {
		stream = stream.parallel();
		return (RT) this;
	}

	public RT unordered() {
		stream = stream.unordered();
		return (RT) this;
	}

	public RT onClose(Runnable closeHandler) {
		stream = stream.onClose(closeHandler);
		return (RT) this;
	}

	public void close() {
		stream.close();
	}

	public RT filter(Predicate<? super T> predicate) {
		stream = stream.filter(predicate);
		return (RT) this;
	}

	public <C> Stream<C> map(Function<? super T, ? extends C> mapper) {
		return stream.map(mapper);
	}

	public IntStream mapToInt(ToIntFunction<? super T> mapper) {
		return stream.mapToInt(mapper);
	}

	public LongStream mapToLong(ToLongFunction<? super T> mapper) {
		return stream.mapToLong(mapper);
	}

	public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
		return stream.mapToDouble(mapper);
	}

	public <C> Stream<C> flatMap(Function<? super T, ? extends Stream<? extends C>> mapper) {
		return stream.flatMap(mapper);
	}

	public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
		return stream.flatMapToInt(mapper);
	}

	public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
		return stream.flatMapToLong(mapper);
	}

	public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
		return stream.flatMapToDouble(mapper);
	}

	public RT distinct() {
		stream = stream.distinct();
		return (RT) this;
	}

	public RT sorted() {
		stream = stream.sorted();
		return (RT) this;
	}

	public RT sorted(Comparator<? super T> comparator) {
		stream = stream.sorted(comparator);
		return (RT) this ;
	}


	public RT peek(Consumer<? super T> action) {
		stream = stream.peek(action);
		return (RT) this;
	}

	public RT limit(long maxSize) {
		stream = stream.limit(maxSize);
		return (RT) this;
	}

	public RT skip(long n) {
		stream = stream.skip(n);
		return (RT) this;
	}

	public void forEach(Consumer<? super T> action) {
		stream.forEach(action);
	}

	public void forEachOrdered(Consumer<? super T> action) {
		stream.forEachOrdered(action);
	}

	public Object[] toArray() {
		return stream.toArray();
	}

	public <A> A[] toArray(IntFunction<A[]> generator) {
		return stream.toArray(generator);
	}

	public T reduce(T identity, BinaryOperator<T> accumulator) {
		return stream.reduce(identity, accumulator);
	}

	public Optional<T> reduce(BinaryOperator<T> accumulator) {
		return stream.reduce(accumulator);
	}

	public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
		return stream.reduce(identity, accumulator, combiner);
	}

	public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
		return stream.collect(supplier, accumulator, combiner);
	}

	public <R, A> R collect(Collector<? super T, A, R> collector) {
		return stream.collect(collector);
	}

	public Optional<T> min(Comparator<? super T> comparator) {
		return stream.min(comparator);
	}

	public Optional<T> max(Comparator<? super T> comparator) {
		return stream.max(comparator);
	}

	public long count() {
		return stream.count();
	}

	public boolean anyMatch(Predicate<? super T> predicate) {
		return stream.anyMatch(predicate);
	}

	public boolean allMatch(Predicate<? super T> predicate) {
		return stream.allMatch(predicate);
	}

	public boolean noneMatch(Predicate<? super T> predicate) {
		return stream.noneMatch(predicate);
	}

	public Optional<T> findFirst() {
		return stream.findFirst();
	}

	public Optional<T> findAny() {
		return stream.findAny();
	}

	// extra method.

	public void debugPrint() {
		forEach(node -> Debug.line(node));
	}

	public RT gt(String propId, Object value) {
		return filter(node -> node.hasProperty(propId) && node.property(propId).compareTo(value) > 0);
	}

	public RT gte(String propId, Object value) {
		return filter(node -> node.hasProperty(propId) && node.property(propId).compareTo(value) >= 0);
	}

	public RT lt(String propId, Object value) {
		return filter(node -> node.hasProperty(propId) && node.property(propId).compareTo(value) < 0);
	}

	public RT lte(String propId, Object value) {
		return filter(node -> node.hasProperty(propId) && node.property(propId).compareTo(value) <= 0);
	}

	public RT ne(String propId, Object value) {
		return filter(node -> node.hasProperty(propId) && node.property(propId).compareTo(value) != 0);
	}

	public RT eq(String propId, Object value) {
		return filter(node -> node.hasProperty(propId) && node.property(propId).compareTo(value) == 0);
	}

	public RT notIn(String propId, String value) {
		return filter(node -> node.hasProperty(propId) && ArrayUtil.indexOf(node.property(propId).asStrings(), value) < 0);
	}

	public RT contains(String propId, String value) {
		return filter(node -> node.hasProperty(propId) && ArrayUtil.contains(node.property(propId).asStrings(), value));
	}

	public RT hasRef(String refName, String targetPath) {
		return filter(node -> node.hasRef(refName) && ArrayUtil.contains(node.property(refName).asStrings(), targetPath));
	}

	public RT startsWith(String propId, String prefix) {
		return filter(node -> node.hasProperty(propId) && node.defaultValue(propId, "").startsWith(prefix));
	}

	public RT endsWith(String propId, String suffix) {
		return filter(node -> node.hasProperty(propId) && node.defaultValue(propId, "").endsWith(suffix));
	}

	// Element
	public RT hasProperty(String... propIds) {
		return filter(node ->  Arrays.asList(propIds).stream().map(p -> node.hasProperty(p)).allMatch(Boolean.TRUE::equals) );
	}

	public RT type(String propId, Class clz) {
		return filter(node -> node.hasProperty(propId) && node.property(propId).value() != null && clz.isInstance(node.property(propId).value()));
	}

	public RT size(String propId, int size) {
		return filter(node -> node.property(propId).asStrings().length == size);
	}

	public RT where(String expr) {
		Parser<Expression> parser = ExpressionParser.expression();
		final Expression result = TerminalParser.parse(parser, expr);
		return filter(node -> Boolean.TRUE.equals(result.value(node)));
	}

	// Logical
	public RT and(Predicate<T>... components) {
		return filter(node -> Arrays.asList(components).stream().map(p -> p.test(node)).allMatch(Boolean.TRUE::equals));
	}

	public RT or(Predicate<T>... components) {
		return filter(node -> Arrays.asList(components).stream().map(p -> p.test(node)).anyMatch(Boolean.TRUE::equals));
	}

	public RT nor(Predicate<T> left, Predicate<T> right) {
		return filter(node -> Arrays.asList(left.test(node), right.test(node)).stream().distinct().count() == 2);
	}

	public RT not(Predicate<T> components) {
		return filter(node -> Arrays.asList(components).stream().map(p -> p.test(node)).allMatch(Boolean.FALSE::equals));
	}

	public <F> F transform(Function<Iterable<T>, F> fn) {
		return fn.apply(this);
	}
	
	public RT ascending(String propId) {
		return sorted(new Comparator<T>() {
			public int compare(T o1, T o2) {
				if ( (!o1.hasProperty(propId)) || (!o2.hasProperty(propId))) return Integer.MIN_VALUE ;
				return o1.property(propId).compareTo(o2.property(propId)) ;
			}
		}) ;
	}

	public RT descending(String propId) {
		return sorted(new Comparator<T>() {
			public int compare(T o1, T o2) {
				if ( (!o1.hasProperty(propId)) || (!o2.hasProperty(propId))) return Integer.MIN_VALUE ;
				return o2.property(propId).compareTo(o1.property(propId)) ;
			}
		}) ;
	}
	
	public List<T> toList(){
		return collect(Collectors.toList()) ;
	}
}
