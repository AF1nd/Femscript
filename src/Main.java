import Exceptions.FemscriptSyntaxException;
import Lexer.Lexer;
import Parser.AST.BlockNode;
import Parser.Parser;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws FemscriptSyntaxException, IOException {

        final Lexer lexer = new Lexer(true, "src/test");
        final Parser parser = new Parser(lexer.tokenize());
        final BlockNode AST = parser.parse_all();

        System.out.println("ABSTRACT SYNTAX TREE: \n");

        AST.nodes.forEach(node -> {
           if (node.toString() != null) System.out.println(" | " + node.toString());
        });

        System.out.println("\n RESULT: \n");

        final Interpreter interpreter = new Interpreter();
        interpreter.run(AST, null, null);
    }
}
