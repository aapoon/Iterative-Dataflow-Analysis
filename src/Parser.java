import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;


public class Parser {
	
	private Tokenizer tokenizer;
	private Digraph digraph;
	
	public Parser(String[] args) throws IOException {
		Scanner scanner = new Scanner(new File(args[0]));
		tokenizer = new Tokenizer(scanner);
		digraph = new Digraph();
		Program program = program();
		cg_all();
		cg_actual();
		controlFlowGraph(program);
	}
	
	private void cg_all() throws IOException {
		File file = new File("cg_all.dot");
		if(!file.exists()) {file.createNewFile();}
		PrintWriter printWriter = new PrintWriter(file);
		printWriter.println("digraph cg_all {");
		for(String vertice : digraph.getVertices()) {
			printWriter.println("\t" + vertice);
			for(String adjacent : digraph.getAdjacentVertices(vertice)) {
				printWriter.println("\t" + vertice +" -> " + adjacent + ";");
			}
		}
		printWriter.print("}");
		printWriter.close();
	}
	
	private void cg_actual() throws IOException {
		File file = new File("cg_actual.dot");
		if(!file.exists()) {file.createNewFile();}
		PrintWriter printWriter = new PrintWriter(file);
		printWriter.println("digraph cg_all {");
		
		Set<String> vertices_remaining = new HashSet<String>();
		vertices_remaining.add("main");
		Set<String> vertices_done = new HashSet<String>();
		
		while(!vertices_remaining.isEmpty()) {
			String vertice = vertices_remaining.iterator().next();
			printWriter.println("\t" + vertice);
			for(String adjacent : digraph.getAdjacentVertices(vertice)) {
				printWriter.println("\t" + vertice + " -> " + adjacent + ";");
				if(!vertices_done.contains(adjacent)) {vertices_remaining.add(adjacent);}
			}
			vertices_done.add(vertice);
			vertices_remaining.remove(vertice);
		}
		
		printWriter.print("}");
		printWriter.close();
	}
	
	private int cfg_counter = 0;
	
	private void cfg_func(List<Node> entries) throws IOException {
		File file = new File("cfg_func.dot");
		if(!file.exists()) {file.createNewFile();}
		PrintWriter printWriter = new PrintWriter(file);
		printWriter.println("digraph cfg_func {");
		printWriter.println("\tsize = \"4,4\";");
		
		for(Node entry : entries) {
			liveOut(entry.exit);
			int exit = cfg_counter++;
			entry.exit.number = exit;
			traverse(entry, printWriter, exit);
			//printWriter.println("\tnode" + exit + "[label=\"Exit\"]");
			printWriter.println("\tnode" + exit + "[label=\"" + exit + "\\n" + dominance(entry.exit) + "\\nExit\\nLiveOut=" + entry.exit.LiveOut.toString() + "\"]");
		}
		
		printWriter.print("}");
		printWriter.close();
	}
	
	private void liveOut(Node node) {
		for(Node successor : node.successors) {if(successor.visited == false) return;}
		for(Node successor : node.successors) {
			Set<String> set = new HashSet<String>(successor.LiveOut);
			Iterator<String> iterator = successor.kill.iterator();
			while(iterator.hasNext()) {set.remove(iterator.next());}
			iterator = successor.gen.iterator();
			while(iterator.hasNext()) {set.add(iterator.next());}
			iterator = set.iterator();
			while(iterator.hasNext()) {node.LiveOut.add(iterator.next());}
		}
		node.visited = true;
		
		for(Node predecessor : node.predecessors) {
			liveOut(predecessor);
		}
	}
	
	private int traverse(Node node, PrintWriter printWriter, int exit) {
		if(node.successors.size() != 0) {
			if(node.number == -1 || node.visited_predecessors == false) {
				if(node.number == -1) {node.number = cfg_counter++;}
				String dominance = dominance(node);
				if(dominance != null) {
					printWriter.println("\tnode" + node.number + "[label=\"" + node.number + "\\n" + dominance + "\\n" + node.statement + "\\nLiveOut=" + node.LiveOut.toString() + "\"]");
					node.visited_predecessors = true;
				}
				else {
					return node.number;
				}
				for(int i = 0; i < node.successors.size(); i++) {
					int successor_num = traverse(node.successors.get(i), printWriter, exit);
					printWriter.println("\tnode" + node.number + " -> node" + successor_num + (node.successors.size() == 2 ? "[label=\"" + (i == 0 ? "true" : "false") +"\"]": ""));
				}
			}
			return node.number;
		}
		return exit;
	}
	
	private String dominance(Node node) {
		Set<Integer> intersection = null;
		if(node.predecessors.size() > 0) {
			if(node.predecessors.get(0).number == -1) {return null;}
			intersection = new LinkedHashSet<Integer>(node.predecessors.get(0).dom);
			for(int i = 1; i < node.predecessors.size(); i++) {
				if(node.predecessors.get(i).number == -1) {return null;}
				intersection.retainAll(node.predecessors.get(i).dom);
			}
		}
		else {
			intersection = new LinkedHashSet<Integer>();
		}
		intersection.add(node.number);

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("DOM={");
		Iterator<Integer> iterator = intersection.iterator();
		Integer first = iterator.next();
		stringBuilder.append(first);
		node.dom.add(first);
		while(iterator.hasNext()) {
			stringBuilder.append(",");
			Integer next = iterator.next();
			stringBuilder.append(next);
			node.dom.add(next);
		}
		stringBuilder.append("}");
		
		return stringBuilder.toString();
	}
	
	private void controlFlowGraph(Program program) throws IOException {
		List<Node> entries = new ArrayList<Node>();
		for(Function function : program.functions) {
			Node entry = new Node();
			entry.statement = "Entry";
			Node exit = new Node();
			exit.statement = "Exit";
			entry.exit = exit;
			
			Node answer = createControlFlowGraph(entry, exit, function.body);
			if(answer != null) {
				answer.successors.add(exit);
				exit.predecessors.add(answer);
			}
			entries.add(entry);
		}
		cfg_func(entries);
	}
	
	private Node createControlFlowGraph(Node predecessor, Node exit, Statement statement) {
		if(statement instanceof ST_SEQ) {
			for(Statement stmt : ((ST_SEQ) statement).stmts) {
				Node node = createControlFlowGraph(predecessor, exit, stmt);
				if(node == null) {return null;}
				predecessor = node;
			}
			return predecessor;
		}
		else if(statement instanceof ST_ASSIGN) {
			Node node = new Node();
			node.statement = ((ST_ASSIGN) statement).toString();
			node.gen = ((ST_ASSIGN) statement).getGenVariables();
			node.kill.add(((ST_ASSIGN) statement).id);
			node.predecessors.add(predecessor);
			predecessor.successors.add(node);
			return node;
		}
		else if(statement instanceof ST_IF) {
			Node node = new Node();
			node.statement = ((ST_IF) statement).toString();
			node.gen = ((ST_IF) statement).getGenVariables();
			node.predecessors.add(predecessor);
			predecessor.successors.add(node);
			
			Node trueNode = createControlFlowGraph(node, exit, ((ST_IF) statement).th);
			Node falseNode = createControlFlowGraph(node, exit, ((ST_IF) statement).el);
			
			if(trueNode == null && falseNode == null) {return null;}
			
			Node joinNode = new Node();
			joinNode.statement = "";
			if(trueNode != null) {
				joinNode.predecessors.add(trueNode);
				trueNode.successors.add(joinNode);
			}
			if(falseNode != null) {
				joinNode.predecessors.add(falseNode);
				falseNode.successors.add(joinNode);
			}
			
			return joinNode;
		}
		else if(statement instanceof ST_WHILE) {
			Node node = new Node();
			node.statement = ((ST_WHILE) statement).toString();
			node.gen = ((ST_WHILE) statement).getGenVariables();
			node.predecessors.add(predecessor);
			predecessor.successors.add(node);
			Node trueNode = createControlFlowGraph(node, exit, ((ST_WHILE) statement).body);
			if(trueNode == null) {return null;}
			trueNode.successors.add(node);
			return node;
		}
		else if(statement instanceof ST_RETURN) {
			Node node = new Node();
			node.statement = ((ST_RETURN) statement).toString();
			node.gen = ((ST_RETURN) statement).getGenVariables();
			
			node.predecessors.add(predecessor);
			predecessor.successors.add(node);
			
			node.successors.add(exit);
			exit.predecessors.add(node);
			return null;
		}
		else if(statement instanceof ST_PRINT) {
			Node node = new Node();
			node.statement = ((ST_PRINT) statement).toString();
			node.gen = ((ST_PRINT) statement).getGenVariables();
			node.predecessors.add(predecessor);
			predecessor.successors.add(node);
			return node;
		}
		
		return null;
	}
	
	public static void main(String[] args) throws IOException {
		new Parser(args);
	}
	
	private Program program() {
		List<Function> functions = new LinkedList<Function>();
		Token token = tokenizer.nextToken();
		while(!(token instanceof TK_EOF)) {
			FunctionStruct struct = parseFunction(token);
			functions.add(struct.function);
			token = struct.token;
		}
		return new Program(functions);
	}
	
	private FunctionStruct parseFunction(Token token) {
		if(token instanceof TK_LBRACKET) {
			if(tokenizer.nextToken() instanceof TK_FUNCTION) {
				token = tokenizer.nextToken();
				if(token instanceof TK_ID) {
					String name = ((TK_ID) token).id;
					if(!digraph.getVertices().contains(name)) {digraph.addVertex(name);}
					if(tokenizer.nextToken() instanceof TK_LPAREN) {
						List<String> params = new LinkedList<String>();
						token = tokenizer.nextToken();
						while(token instanceof TK_ID) {
							params.add(((TK_ID) token).id);
							token = tokenizer.nextToken();
						}
						if(token instanceof TK_RPAREN) {
							if(tokenizer.nextToken() instanceof TK_LPAREN) {
								List<String> vars = new LinkedList<String>();
								token = tokenizer.nextToken();
								while(token instanceof TK_ID) {
									vars.add(((TK_ID) token).id);
									token = tokenizer.nextToken();
								}
								if(token instanceof TK_RPAREN) {
									StatementStruct struct = parseStatement(tokenizer.nextToken(), name);
									if(struct.token instanceof TK_RBRACKET) {
										return new FunctionStruct(new Function(name, params, vars, struct.statement), tokenizer.nextToken());
									}
									System.out.println("Expected ]");
									System.exit(0);
								}
								System.out.println("Expected )");
								System.exit(0);
							}
							System.out.println("Expected (");
							System.exit(0);
						}
						System.out.println("Expected )");
						System.exit(0);
					}
					System.out.println("Expected (");
					System.exit(0);
				}
			}
			System.out.println("Expected fun");
			System.exit(0);
		}
		System.out.println("Expected [");
		System.exit(0);
		return null;
	}
	
	private StatementStruct parseStatement(Token token, String function_name) {
		if(token instanceof TK_LBRACE) {
			token = tokenizer.nextToken();
			if(token instanceof TK_SEQ) {
				List<Statement> stmts = new LinkedList<Statement>();
				token = tokenizer.nextToken();
				while(token instanceof TK_LBRACE) {
					StatementStruct struct = parseStatement(token, function_name);
					stmts.add(struct.statement);
					token = struct.token;
				}
				if(token instanceof TK_RBRACE) {
					return new StatementStruct(new ST_SEQ(stmts), tokenizer.nextToken());
				}
				System.out.println("Expected } after statement");
				System.exit(0);
			}
			else if(token instanceof TK_ASSIGN) {
				token = tokenizer.nextToken();
				if(token instanceof TK_ID) {
					ExpressionStruct struct = parseExpression(tokenizer.nextToken(), function_name);
					if(struct.token instanceof TK_RBRACE) {
						return new StatementStruct(new ST_ASSIGN(((TK_ID) token).id, struct.expression), tokenizer.nextToken());
					}
					System.out.println("Expected } after expression");
					System.exit(0);
				}
				System.out.println("Expected id after assign");
			}
			else if(token instanceof TK_IF) {
				ExpressionStruct struct = parseExpression(tokenizer.nextToken(), function_name);
				StatementStruct thenStruct = parseStatement(struct.token, function_name);
				StatementStruct elseStruct = parseStatement(thenStruct.token, function_name);
				if(elseStruct.token instanceof TK_RBRACE) {
					return new StatementStruct(new ST_IF(struct.expression, thenStruct.statement, elseStruct.statement), tokenizer.nextToken());
				}
				System.out.println("Expected } after statement");
				System.exit(0);
			}
			else if(token instanceof TK_WHILE) {
				ExpressionStruct struct = parseExpression(tokenizer.nextToken(), function_name);
				StatementStruct stmtStruct = parseStatement(struct.token, function_name);
				if(stmtStruct.token instanceof TK_RBRACE) {
					return new StatementStruct(new ST_WHILE(struct.expression, stmtStruct.statement), tokenizer.nextToken());
				}
				System.out.println("Expected } after statement");
				System.exit(0);
			}
			else if(token instanceof TK_RETURN) {
				ExpressionStruct struct = parseExpression(tokenizer.nextToken(), function_name);
				if(struct.token instanceof TK_RBRACE) {
					return new StatementStruct(new ST_RETURN(struct.expression), tokenizer.nextToken());
				}
				System.out.println("Expected } after statement");
				System.exit(0);
			}
			else if(token instanceof TK_PRINT) {
				ExpressionStruct struct = parseExpression(tokenizer.nextToken(), function_name);
				if(struct.token instanceof TK_RBRACE) {
					return new StatementStruct(new ST_PRINT(struct.expression), tokenizer.nextToken());
				}
				System.out.println("Expected } after statement");
				System.exit(0);
			}
			System.out.println("No such statement");
			System.exit(0);
		}
		
		System.out.println("Expected {");
		System.exit(0);
		return null;
	}
	
	private ExpressionStruct parseExpression(Token token, String function_name) {
		if(token instanceof TK_LPAREN) {
			token = tokenizer.nextToken();
			if(token instanceof TK_NUM) {
				token = tokenizer.nextToken();
				if(token instanceof TK_INTEGER) {
					int num = ((TK_INTEGER) token).num;
					token = tokenizer.nextToken();
					if(token instanceof TK_RPAREN) {
						return new ExpressionStruct(new EXP_NUM(num), tokenizer.nextToken());
					}
					System.out.println("Expected ) after integer");
					System.exit(0);
				}
				System.out.println("Expected integer after num");
				System.exit(0);
			}
			else if(token instanceof TK_VAR) {
				token = tokenizer.nextToken();
				if(token instanceof TK_ID) {
					String id = ((TK_ID) token).id;
					token = tokenizer.nextToken();
					if(token instanceof TK_RPAREN) {
						return new ExpressionStruct(new EXP_ID(id), tokenizer.nextToken());
					}
					System.out.println("Expected ) after id");
					System.exit(0);
				}
				System.out.println("Expected id after var");
				System.exit(0);
			}
			else if(token instanceof TK_BOOL) {
				token = tokenizer.nextToken();
				if(token instanceof TK_TRUE) {
					token = tokenizer.nextToken();
					if(token instanceof TK_RPAREN) {
						return new ExpressionStruct(new EXP_TRUE(), tokenizer.nextToken());
					}
					System.out.println("Expected ) after true");
				}
				else if(token instanceof TK_FALSE) {
					token = tokenizer.nextToken();
					if(token instanceof TK_RPAREN) {
						return new ExpressionStruct(new EXP_FALSE(), tokenizer.nextToken());
					}
					System.out.println("Expected ) after false");
				}
				System.out.println("Expected true or false after bool");
				System.exit(0);
			}
			else if(token instanceof TK_NOT) {
				ExpressionStruct struct = parseExpression(tokenizer.nextToken(), function_name);
				if(struct.token instanceof TK_RPAREN) {
					return new ExpressionStruct(new EXP_UNARY(UnaryOperator.UOP_NOT, struct.expression), tokenizer.nextToken());
				}
				System.out.println("Expected ) after expression");
				System.exit(0);
			}
			else if(token instanceof TK_CALL) {
				token = tokenizer.nextToken();
				if(token instanceof TK_ID) {
					String id = ((TK_ID) token).id;
					digraph.addEdge(function_name, id);
					List<Expression> args = new LinkedList<Expression>();
					token = tokenizer.nextToken();
					while(token instanceof TK_LPAREN) {
						ExpressionStruct struct = parseExpression(token, function_name);
						args.add(struct.expression);
						token = struct.token;
					}
					if(token instanceof TK_RPAREN) {
						return new ExpressionStruct(new EXP_CALL(id, args), tokenizer.nextToken());
					}
					System.out.println("Expected ) after expression");
					System.exit(0);
				}
				System.out.println("Expected id after call");
				System.exit(0);
			}
			else if(isBinaryOp(token)) {
				BinaryOperator opr = getBinaryOp(token);
				ExpressionStruct struct = parseExpression(tokenizer.nextToken(), function_name);
				Expression lft = struct.expression;
				struct = parseExpression(struct.token, function_name);
				if(struct.token instanceof TK_RPAREN) {
					return new ExpressionStruct(new EXP_BINARY(opr, lft, struct.expression), tokenizer.nextToken());
				}
				System.out.println("Expected ) after expression");
				System.exit(0);
			}
			
			System.out.println("No such expression");
			System.exit(0);
		}
		System.out.println("Expected (");
		System.exit(0);
		return null;
	}
	
	private boolean isBinaryOp(Token token) {
		return token instanceof TK_EQ || token instanceof TK_NE
			|| token instanceof TK_LT || token instanceof TK_GT
			|| token instanceof TK_LE || token instanceof TK_GE
			|| token instanceof TK_PLUS || token instanceof TK_MINUS
			|| token instanceof TK_TIMES || token instanceof TK_DIVIDE
			|| token instanceof TK_AND || token instanceof TK_OR;
	}
	
	private BinaryOperator getBinaryOp(Token token) {
		if(token instanceof TK_EQ) {return BinaryOperator.BOP_EQ;}
		else if(token instanceof TK_NE) {return BinaryOperator.BOP_NE;}
		else if(token instanceof TK_LT) {return BinaryOperator.BOP_LT;}
		else if(token instanceof TK_GT) {return BinaryOperator.BOP_GT;}
		else if(token instanceof TK_LE) {return BinaryOperator.BOP_LE;}
		else if(token instanceof TK_GE) {return BinaryOperator.BOP_GE;}
		else if(token instanceof TK_PLUS) {return BinaryOperator.BOP_PLUS;}
		else if(token instanceof TK_MINUS) {return BinaryOperator.BOP_MINUS;}
		else if(token instanceof TK_TIMES) {return BinaryOperator.BOP_TIMES;}
		else if(token instanceof TK_DIVIDE) {return BinaryOperator.BOP_DIVIDE;}
		else if(token instanceof TK_AND) {return BinaryOperator.BOP_AND;}
		else if(token instanceof TK_OR) {return BinaryOperator.BOP_OR;}
		
		return null;
	}
	
	private class ExpressionStruct {
		public Expression expression;
		public Token token;
		public ExpressionStruct(Expression expression, Token token) {
			this.expression = expression;
			this.token = token;
		}
	}
	
	private class StatementStruct {
		public Statement statement;
		public Token token;
		public StatementStruct(Statement statement, Token token) {
			this.statement = statement;
			this.token = token;
		}
	}
	
	private class FunctionStruct {
		public Function function;
		public Token token;
		public FunctionStruct(Function function, Token token) {
			this.function = function;
			this.token = token;
		}
	}

}