package Parser.AST;

import Lexer.Token;

public class BinaryOperationNode extends Node {
    public final Node left;
    public final Node right;
    public final Token operator;

    public BinaryOperationNode(Node left, Node right, Token operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public String toString() {
        return "Binary operation: [ " + left.toString() + " " + operator.value + " " + right.toString() + " ]";
    }
}
