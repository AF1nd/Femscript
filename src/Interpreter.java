import Exceptions.FemscriptRuntimeExpection;
import Lexer.Lexer;
import Lexer.Token;
import Lexer.TokenType;
import Parser.AST.*;
import Parser.Parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Interpreter implements Run {
    private static final HashMap<String, BlockNode> scripts_paths = new HashMap<>();

    private final String current_script_path;

    Interpreter(String current_script_path) {
        this.current_script_path = current_script_path;
    }

    private Object main_run(Node node, BlockNode parent_statement, Boolean assertion) throws FemscriptRuntimeExpection {
        if (parent_statement == null && node instanceof BlockNode && !scripts_paths.containsKey(current_script_path)) {
            scripts_paths.put(current_script_path, (BlockNode) node);
        }

        try {
            if (node instanceof BlockNode typed_node) {
                if (parent_statement != null) {
                    parent_statement.identifiers.forEach((key, _node) -> {
                        if (!typed_node.identifiers.containsKey(key)) {
                            typed_node.identifiers.put(key, _node);
                        }
                    });
                };

                for (int i = 0; i < typed_node.nodes.size(); i++) {
                    final Node _node = typed_node.nodes.get(i);
                    if (_node != null) {
                        final Object result;

                        try {
                            result = run(_node, typed_node);

                            if (_node instanceof UnarOperationNode unar_operation_node && unar_operation_node.operator.is(TokenType.RETURN)) {
                                if (result != null) return result;
                            } else  if (_node instanceof ReturnArrowSugarNode) {
                                if (result != null) return result;
                            } else if (_node instanceof IfStatementNode) {
                                if (result != null) return result;
                            }
                        } catch (Exception err) {
                            err.printStackTrace();
                            break;
                        }
                    } else System.out.println("PROGRAM RUN ENDED");
                }
            } else {
                if (parent_statement == null) throw new FemscriptRuntimeExpection("Node must have parent statement");

                if (node instanceof UnarOperationNode typed_node) {
                    final Token operator = typed_node.operator;
                    final Object opperrand = run(typed_node.right, parent_statement, operator.is(TokenType.ASSERT) ? true : null);

                    if (opperrand == null) {
                        if (operator.is(TokenType.ASSERT)) {
                            throw new FemscriptRuntimeExpection("Assertion failed");
                        }

                        throw new FemscriptRuntimeExpection("Unar operator must have one opperrand");
                    }

                    if (operator.is(TokenType.WAIT)) {
                        if (!(opperrand instanceof Number)) throw new FemscriptRuntimeExpection("Delay operator can work only with numbers");

                        try {
                            Thread.sleep((long) ((double) opperrand * 1000));
                        } catch (InterruptedException err) {
                           err.printStackTrace();
                        }
                    }
                    else if (operator.is(TokenType.OUTPUT)) System.out.println(opperrand.toString());
                    else if (operator.is(TokenType.RETURN)) return opperrand;
                } else if (node instanceof UsingNode typed_node) {
                    final String path = (String) run(typed_node.using, parent_statement);

                    if (path.equals(current_script_path)) throw new FemscriptRuntimeExpection("Script cannot using itself");

                    BlockNode AST = scripts_paths.get(path);

                    if (AST == null) {
                        final List<Token> tokens = new Lexer(false, path).tokenize();
                        AST = new Parser(tokens).parse_all();

                        scripts_paths.put(path, AST);

                        new Interpreter(path).run(AST);
                    }

                    parent_statement.identifiers.putAll(AST.identifiers);
                } else if (node instanceof IdentifierNode typed_node) {
                    if (parent_statement.identifiers.containsKey(typed_node.identifier.value)) {
                        return run(parent_statement.identifiers.get(typed_node.identifier.value), parent_statement);
                    }

                    if (assertion != null && assertion) return null;

                    throw new FemscriptRuntimeExpection("ID " + typed_node.identifier.value + " doesn't exist");
                } else if (node instanceof FunctionDefineNode typed_node) {
                    parent_statement.identifiers.put(typed_node.id, typed_node);
                } else if (node instanceof FunctionCallNode typed_node) {
                    final String fn_id = typed_node.fn_id;
                    final List<Node> args = typed_node.args;

                    final Node func = parent_statement.identifiers.get(fn_id);

                    if (!(func instanceof FunctionDefineNode)) throw new FemscriptRuntimeExpection("Function " + fn_id + " doesn't exist");

                    final BlockNode block = ((FunctionDefineNode) func).block.clone();

                    for (int i = 0; i < args.size(); i++) {
                        final Node arg = args.get(i);
                        final IdentifierNode _arg = ((FunctionDefineNode) func).args.get(i);

                        if (_arg != null) block.identifiers.put(_arg.identifier.value, arg);
                    }

                    return run(block, parent_statement);
                } else if (node instanceof NumberNode typed_node) {
                    return Double.parseDouble(typed_node.value.value);
                } else if (node instanceof StringNode typed_node) {
                    return typed_node.value.value.substring(1, typed_node.value.value.length() - 1);
                } else if (node instanceof BooleanNode typed_node) {
                    return typed_node.bool;
                } else if (node instanceof NullNode typed_node) {
                    return null;
                } else if (node instanceof ReturnArrowSugarNode typed_node) {
                    return run(typed_node.value, parent_statement);
                } else if (node instanceof ConditionNode typed_node) {
                    if (typed_node.operator.is(TokenType.EQ)) {
                        return run(typed_node.left, parent_statement) == run(typed_node.right, parent_statement);
                    } else if (typed_node.operator.is(TokenType.NOTEQ)) {
                        return run(typed_node.left, parent_statement) != run(typed_node.right, parent_statement);
                    } else if (typed_node.operator.is(TokenType.BIGGER)) {
                        final Object left = run(typed_node.left, parent_statement);
                        final Object right = run(typed_node.right, parent_statement);

                        if (left instanceof Number && right instanceof Number) return ((double) left) > ((double) right);
                    } else if (typed_node.operator.is(TokenType.SMALLER)) {
                        final Object left = run(typed_node.left, parent_statement);
                        final Object right = run(typed_node.right, parent_statement);

                        if (left instanceof Number && right instanceof Number) return ((double) left) < ((double) right);
                    } else if (typed_node.operator.is(TokenType.BIGGER_OR_EQ)) {
                        final Object left = run(typed_node.left, parent_statement);
                        final Object right = run(typed_node.right, parent_statement);

                        if (left instanceof Number && right instanceof Number) return ((double) left) >= ((double) right);
                    } else if (typed_node.operator.is(TokenType.SMALLER_OR_EQ)) {
                        final Object left = run(typed_node.left, parent_statement);
                        final Object right = run(typed_node.right, parent_statement);

                        if (left instanceof Number && right instanceof Number) return ((double) left) <= ((double) right);
                    }
                } else if (node instanceof IfStatementNode typed_node) {
                    ConditionNode main_condition = null;

                    for (final ConditionNode condition : typed_node.conditions) {
                        if (condition.context.equals("MAIN")) {
                            main_condition = condition;
                            break;
                        }
                    }

                    if (main_condition == null)
                        throw new FemscriptRuntimeExpection("If-statement must have main condition");
                    else {
                        final Set<ConditionNode> successful_conditions = new HashSet<ConditionNode>();

                        final Object main_condition_result = run(main_condition, parent_statement);

                        if (main_condition_result != null && (boolean) main_condition_result) successful_conditions.add(main_condition);

                        final int conditions_size = typed_node.conditions.size();

                        for (int i = 0; i < typed_node.conditions.size(); i++) {
                            final ConditionNode condition = typed_node.conditions.get(i);
                            final ConditionNode previous = i > 0 ? typed_node.conditions.get(i - 1) : null;

                            if (condition != null) {
                                if (condition.context.equals("AND")) {
                                    if (previous != null) {
                                        final Object left = run(previous, parent_statement);
                                        final Object right = run(condition, parent_statement);

                                        if (left != null && right != null) {
                                            if (((boolean) left) && ((boolean) right)) {
                                                successful_conditions.add(previous);
                                                successful_conditions.add(condition);
                                            }
                                        }
                                    }
                                } else if (condition.context.equals("OR")) {
                                    if (previous != null) {
                                        final Object left = run(previous, parent_statement);
                                        final Object right = run(condition, parent_statement);

                                        if ((left == null || !((boolean) left) && (right != null && (boolean) right))) {
                                            successful_conditions.add(previous);
                                            successful_conditions.add(condition);
                                        }
                                    }
                                }
                            }
                        }

                        if (successful_conditions.size() == conditions_size) return run(typed_node.block, parent_statement);
                        else {
                            if (typed_node.else_block != null) return run(typed_node.else_block, parent_statement);
                        }
                    }
                } else if (node instanceof BinaryOperationNode typed_node) {
                    final Token operator = typed_node.operator;

                    final Node left = typed_node.left;
                    final Node right = typed_node.right;

                    if (operator.is(TokenType.ASSIGN)) {
                        if (left instanceof IdentifierNode typed_left) {
                            final int node_index = parent_statement.nodes.indexOf(node);
                            final Node previous_node = node_index > 0 ? parent_statement.nodes.get(node_index - 1) : null;

                            if (previous_node instanceof VariableDefineNode) {
                                parent_statement.identifiers.put(typed_left.identifier.value, right);
                            } else {
                                if (parent_statement.identifiers.containsKey(typed_left.identifier.value)) {
                                    parent_statement.identifiers.put(typed_left.identifier.value, right);
                                } else throw new FemscriptRuntimeExpection("Variable " + typed_left.identifier.value + " doesn't defined");
                            }
                        } else throw new FemscriptRuntimeExpection("Assign operator can only use with identifiers");
                    } else {
                        final double left_number = (double) run(left, parent_statement);
                        final double right_number = (double) run(right, parent_statement);

                        switch (operator.type) {
                            case DIV -> {
                                return left_number / right_number;
                            }
                            case MUL -> {
                                return left_number * right_number;
                            }
                            case PLUS -> {
                                return left_number + right_number;
                            }
                            case MINUS -> {
                                return left_number - right_number;
                            }
                        }
                    }
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }


        return null;
    }

    @Override
    public void run(Node node) {
        main_run(node, null, null);
    }

    @Override
    public Object run(Node node, BlockNode parent_statement) {
        return main_run(node, parent_statement, null);
    }

    @Override
    public Object run(Node node, BlockNode parent_statement, Boolean assertion) {
        return main_run(node, parent_statement, assertion);
    }
}
