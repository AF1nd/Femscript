package Parser.AST;

import Lexer.Token;

public class NumberNode extends Node {
    public final Token value;

    public NumberNode(Token value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "number: " + value.value;
    }
}
