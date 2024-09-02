package Parser.AST;

import Lexer.Token;

public class VariableDefineNode extends Node {
    public final Token value;

    public VariableDefineNode(Token value) {
        this.value = value;
    }
}
