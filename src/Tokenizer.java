import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Tokenizer {
	
	private Scanner scanner;
	private String buffer;
	
	public Tokenizer(Scanner scanner) {
		this.scanner = scanner;
		scanner.useDelimiter("");
	}
	
	public Token nextToken() {
		String next = null;
		
		try {
			next = clearWhitespace(scanner);
		} catch(NoSuchElementException e) {
			return new TK_EOF();
		}
		
		if(Pattern.matches("[a-zA-Z]", next)) {
			String token = build(next, "[a-zA-Z0-9]");
			if(token.equals("fun")) {return new TK_FUNCTION();}
			else if(token.equals("seq")) {return new TK_SEQ();}
			else if(token.equals("assign")) {return new TK_ASSIGN();}
			else if(token.equals("if")) {return new TK_IF();}
			else if(token.equals("while")) {return new TK_WHILE();}
			else if(token.equals("return")) {return new TK_RETURN();}
			else if(token.equals("print")) {return new TK_PRINT();}
			else if(token.equals("num")) {return new TK_NUM();}
			else if(token.equals("var")) {return new TK_VAR();}
			else if(token.equals("bool")) {return new TK_BOOL();}
			else if(token.equals("true")) {return new TK_TRUE();}
			else if(token.equals("false")) {return new TK_FALSE();}
			else if(token.equals("and")) {return new TK_AND();}
			else if(token.equals("or")) {return new TK_OR();}
			else if(token.equals("call")) {return new TK_CALL();}
			else {return new TK_ID(token);}
		}
		else if(Pattern.matches("[0-9]", next)) {
			String token = build(next, "[0-9]");
			return new TK_INTEGER(Integer.parseInt(token));
		}
		else {
			if(next.equals("{")) {return new TK_LBRACE();}
			else if(next.equals("}")) {return new TK_RBRACE();}
			else if(next.equals("(")) {return new TK_LPAREN();}
			else if(next.equals(")")) {return new TK_RPAREN();}
			else if(next.equals("[")) {return new TK_LBRACKET();}
			else if(next.equals("]")) {return new TK_RBRACKET();}
			else if(next.equals("=")) {
				String lookahead = nextChar();
				if(lookahead.equals("=")) {return new TK_EQ();}
				buffer = lookahead;
				return new TK_ASSIGN();
			}
			else if(next.equals("!")) {
				String lookahead = nextChar();
				if(lookahead.equals("=")) {return new TK_NE();}
				buffer = lookahead;
				return new TK_NOT();
			}
			else if(next.equals("<")) {
				String lookahead = nextChar();
				if(lookahead.equals("=")) {return new TK_LE();}
				buffer = lookahead;
				return new TK_LT();
			}
			else if(next.equals(">")) {
				String lookahead = nextChar();
				if(lookahead.equals("=")) {return new TK_GE();}
				buffer = lookahead;
				return new TK_GT();
			}
			else if(next.equals("+")) {return new TK_PLUS();}
			else if(next.equals("-")) {return new TK_MINUS();}
			else if(next.equals("*")) {return new TK_TIMES();}
			else if(next.equals("/")) {return new TK_DIVIDE();}
		}
		
		return null;
	}
	
	private String build(String token, String regex) {
		String next = null;
		
		try {
			next = nextChar();
		}
		catch(NoSuchElementException e) {
			return token;
		}
		
		if(Pattern.matches(regex, next)) {
			token = token + next;
			return build(token, regex);
		}
		buffer = next;
		return token;
	}
	
	private String clearWhitespace(Scanner scanner) {
		String next = null;
		
		do {
			next = nextChar();
		} while(next.equals(" ") || next.equals("\n"));
		
		return next;
	}
	
	private String nextChar() {
		String next = null;
		
		if(buffer != null) {
			next = buffer;
			buffer = null;
		}
		else {
			next = scanner.next();
		}
		
		return next;
	}

}
