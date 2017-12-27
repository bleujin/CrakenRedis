package net.bleujin.rcraken;

import java.util.Arrays;
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

import net.bleujin.rcraken.expression.Expression;
import net.bleujin.rcraken.expression.ExpressionParser;
import net.bleujin.rcraken.expression.TerminalParser;
import net.bleujin.rosetta.Parser;
import net.ion.framework.util.ArrayUtil;
import net.ion.framework.util.Debug;

public abstract class AbstractStream<T extends CommonNode, R> {

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

	public R sequential() {
		stream = stream.sequential();
		return (R) this;
	}

	public R parallel() {
		stream = stream.parallel();
		return (R) this;
	}

	public R unordered() {
		stream = stream.unordered();
		return (R) this;
	}

	public R onClose(Runnable closeHandler) {
		stream = stream.onClose(closeHandler);
		return (R) this;
	}

	public void close() {
		stream.close();
	}

	public R filter(Predicate<? super T> predicate) {
		stream = stream.filter(predicate);
		return (R) this;
	}

	public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
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

	public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
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

	public R distinct() {
		stream = stream.distinct();
		return (R) this;
	}

	public R sorted() {
		stream = stream.sorted();
		return (R) this;
	}

	public Stream<T> sorted(Comparator<? super T> comparator) {
		return stream.sorted(comparator);
	}

	public R peek(Consumer<? super T> action) {
		stream = stream.peek(action);
		return (R) this;
	}

	public R limit(long maxSize) {
		stream = stream.limit(maxSize);
		return (R) this;
	}

	public R skip(long n) {
		stream = stream.skip(n);
		return (R) this;
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

	public R gt(String propId, Object value) {
		return filter(node -> node.hasProperty(propId) && node.property(propId).compareTo(value) > 0);
	}

	public R gte(String propId, Object value) {
		return filter(node -> node.hasProperty(propId) && node.property(propId).compareTo(value) >= 0);
	}

	public R lt(String propId, Object value) {
		return filter(node -> node.hasProperty(propId) && node.property(propId).compareTo(value) < 0);
	}

	public R lte(String propId, Object value) {
		return filter(node -> node.hasProperty(propId) && node.property(propId).compareTo(value) <= 0);
	}

	public R ne(String propId, Object value) {
		return filter(node -> node.hasProperty(propId) && node.property(propId).compareTo(value) != 0);
	}

	public R eq(String propId, Object value) {
		return filter(node -> node.hasProperty(propId) && node.property(propId).compareTo(value) == 0);
	}

	public R notIn(String propId, String value) {
		return filter(node -> node.hasProperty(propId) && ArrayUtil.indexOf(node.property(propId).asStrings(), value) < 0);
	}

	public R contains(String propId, String value) {
		return filter(node -> node.hasProperty(propId) && ArrayUtil.contains(node.property(propId).asStrings(), value));
	}

	public R hasRef(String refName, String targetPath) {
		return filter(node -> node.hasRef(refName) && ArrayUtil.contains(node.property(refName).asStrings(), targetPath));
	}

	public R startsWith(String propId, String prefix) {
		return filter(node -> node.hasProperty(propId) && node.defaultValue(propId, "").startsWith(prefix));
	}

	public R endsWith(String propId, String suffix) {
		return filter(node -> node.hasProperty(propId) && node.defaultValue(propId, "").endsWith(suffix));
	}

	// Element
	public R exists(String propId) {
		return filter(node -> node.hasProperty(propId));
	}

	public R type(String propId, Class clz) {
		return filter(node -> node.hasProperty(propId) && node.property(propId).value() != null && clz.isInstance(node.property(propId).value()));
	}

	public R size(String propId, int size) {
		return filter(node -> node.property(propId).asStrings().length == size);
	}

	public R where(String expr) {
		Parser<Expression> parser = ExpressionParser.expression();
		final Expression result = TerminalParser.parse(parser, expr);
		return filter(node -> Boolean.TRUE.equals(result.value(node)));
	}

	// Logical
	public R and(Predicate<T>... components) {
		return filter(node -> Arrays.asList(components).stream().map(p -> p.test(node)).allMatch(Boolean.TRUE::equals));
	}

	public R or(Predicate<T>... components) {
		return filter(node -> Arrays.asList(components).stream().map(p -> p.test(node)).anyMatch(Boolean.TRUE::equals));
	}

	public R nor(Predicate<T> left, Predicate<T> right) {
		return filter(node -> Arrays.asList(left.test(node), right.test(node)).stream().distinct().count() == 2);
	}

	public R not(Predicate<T> components) {
		return filter(node -> Arrays.asList(components).stream().map(p -> p.test(node)).noneMatch(Boolean.TRUE::equals));
	}

	@Deprecated // use map
	public <F> F transform(Function<Iterator<T>, F> fn) {
		return fn.apply(iterator());
	}
}
