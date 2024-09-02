package Parser.AST;

import Lexer.Token;

public class IdentifierNode extends Node {
    public final Token identifier;

    public IdentifierNode(Token identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return "ID: " + identifier.value;
    }
}
