package Lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private final String _code;

    private boolean _debug = false;

    private Integer _position = 0;
    private List<Token> _tokens = new ArrayList<Token>();

    private final TokenTypeImpl[] _token_types = {
            new TokenTypeImpl(TokenType.VARIABLE, "var"),
            new TokenTypeImpl(TokenType.DEFINE_FUNCTION, "fn"),

            new TokenTypeImpl(TokenType.RETURN, "->"),

            new TokenTypeImpl(TokenType.BIGGER_OR_EQ, ">="),
            new TokenTypeImpl(TokenType.SMALLER_OR_EQ, "<="),

            new TokenTypeImpl(TokenType.BIGGER, ">"),
            new TokenTypeImpl(TokenType.SMALLER, "<"),

            new TokenTypeImpl(TokenType.EQ, "=="),
            new TokenTypeImpl(TokenType.NOTEQ, "!="),

            new TokenTypeImpl(TokenType.AND, "&"),
            new TokenTypeImpl(TokenType.OR, "\\?"),
            new TokenTypeImpl(TokenType.NOT, "!"),

            new TokenTypeImpl(TokenType.ASSIGN, "="),
            new TokenTypeImpl(TokenType.PLUS, "\\+"),
            new TokenTypeImpl(TokenType.MINUS, "-"),
            new TokenTypeImpl(TokenType.MUL, "\\*"),
            new TokenTypeImpl(TokenType.DIV, "/"),

            new TokenTypeImpl(TokenType.IF, "if"),
            new TokenTypeImpl(TokenType.ELSE, "else"),

            new TokenTypeImpl(TokenType.NUMBER, "[+-]?([0-9]*[.])?[0-9]+"),
            new TokenTypeImpl(TokenType.STRING, "'[^']*'"),
            new TokenTypeImpl(TokenType.ID, "[a-zA-Z_][a-zA-Z0-9_]*"),

            new TokenTypeImpl(TokenType.LEFT_BRACKET, "\\("),
            new TokenTypeImpl(TokenType.RIGHT_BRACKET, "\\)"),

            new TokenTypeImpl(TokenType.BEGIN, "\\{"),
            new TokenTypeImpl(TokenType.END, "}"),

            new TokenTypeImpl(TokenType.COMMA, ","),
            new TokenTypeImpl(TokenType.DOT, "\\."),

            new TokenTypeImpl(TokenType.TRUE, "true"),
            new TokenTypeImpl(TokenType.FALSE, "false"),

            new TokenTypeImpl(TokenType.NULL, "null"),

            new TokenTypeImpl(TokenType.OUTPUT, "output"),
            new TokenTypeImpl(TokenType.WAIT, "delay"),

            new TokenTypeImpl(TokenType.NEWLINE, "\n"),
            new TokenTypeImpl(TokenType.SPACE, "\\s+"),
    };

    public Lexer(String code, boolean debug) {
        code = code.trim();

        this._code = code;
        this._debug = debug;
    }

    public List<Token> tokenize() {
        while (next_token()) {}

        _tokens = _tokens.stream().filter(token -> !token.is(new TokenType[] {TokenType.SPACE})).toList();
        if (_debug) {
            System.out.println("TOKENS: \n");
            _tokens.forEach(token -> System.out.println(" | [ type: " + token.type + " ] [ value: " + token.value + " ]"));
        }

        return this._tokens;
    }

    private boolean next_token() {
        if (_position >= _code.length()) return false;

        for (TokenTypeImpl token_type : _token_types) {
            final Matcher matcher = Pattern.compile("^" + token_type.regexp, Pattern.UNICODE_CHARACTER_CLASS).matcher(_code.substring(_position));

            if (matcher.find()) {
                final String string = matcher.group();

                for (final TokenTypeImpl _token_type : _token_types) {
                    if (_token_type.regexp.equals(string) && _token_type != token_type) {
                        token_type = _token_type;
                    }
                }

                final Token token = new Token(token_type.type_name, string);
                _tokens.add(token);

                _position += string.length();

                return true;
            }
        }

        throw new RuntimeException("FEMSCRIPT | Unexpected token | " + _position);
    }
}
