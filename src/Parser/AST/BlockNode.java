package Parser.AST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockNode extends Node implements Cloneable {
    public final List<Node> nodes = new ArrayList<>();
    public final Map<String, Object> scope = new HashMap<>();

    public void add(Node node) {
        nodes.add(node);
    }

    @Override
    public BlockNode clone() throws CloneNotSupportedException {
        return (BlockNode) super.clone();
    }

    @Override
    public String toString() {
        return "\n block: \n" + nodes.toString() + "\n end";
    }
}
