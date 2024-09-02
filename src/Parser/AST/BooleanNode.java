package Parser.AST;

import Lexer.Token;
import Lexer.TokenType;

public class BooleanNode extends Node {
    public final boolean bool;

    public BooleanNode(Token boolean_token) {
        this.bool = boolean_token.is(TokenType.TRUE);
    }
}
