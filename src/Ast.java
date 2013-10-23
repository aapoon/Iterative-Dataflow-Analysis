import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Ast {}

enum BinaryOperator {
	BOP_EQ ("=="), BOP_NE ("!="), BOP_LT ("<"), BOP_GT (">"), BOP_LE ("<="), BOP_GE (">="), BOP_PLUS ("+"), BOP_MINUS ("-"), BOP_TIMES ("*"), BOP_DIVIDE ("/"), BOP_AND ("and"), BOP_OR ("or");
	
	private final String symbol;
	private BinaryOperator(String symbol) {this.symbol = symbol;}
	public String getSymbol() {return symbol;}
}

enum UnaryOperator {
	UOP_NOT ("!");
	
	private final String symbol;
	private UnaryOperator(String symbol) {this.symbol = symbol;}
	public String getSymbol() {return symbol;}
}

interface Expression {
	public Set<String> getGenVariables();
}

class EXP_NUM implements Expression {
	public int num;
	
	public EXP_NUM(int num) {
		this.num = num;
	}

	@Override
	public String toString() {
		return "(num " + num + ")";
	}
	
	@Override
	public Set<String> getGenVariables() {return new HashSet<String>();}
}

class EXP_ID implements Expression {
	public String id;
	
	public EXP_ID(String id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "(var " + id + ")";
	}
	
	@Override
	public Set<String> getGenVariables() {
		Set<String> set = new HashSet<String>();
		set.add(id);
		return set;
	}
}

class EXP_TRUE implements Expression {
	@Override
	public String toString() {
		return "(bool true)";
	}
	
	@Override
	public Set<String> getGenVariables() {return new HashSet<String>();}
}
class EXP_FALSE implements Expression {
	@Override
	public String toString() {return "(bool false)";}
	
	@Override
	public Set<String> getGenVariables() {return new HashSet<String>();}
}

class EXP_BINARY implements Expression {
	public BinaryOperator opr;
	public Expression lft;
	public Expression rht;
	
	public EXP_BINARY(BinaryOperator opr, Expression lft, Expression rht) {
		this.opr = opr;
		this.lft = lft;
		this.rht = rht;
	}
	
	@Override
	public String toString() {
		return "(" + opr.getSymbol() + " " + lft.toString() + " " + rht.toString() + ")";
	}
	
	@Override
	public Set<String> getGenVariables() {
		Set<String> set = lft.getGenVariables();
		Iterator<String> iterator = rht.getGenVariables().iterator();
		while(iterator.hasNext()) {set.add(iterator.next());}
		return set;
	}
}

class EXP_UNARY implements Expression {
	public UnaryOperator opr;
	public Expression opnd;
	
	public EXP_UNARY(UnaryOperator opr, Expression opnd) {
		this.opr = opr;
		this.opnd = opnd;
	}
	
	@Override
	public String toString() {
		return "(" + opr.getSymbol() + opnd.toString() + ")";
	}
	
	@Override
	public Set<String> getGenVariables() {return opnd.getGenVariables();}
}

class EXP_CALL implements Expression {
	public String id;
	public List<Expression> args;
	
	public EXP_CALL(String id, List<Expression> args) {
		this.id = id;
		this.args = args;
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("(call ");
		stringBuilder.append(id);
		for(Expression expression : args) {
			stringBuilder.append(" ");
			stringBuilder.append(expression.toString());
		}
		stringBuilder.append(")");
		return stringBuilder.toString();
	}
	
	@Override
	public Set<String> getGenVariables() {
		Set<String> set = new HashSet<String>();
		for(Expression expression : args) {
			Iterator<String> iterator = expression.getGenVariables().iterator();
			while(iterator.hasNext()) {set.add(iterator.next());}
		}
		return set;
	}
}

interface Statement {}

class ST_SEQ implements Statement {
	public List<Statement> stmts;
	
	public ST_SEQ(List<Statement> stmts) {
		this.stmts = stmts;
	}
}

class ST_ASSIGN implements Statement {
	public String id;
	public Expression value;
	
	public ST_ASSIGN(String id, Expression value) {
		this.id = id;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "{assign " + id + value.toString() + "}";
	}

	public Set<String> getGenVariables() {return value.getGenVariables();}
}

class ST_IF implements Statement {
	public Expression guard;
	public Statement th;
	public Statement el;
	
	public ST_IF(Expression guard, Statement th, Statement el) {
		this.guard = guard;
		this.th = th;
		this.el = el;
	}
	
	@Override
	public String toString() {
		return guard.toString();
	}
	
	public Set<String> getGenVariables() {return guard.getGenVariables();}
}

class ST_WHILE implements Statement {
	public Expression guard;
	public Statement body;
	
	public ST_WHILE(Expression guard, Statement body) {
		this.guard = guard;
		this.body = body;
	}
	
	@Override
	public String toString() {
		return guard.toString();
	}
	
	public Set<String> getGenVariables() {return guard.getGenVariables();}
}

class ST_RETURN implements Statement {
	public Expression exp;
	
	public ST_RETURN(Expression exp) {
		this.exp = exp;
	}
	
	@Override
	public String toString() {
		return "{return " + exp.toString() + "}";
	}
	
	public Set<String> getGenVariables() {return exp.getGenVariables();}
}

class ST_PRINT implements Statement {
	public Expression exp;
	
	public ST_PRINT(Expression exp) {
		this.exp = exp;
	}
	
	@Override
	public String toString() {
		return "{print " + exp.toString() + "}";
	}

	public Set<String> getGenVariables() {return exp.getGenVariables();}
}

class Function {
	public String name;
	public List<String> params;
	public List<String> vars;
	public Statement body;
	
	public Function(String name, List<String> params, List<String> vars, Statement body) {
		this.name = name;
		this.params = params;
		this.vars = vars;
		this.body = body;
	}
}

class Program {
	public List<Function> functions;
	public Program(List<Function> functions) {
		this.functions = functions;
	}
}