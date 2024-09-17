package Parser.AST;

import java.util.ArrayList;
import java.util.List;

public class FunctionCallNode extends Node {
    public final List<Node> args = new ArrayList<>();
    public final String fn_id;

    public FunctionCallNode(String fn_id) {
        this.fn_id = fn_id;
    }

    public void add_arg(Node arg) {
        args.add(arg);
    }

    @Override
    public String toString() {
        return "call " + fn_id + " with args: " + args;
    }
}
