package Lexer;

import java.util.Arrays;

public class Token {
    public final TokenType type;
    public final String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public boolean is(TokenType[] types) {
        return Arrays.asList(types).contains(type);
    }
    public boolean is(TokenType _type) {
        return type == _type;
    }
}