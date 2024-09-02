package Lexer;

import java.util.regex.Pattern;

public class TokenTypeImpl {
    final TokenType type_name;
    final String regexp;

    TokenTypeImpl(TokenType type_name, String regexp) {
        this.type_name = type_name;
        this.regexp = regexp;
    }
}
