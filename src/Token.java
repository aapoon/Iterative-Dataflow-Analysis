
public interface Token {}

class TK_LBRACE implements Token {}
class TK_RBRACE implements Token {}
class TK_LPAREN implements Token {}
class TK_RPAREN implements Token {}
class TK_LBRACKET implements Token {}
class TK_RBRACKET implements Token {}
class TK_FUNCTION implements Token {}
class TK_SEQ implements Token {}
class TK_ASSIGN implements Token {}
class TK_IF implements Token {}
class TK_WHILE implements Token {}
class TK_RETURN implements Token {}
class TK_PRINT implements Token {}
class TK_NUM implements Token {}
class TK_VAR implements Token {}
class TK_BOOL implements Token {}
class TK_TRUE implements Token {}
class TK_FALSE implements Token {}
class TK_EQ implements Token {}
class TK_NE implements Token {}
class TK_LT implements Token {}
class TK_GT implements Token {}
class TK_LE implements Token {}
class TK_GE implements Token {}
class TK_PLUS implements Token {}
class TK_MINUS implements Token {}
class TK_TIMES implements Token {}
class TK_DIVIDE implements Token {}
class TK_AND implements Token {}
class TK_OR implements Token {}
class TK_NOT implements Token {}
class TK_CALL implements Token {}
class TK_EOF implements Token {}
class TK_INTEGER implements Token {
	public int num;
	public TK_INTEGER(int num) {this.num = num;}
}
class TK_ID implements Token {
	public String id;
	public TK_ID(String id) {this.id = id;}
}
