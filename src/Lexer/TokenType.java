package Lexer;

public enum TokenType {
    ID,
    VARIABLE,

    ASSIGN,
    PLUS,
    MINUS,
    DIV,
    MUL,

    EQ,
    NOTEQ,

    BIGGER_OR_EQ,
    SMALLER_OR_EQ,

    BIGGER,
    SMALLER,

    AND,
    OR,
    NOT,

    IF,
    ELSE,

    NUMBER,
    STRING,

    SPACE,

    RIGHT_BRACKET,
    LEFT_BRACKET,

    COMMA,
    DOT,

    TRUE,
    FALSE,

    NULL,

    OUTPUT,
    RETURN,
    WAIT,

    BEGIN,
    END,

    DEFINE_FUNCTION,

    NEWLINE,
}