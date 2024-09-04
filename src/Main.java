import Exceptions.FemscriptSyntaxException;
import Lexer.Lexer;
import Parser.AST.BlockNode;
import Parser.Parser;

import java.io.IOException;
import java.text.ParseException;

public class Main {
    public static void main(String[] args) throws FemscriptSyntaxException, ParseException, CloneNotSupportedException, IOException {

        final Lexer lexer = new Lexer(true, "src/test");
        final Parser parser = new Parser(lexer.tokenize());
        final BlockNode AST = parser.parse_all();

        System.out.println("ABSTRACT SYNTAX TREE: \n");

        AST.nodes.forEach(node -> System.out.println(" | " + node.toString()));

        System.out.println("RESULT: \n");

        final Interpreter interpreter = new Interpreter();
        interpreter.run(AST, null);
    }
}
