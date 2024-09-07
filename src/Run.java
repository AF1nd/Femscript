import Parser.AST.BlockNode;
import Parser.AST.Node;

public interface Run {
    public void run(Node node);
    public Object run(Node node, BlockNode parent_statement);
    public Object run(Node node, BlockNode parent_statement, Boolean assertion);
}
