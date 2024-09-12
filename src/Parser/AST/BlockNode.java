package Parser.AST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockNode extends Node implements Cloneable {
    public final List<Node> nodes = new ArrayList<Node>();
    public final Map<String, Object> scope = new HashMap<String, Object>();

    public void add(Node node) {
        nodes.add(node);
    }

    @Override
    public BlockNode clone() throws CloneNotSupportedException {
        return (BlockNode) super.clone();
    }
}
