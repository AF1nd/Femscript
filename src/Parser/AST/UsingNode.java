package Parser.AST;
import Lexer.Token;

public class UsingNode extends Node {
    public final Token token;
    public final StringNode using;

    public UsingNode(Token token, StringNode using) {
        this.token = token;
        this.using = using;
    }
}
