package Parser.AST;

import Lexer.Token;

public class StringNode extends Node {
    public final Token value;

    public StringNode(Token value) {
        this.value = value;
    }
}
