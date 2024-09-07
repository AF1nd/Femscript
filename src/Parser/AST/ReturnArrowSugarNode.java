package Parser.AST;

import Lexer.Token;

public class ReturnArrowSugarNode extends Node {
    public final Token token;
    public final Node value;

    public ReturnArrowSugarNode(Token token, Node value) {
        this.token = token;
        this.value = value;
    }

    @Override
    public String toString() {
        return "[RETURN ARROW]: " + token.value + " | [VALUE]: " + value.toString();
    }
}
