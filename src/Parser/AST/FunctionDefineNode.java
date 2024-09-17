package Parser.AST;

import java.util.ArrayList;
import java.util.List;

public class FunctionDefineNode extends Node {
    public final List<IdentifierNode> args = new ArrayList<>();
    public final BlockNode block;
    public final String id;

    public FunctionDefineNode(String id, BlockNode block) {
        this.block = block;
        this.id = id;
    }

    public void add_arg(IdentifierNode arg) {
        args.add(arg);
    }

    @Override
    public String toString() {
        return "fn " + id + " | args: " + args + block;
    }
}
