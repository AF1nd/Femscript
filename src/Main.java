import Exceptions.FemscriptSyntaxException;
import Interpreter.Interpreter;
import Lexer.Lexer;
import Parser.AST.BlockNode;
import Parser.Parser;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws FemscriptSyntaxException, IOException {
        final Lexer lexer = new Lexer(false, "src/FemscriptScripts/Main");
        final Parser parser = new Parser(lexer.tokenize());
        final BlockNode AST = parser.parse_all();

        System.out.println("ABSTRACT SYNTAX TREE: \n");

        AST.nodes.forEach(node -> {
           if (node.toString() != null) System.out.println(" | " + node);
        });

        System.out.println("\n RESULT: \n");

        final Interpreter interpreter = new Interpreter("src/scripts/Main");
        interpreter.run(AST, null, null);
    }
}
