package Parser.AST;

import java.util.List;

public class IfStatementNode extends Node {
    public final List<ConditionNode> conditions;
    public final StatementNode block;
    public final StatementNode else_block;

    public IfStatementNode(List<ConditionNode> conditions, StatementNode block, StatementNode else_block) {
        this.conditions = conditions;
        this.block = block;
        this.else_block = else_block;
    }
}
