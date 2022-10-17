package net.bleujin.rcraken.template;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

import net.bleujin.rcraken.Fqn;
import net.bleujin.rcraken.ReadNode;
import net.bleujin.rcraken.expression.Expression;
import net.bleujin.rcraken.expression.ExpressionParser;
import net.bleujin.rcraken.expression.TerminalParser;
import net.bleujin.rosetta.Parser;
import net.ion.framework.util.ArrayUtil;
import net.ion.framework.util.ListUtil;
import net.ion.framework.util.ObjectUtil;
import net.ion.framework.util.StringUtil;

public class Predicates {

	
//	public final static Predicate<PropertyId> onlyNormal(){
//		return onlyPropertyType(PropertyId.PType.NORMAL) ;
//	}
	
	public final static Predicate<PropertyId> onlyPropertyType(final String ptype){
		return new Predicate<PropertyId>(){
			@Override
			public boolean apply(PropertyId pid) {
				return StringUtil.equals(pid.type(), ptype);
			}
		} ;
	}
	
	
	
	
	// Comparison
	public final static Predicate<ReadNode> hasRelation(final String refName, final Fqn target) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				return node.hasRef(refName, target);
			}
		};
	}

	public final static Predicate<ReadNode> equalValue(final String propId, final Object value) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				if (node.property(propId).value() == null || value == null) return false ;
				return value.equals(node.property(propId).value());
			}
		};
	}

	public final static Predicate<ReadNode> anyValue(final String propId, final Object value) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				if (node.property(propId).value() == null || value == null) return false ;
				return ArrayUtil.contains(node.property(propId).asSet().toArray(), value);
			}
		};
	}


	public final static Predicate<ReadNode> inValue(final String propId, final Object... values) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				if (node.property(propId).value() == null || values == null) return false ;
				return ArrayUtil.contains(values, node.property(propId).value());
			}
		};
	}


	
	public static Predicate<ReadNode> containsValue(final String propId, final String value) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				if (node.property(propId).value() == null || value == null) return false ;
				for(Object eleValue : node.property(propId).asSet()){
					if (ObjectUtil.toString(eleValue).contains(value)) return true ;
				}
				return false;
			}
		};
	}
	
	
	public static Predicate<ReadNode> allValue(final String propId, final Object value) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				if (node.property(propId).value() == null || value == null) return false ;
				
				if (value.getClass().isArray()){
					Set saved = node.property(propId).asSet();
					int size = Array.getLength(value);
					int ii = 0 ;
					for (Object object : saved) {
						if (! ObjectUtil.equals(object, Array.get(value, ii++))){
							return false ;
						}
					}
					
					return saved.size() == size ;
				} else {
					return ArrayUtil.isEquals(node.property(propId).asSet().toArray(), value) ;
				}
			}
		} ;
	}
	
	public static Predicate<ReadNode> gtValue(final String propId, final Object value) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				if (node.property(propId).value() == null || value == null) return false ;
				return ((Comparable)node.property(propId).value()).compareTo(value) > 0  ;
			}
		} ;
	}
	
	public static Predicate<ReadNode> gteValue(final String propId, final Object value) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				if (node.property(propId).value() == null || value == null) return false ;
				return ((Comparable)node.property(propId).value()).compareTo(value) >= 0  ;
			}
		} ;
	}
	
	public static Predicate<ReadNode> ltValue(final String propId, final Object value) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				if (node.property(propId).value() == null || value == null) return false ;
				return ((Comparable)node.property(propId).value()).compareTo(value) < 0  ;
			}
		} ;
	}
	
	public static Predicate<ReadNode> lteValue(final String propId, final Object value) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				if (node.property(propId).value() == null || value == null) return false ;
				return ((Comparable)node.property(propId).value()).compareTo(value) <= 0  ;
			}
		} ;
	}
	
	public static Predicate<ReadNode> neValue(final String propId, final Object value) {
		return  new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				if (node.property(propId).value() == null || value == null) return false ;
				return ! value.equals(node.property(propId).value());
			}
		};
	}
	
	public static Predicate<ReadNode> notAllValue(final String propId, final Object value) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				if (node.property(propId).value() == null || value == null) return false ;
				return ! ArrayUtil.contains(node.property(propId).asSet().toArray(), value);
			}
		};
	}
	

	public static Predicate<ReadNode> startsWith(final String propId, final String value) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				if (node.property(propId).value() == null || value == null) return false ;
				return node.property(propId).asString().startsWith(value) ;
			}
		};
	}
	
	public static Predicate<ReadNode> endsWith(final String propId, final String value) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				if (node.property(propId).value() == null || value == null) return false ;
				return node.property(propId).asString().endsWith(value) ;
			}
		};
	}
	
	
	// Element
	public static Predicate<ReadNode> exists(final String propId) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				return node.hasProperty(propId) ;
			}
		} ;
	}
	
	public static Predicate<ReadNode> type(final String propId, final Class clz) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				if (node.property(propId).value() == null ) return false ;
				return clz.isInstance(node.property(propId).value()) ;
			}
		} ;
	}
	
	public static Predicate<ReadNode> size(final String propId, final int size) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				return node.property(propId).asSet().size() == size ;
			}
		} ;
	}
	

	public static Predicate<ReadNode> where(String expression) {
		Parser<Expression> parser = ExpressionParser.expression();
		final Expression result = TerminalParser.parse(parser, expression);
		
		return new Predicate<ReadNode>(){
			@Override
			public boolean apply(ReadNode node) {
				return Boolean.TRUE.equals(result.value(node));
			}
		} ;
	}
	
	
	
	
	// Logical

	public final static Predicate<ReadNode> and(List<Predicate<ReadNode>> list) {
		return new AndPredicate(list) ;
	}

	public final static Predicate<ReadNode> and(Predicate<ReadNode>... components) {
		return new AndPredicate(ListUtil.toList(components)) ;
	}

	public final static Predicate<ReadNode> or(Predicate<ReadNode>... components) {
		return new OrPredicate(ListUtil.toList(components)) ;
	}

	public final static Predicate<ReadNode> nor(final Predicate<ReadNode> left, final Predicate<ReadNode> right) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				return left.apply(node) ^ right.apply(node) ;
			}
		} ;
	}
	
	public final static Predicate<ReadNode> not(final Predicate<ReadNode> component) {
		return new Predicate<ReadNode>() {
			@Override
			public boolean apply(ReadNode node) {
				return ! component.apply(node) ;
			}
		} ;
	}


	
	private static class OrPredicate<T> implements Predicate<T>, Serializable {

		private final List<Predicate> components;
		private static final long serialVersionUID = 0L;

		private OrPredicate(List<Predicate> components) {
			this.components = components;
		}

		public boolean apply(Object t) {
			for (int i = 0; i < components.size(); i++)
				if (((Predicate) components.get(i)).apply(t))
					return true;

			return false;
		}

		public int hashCode() {
			return components.hashCode() + 87855567;
		}

		public boolean equals(Object obj) {
			if (obj instanceof OrPredicate) {
				OrPredicate that = (OrPredicate) obj;
				return components.equals(that.components);
			} else {
				return false;
			}
		}

	}

	private static class AndPredicate<ReadNode> implements Predicate<ReadNode>, Serializable {

		private final List<Predicate<ReadNode>> components;
		private static final long serialVersionUID = 0L;

		private AndPredicate(List<Predicate<ReadNode>> components) {
			this.components = components;
		}

		public boolean apply(Object t) {
			for (int i = 0; i < components.size(); i++)
				if (!((Predicate) components.get(i)).apply(t))
					return false;

			return true;
		}

		public int hashCode() {
			return components.hashCode() + 306654252;
		}

		public boolean equals(Object obj) {
			if (obj instanceof AndPredicate) {
				AndPredicate that = (AndPredicate) obj;
				return components.equals(that.components);
			} else {
				return false;
			}
		}

	}

	private static class NotPredicate<ReadNode> implements Predicate<ReadNode>, Serializable {

		final Predicate predicate;
		private static final long serialVersionUID = 0L;

		NotPredicate(Predicate predicate) {
			this.predicate = (Predicate) Preconditions.checkNotNull(predicate);
		}

		public boolean apply(Object t) {
			return !predicate.apply(t);
		}

		public int hashCode() {
			return ~predicate.hashCode();
		}

		public boolean equals(Object obj) {
			if (obj instanceof NotPredicate) {
				NotPredicate that = (NotPredicate) obj;
				return predicate.equals(that.predicate);
			} else {
				return false;
			}
		}

		public String toString() {
			return (new StringBuilder()).append("Not(").append(predicate.toString()).append(")").toString();
		}

	}




}
