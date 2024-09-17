package Interpreter;

import Parser.AST.BlockNode;
import Parser.AST.Node;

public interface Run {
    public Object run(Node node);
    public Object run(Node node, BlockNode parent_statement);
}
