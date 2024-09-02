package Parser.AST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatementNode extends Node implements Cloneable {
    public final List<Node> nodes = new ArrayList<Node>();
    public final Map<String, Node> identifiers = new HashMap<String, Node>();

    public void add(Node node) {
        nodes.add(node);
    }

    @Override
    public StatementNode clone() throws CloneNotSupportedException {
        return (StatementNode) super.clone();
    }
}
