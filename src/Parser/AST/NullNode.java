package Parser.AST;

import Lexer.Token;

public class NullNode extends Node {
    public final Token token;

    public NullNode(Token token) {
        this.token = token;
    }
}
