package Parser.AST;

import Lexer.Token;

public class UnarOperationNode extends Node {
    public final Token operator;
    public final Node right;

    public UnarOperationNode(Token operator, Node right) {
        this.operator = operator;
        this.right = right;
    }

    @Override
    public String toString() {
        return "[OPERATOR]: " + operator.value + " | [OPPERRAND]: " + right.toString();
    }
}
