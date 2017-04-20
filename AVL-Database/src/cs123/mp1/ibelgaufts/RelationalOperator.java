package cs123.mp1.ibelgaufts;

// Mostly obsolete because of the AVL, but still necessary for RowNo

// Used by "where" keyword to pass relational operators to Tables
public enum RelationalOperator {
	// Ex: RelationalOperator.EQ.apply("foo", "bar") == false

	EQ("=") {
		public boolean apply(String x, String y) {
			return x.equals(y);
		}
		
		public boolean apply(Integer x, Integer y) {
			return x.equals(y);
		}
	},
	
	LT("<") {
		public boolean apply(String x, String y) {
			return x.compareTo(y) < 0;
		}
		
		public boolean apply(Integer x, Integer y) {
			return x.compareTo(y) < 0;
		}
	},
	
	GT(">") {
		public boolean apply(String x, String y) {
			return x.compareTo(y) > 0;
		}
		
		public boolean apply(Integer x, Integer y) {
			return x.compareTo(y) > 0;
		}
	},
	
	LE("<=") {
		public boolean apply(String x, String y) {
			return x.compareTo(y) <= 0;
		}
		
		public boolean apply(Integer x, Integer y) {
			return x.compareTo(y) <= 0;
		}
	},
	
	GE(">=") {
		public boolean apply(String x, String y) {
			return x.compareTo(y) >= 0;
		}
		
		public boolean apply(Integer x, Integer y) {
			return x.compareTo(y) >= 0;
		}
	};

	private final String text;

	private RelationalOperator(String text) {
		this.text = text;
	}

	public abstract boolean apply(String x, String y);
	public abstract boolean apply(Integer x, Integer y);

	@Override public String toString() {
		return text;
	}
}