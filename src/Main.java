import Exceptions.FemscriptSyntaxException;
import Lexer.Lexer;
import Parser.AST.StatementNode;
import Parser.Parser;

import java.text.ParseException;

public class Main {
    public static void main(String[] args) throws FemscriptSyntaxException, ParseException, CloneNotSupportedException {

        String code = """
            fn foo(a) -> a + 5
            
            delay 2
            
            if 5 == 6 {
                output foo(5)
            } else {
                output foo(10)
            }
            
            """;

        final Lexer lexer = new Lexer(code, true);
        final Parser parser = new Parser(lexer.tokenize());
        final StatementNode AST = parser.parse_all();

        System.out.println("ABSTRACT SYNTAX TREE: \n");

        AST.nodes.forEach(node -> System.out.println(" | " + node.toString()));

        System.out.println("RESULT: \n");

        final Interpreter interpreter = new Interpreter();
        interpreter.run(AST, null);
    }
}
