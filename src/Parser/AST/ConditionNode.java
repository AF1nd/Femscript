package Parser.AST;

import Lexer.Token;

public class ConditionNode extends Node {
    public final Node left;
    public final Node right;
    public final Token operator;

    public final String context;

    public ConditionNode(Node left, Node right, Token operator, String context) {
        this.left = left;
        this.right = right;
        this.operator = operator;
        this.context = context;
    }
}
