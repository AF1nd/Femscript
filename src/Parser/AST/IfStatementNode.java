package Parser.AST;

import java.util.List;

public class IfStatementNode extends Node {
    public final List<ConditionNode> conditions;
    public final BlockNode block;
    public final BlockNode else_block;

    public IfStatementNode(List<ConditionNode> conditions, BlockNode block, BlockNode else_block) {
        this.conditions = conditions;
        this.block = block;
        this.else_block = else_block;
    }

    public String toString() {
        return "if statement" + block + "\n else statement:" + else_block;
    }
}
