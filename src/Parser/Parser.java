package Parser;

import Exceptions.FemscriptSyntaxException;
import Lexer.Token;
import Lexer.TokenType;
import Parser.AST.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

interface NeededCallback {
    Token needed(Integer offset, TokenType[] types) throws FemscriptSyntaxException;
}
public class Parser {
    private final List<Token> _tokens;

    private final BlockNode _root_node = new BlockNode();

    private static final List<Token> _parsed_tokens = new ArrayList<Token>();

    private final TokenType[] _arguments_or_values_token_types = new TokenType[]
            {TokenType.NUMBER, TokenType.STRING, TokenType.ID, TokenType.TRUE, TokenType.FALSE};

    private final TokenType[] _operators_token_types = new TokenType[]
            {TokenType.ASSIGN, TokenType.PLUS, TokenType.MINUS, TokenType.MUL, TokenType.DIV};

    private final TokenType[] _unar_operators_token_types = new TokenType[]
            {TokenType.OUTPUT, TokenType.WAIT, TokenType.RETURN, TokenType.ASSERT};

    private final TokenType[] _condition_contexts_tokens = new TokenType[]
            {TokenType.AND, TokenType.OR};

    private final TokenType[] _logical_operators_token_types = new TokenType[]
            {TokenType.EQ, TokenType.NOTEQ, TokenType.BIGGER, TokenType.SMALLER, TokenType.BIGGER_OR_EQ, TokenType.SMALLER_OR_EQ};

    private final TokenType[] _booleans_token_types = new TokenType[]
            {TokenType.TRUE, TokenType.FALSE};

    private Integer _current_line = 1;

    public Parser(List<Token> tokens) {
        this._tokens = tokens;
    }

    private void push_to_parsed(Token token) {
        Parser._parsed_tokens.add(token);
    }

    private boolean is_parsed(Token token) {
        return Parser._parsed_tokens.contains(token);
    }

    private NeededCallback needed(Integer base_index) {
        return (offset, types) -> {
            int index = base_index + offset;
            Token token = _tokens.get(index);

            if (token == null | (token != null && !token.is(types))) throw new FemscriptSyntaxException("Expected token " + Arrays.toString(types), _current_line);
            else {
                return token;
            }
        };
    }

    private VariableDefineNode parse_variable_define(int index) {
        final Token token = _tokens.get(index);
        if (token != null && token.is(TokenType.VARIABLE) && !is_parsed(token)) {
            push_to_parsed(token);
            return new VariableDefineNode(token);
        }

        return null;
    }

    private BinaryOperationNode parse_binary_operation(int index, Node left_Node) throws FemscriptSyntaxException {
        final Token token = get_token_by_index(index);
        if (token != null && token.is(_operators_token_types) && !is_parsed(token) && left_Node != null) {
            final Node right_node = parse_arg_or_value(index + 1);

            push_to_parsed(token);

            return new BinaryOperationNode(left_Node, right_node, token);
        }

        return null;
    }

    private UnarOperationNode parse_unar_operation(int index) throws FemscriptSyntaxException {
        final Token token = _tokens.get(index);
        if (token != null && token.is(_unar_operators_token_types) && !is_parsed(token)) {
            Node opperrand = null;

            opperrand = parse_arg_or_value(index + 1);

            if (opperrand != null) {
                push_to_parsed(token);
                return new UnarOperationNode(token, opperrand);
            }
        }

        return null;
    }

    private IfStatementNode parse_if_statement(int index) throws FemscriptSyntaxException {
        final Token token = _tokens.get(index);
        if (token != null && token.is(TokenType.IF) && !is_parsed(token)) {
            final ConditionNode condition = parse_condition(index + 2);
            if (condition == null) throw new FemscriptSyntaxException("If-statement must have main condition", _current_line);

            final List<ConditionNode> conditions = new ArrayList<ConditionNode>();
            conditions.add(condition);

            int begin_index = 0;

            for (int i = index; i < _tokens.size(); i++) {
                final Token token_under_index = _tokens.get(i);
                if (token_under_index.is(TokenType.BEGIN) && !is_parsed(token_under_index)) {
                    begin_index = i;
                    break;
                }
            }

            if (begin_index == 0) throw new FemscriptSyntaxException("If-statement must have block begin", _current_line);

            final Tuple<List<Token>, Integer> result = get_block_info(begin_index);
            if (result != null) {
                for (int i = index + 2; i < begin_index; i++) {
                    final ConditionNode node = parse_condition(i);
                    if (node != null) {
                        final Token prev_token = _tokens.get(i - 2);
                        if (prev_token != null && !prev_token.is(_logical_operators_token_types))
                            throw new FemscriptSyntaxException("Between conditions need ? or &", _current_line);

                        conditions.add(node);
                    }
                }

                push_to_parsed(token);

                final BlockNode else_statement = parse_else_statement(result.second() + 1);
                if (!conditions.isEmpty())
                    return new IfStatementNode(conditions, new Parser(result.first()).parse_all(), else_statement);
                else throw new FemscriptSyntaxException("If-statement must have > 0 conditions", _current_line);
            }
        }

        return null;
    }

    private BlockNode parse_else_statement(int index) throws FemscriptSyntaxException {
        final Token token = get_token_by_index(index);
        if (token != null && token.is(TokenType.ELSE) && !is_parsed(token)) {
            int begin_index = 0;

            for (int i = index; i < _tokens.size(); i++) {
                final Token token_under_index = _tokens.get(i);
                if (token_under_index.is(TokenType.BEGIN) && !is_parsed(token_under_index)) {
                    begin_index = i;
                    break;
                }
            }

            if (begin_index == 0) throw new FemscriptSyntaxException("Else-statement must have block begin", _current_line);

            final Tuple<List<Token>, Integer> result = get_block_info(begin_index);
            if (result != null) {
                final BlockNode statement = new BlockNode();
                final BlockNode ast = new Parser(result.first()).parse_all();

                ast.nodes.forEach(statement::add);

                this.push_to_parsed(token);

                return statement;
            }
        }

        return null;
    }

    private FunctionDefineNode parse_function_define(int index) throws FemscriptSyntaxException {
        final Token token = _tokens.get(index);
        if (token != null && token.is(TokenType.DEFINE_FUNCTION) && !is_parsed(token)) {
            final NeededCallback needed_descriptor = needed(index);

            final Token id_token = needed_descriptor.needed(1, new TokenType[] {TokenType.ID});

            needed_descriptor.needed(2, new TokenType[] {TokenType.LEFT_BRACKET});

            int right_bracket_index = 0;

            int finded_left_brackets = 0;
            int finded_right_brackets = 0;

            for (int i = index + 2; i < _tokens.size(); i++) {
                final Token token_under_index = _tokens.get(i);
                if (token_under_index != null) {
                    if (token_under_index.is(TokenType.RIGHT_BRACKET)) {
                        right_bracket_index = i;
                        finded_right_brackets ++;
                    } else if (token_under_index.is(TokenType.LEFT_BRACKET)) {
                        finded_left_brackets ++;
                    }
                }

                if (finded_left_brackets == finded_right_brackets) break;
            }

            if (right_bracket_index == 0)
                throw new FemscriptSyntaxException("Function " + id_token.value + " need right arguments bracket", _current_line);

            final List<IdentifierNode> args = new ArrayList<IdentifierNode>();

            for (int i = index + 3; i < right_bracket_index; i++) {
                final Token token_under_index = get_token_by_index(i);
                if (token_under_index.is(TokenType.COMMA)) continue;
                else {
                    if (!token_under_index.is(TokenType.ID))
                        throw new FemscriptSyntaxException("Function defined arg must be identifier", _current_line);

                    if (token_under_index.is(TokenType.ID) && !args.isEmpty() && !get_token_by_index(i - 1).is(TokenType.COMMA))
                        throw new FemscriptSyntaxException("Between function args need comma", _current_line);

                    args.add(new IdentifierNode(token_under_index));
                }
            }

            final Tuple<List<Token>, Integer> result = get_block_info(right_bracket_index + 1);
            if (result != null) {
                final FunctionDefineNode node = new FunctionDefineNode(id_token.value, new Parser(result.first()).parse_all());
                args.forEach(node::add_arg);

                push_to_parsed(token);
                return node;
            } else {
                final Token return_token = needed(right_bracket_index).needed(1, new TokenType[] {TokenType.RETURN});
                if (return_token != null) {
                    final UnarOperationNode return_node = parse_unar_operation(right_bracket_index + 1);
                    if (return_node == null)
                        throw new FemscriptSyntaxException("Function doesn't have block or return arrow", _current_line);

                    final BlockNode statement = new BlockNode();
                    statement.add(return_node);

                    final FunctionDefineNode node = new FunctionDefineNode(id_token.value, statement);

                    args.forEach(node::add_arg);

                    return node;
                }
            }
        }

        return null;
    }

    private FunctionCallNode parse_function_call(int index) throws FemscriptSyntaxException {
        final Token token = get_token_by_index(index);
        final Token previous_token = index > 0 ? get_token_by_index(index - 1) : null;

        if (previous_token != null) {
            if (previous_token.is(TokenType.DEFINE_FUNCTION)) return null;
        }

        if (token != null && token.is(TokenType.ID) && !is_parsed(token)) {
            final Token left_bracket_token = get_token_by_index(index + 1);
            if (left_bracket_token != null && left_bracket_token.is(TokenType.LEFT_BRACKET)) {
                final FunctionCallNode node = new FunctionCallNode(token.value);

                int right_bracket_index = 0;

                int finded_left_brackets = 0;
                int finded_right_brackets = 0;

                for (int i = index + 1; i < _tokens.size(); i++) {
                    final Token arg_token = get_token_by_index(i);
                    if (arg_token != null) {
                        if (arg_token.is(TokenType.RIGHT_BRACKET)) {
                            finded_right_brackets ++;
                            push_to_parsed(arg_token);

                            if (finded_right_brackets == finded_left_brackets) break;
                        } else if (arg_token.is(TokenType.LEFT_BRACKET)) {
                            finded_left_brackets ++;
                        } else if (arg_token.is(TokenType.COMMA)) {
                            push_to_parsed(arg_token);
                        } else {
                            if (is_parsed(arg_token)) continue;
                            if (arg_token.is(_arguments_or_values_token_types)) {
                                final Node arg_node = parse_arg_or_value(i);
                                if (arg_node != null) {
                                    final Token previous_argument_token = get_token_by_index(i - 1);

                                    if (previous_argument_token != null && !node.args.isEmpty() && !previous_argument_token.is(TokenType.COMMA))
                                        throw new FemscriptSyntaxException("Between function call arguments need comma", _current_line);

                                    node.add_arg(arg_node);
                                    push_to_parsed(arg_token);
                                 }
                            }
                        }
                    }
                }

                push_to_parsed(token);
                push_to_parsed(left_bracket_token);

                return node;
            }
        }

        return null;
    }

    private Token get_token_by_index(int index) {
        return _tokens.size() - 1 >= index ? _tokens.get(index) : null;
    }

    private BooleanNode parse_boolean(int index) {
        final Token token = get_token_by_index(index);
        if (token != null && token.is(_booleans_token_types) && !is_parsed(token)) {
            push_to_parsed(token);
            return new BooleanNode(token);
        }

        return null;
    }

    private ConditionNode parse_condition(int index) throws FemscriptSyntaxException {
        final Token token = get_token_by_index(index);
        if (token != null && token.is(_logical_operators_token_types) && !is_parsed(token)) {
            final Token context_token = get_token_by_index(index - 2);

            final String context = context_token.is(_condition_contexts_tokens) ? context_token.type.name() : "MAIN";

            final Node left_node = parse_arg_or_value(index - 1);
            final Node right_node = parse_arg_or_value(index + 1);

            push_to_parsed(token);

            if (right_node != null) return new ConditionNode(left_node, right_node, token, context);
        }

        return null;
    }

    private Node parse_arg_or_value(int index) throws FemscriptSyntaxException {
        final NeededCallback needed = needed(index);

        FunctionCallNode function_call_node = null;

        final Tuple<Integer, Integer> function_call_tuple = get_function_call_indexes(index);

        int function_call_start;
        int function_call_end;

        boolean add = true;

        if (function_call_tuple != null) {
            function_call_start = function_call_tuple.first();
            function_call_end = function_call_tuple.second();

            function_call_node = parse_function_call(function_call_start);

            if (function_call_node != null) {
                index = function_call_end + 1;
                add = false;
            }
        }

        final BooleanNode boolean_node = parse_boolean(index);

        Node node = boolean_node != null ? boolean_node : function_call_node;

        if (node == null) {
            Token token;

            token = needed.needed(0, _arguments_or_values_token_types);
            if (is_parsed(token)) return null;

            push_to_parsed(token);
            node = token.is(TokenType.ID) ? new IdentifierNode(token) : (token.is(TokenType.STRING) ? new StringNode(token) : (token.is(TokenType.NUMBER) ? new NumberNode(token) : new NullNode(token)));
        }

        final BinaryOperationNode binary_operation_node = parse_binary_operation(add ? index + 1 : index, node);
        if (binary_operation_node != null) node = binary_operation_node;

        return (Node) node;
    }

    private Tuple<Integer, Integer> get_function_call_indexes(int start_or_end_index) {
        final Token token = get_token_by_index(start_or_end_index);

        if (token != null && token.is(TokenType.ID) && get_token_by_index(start_or_end_index + 1).is(TokenType.LEFT_BRACKET)) {
            for (int i = start_or_end_index; i < _tokens.size(); i++) {
                final Token token_under_index = get_token_by_index(i);
                if (token_under_index != null && token_under_index.is(TokenType.RIGHT_BRACKET)) {
                   return new Tuple<>(start_or_end_index, i);
                }
            }
        }

        if (token != null && token.is(TokenType.RIGHT_BRACKET)) {
            for (int i = start_or_end_index; i > 0; i--) {
                final Token token_under_index = get_token_by_index(i);
                if (token_under_index != null && token_under_index.is(TokenType.ID)) {
                    final Token next = get_token_by_index(i + 1);
                    if (next != null && next.is(TokenType.LEFT_BRACKET))
                        return new Tuple<>(i, start_or_end_index);
                }
            }
        }

        return null;
    }

    private Tuple<List<Token>, Integer> get_block_info(int block_start_index) throws FemscriptSyntaxException {
        final Token token = get_token_by_index(block_start_index);

        if (token != null && token.is(TokenType.BEGIN)) {
            int begins_finded = 0;
            int ends_finded = 0;

            int end_index = 0;

            final List<Token> tokens = new ArrayList<>();

            for (int i = block_start_index; i < _tokens.size(); i++) {
                final Token token_under_index = get_token_by_index(i);
                if (token_under_index != null) {
                    if (token_under_index.is(TokenType.END) && !is_parsed(token_under_index)) {
                        ends_finded++;
                        end_index = i;
                    } else if (token_under_index.is(TokenType.BEGIN) && !is_parsed(token_under_index)) {
                        begins_finded++;
                    }

                    tokens.add(token_under_index);

                    if (ends_finded == begins_finded && begins_finded > 0) break;
                }
            }

            if (begins_finded != ends_finded) throw new FemscriptSyntaxException("Block doesn't have end", _current_line);

            return new Tuple<>(tokens, end_index);
        }

        return null;
    }

    private Node main_parse_function(int index) throws FemscriptSyntaxException {
        Node node = parse_variable_define(index);

        if (node == null) node = parse_unar_operation(index);
        if (node == null) node = parse_if_statement(index);
        if (node == null) node = parse_function_define(index);
        if (node == null) node = parse_function_call(index);

        return node;
    }

    public BlockNode parse_all() throws FemscriptSyntaxException {
        for (int i = 0; i < _tokens.size(); i++) {
            final Token token = get_token_by_index(i);
            final Node node = main_parse_function(i);

            if (node != null) _root_node.add(node);

            assert token != null;
            if (token.is(TokenType.NEWLINE)) _current_line ++;
        }

        return _root_node;
    }
}
