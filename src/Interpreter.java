import Lexer.Token;
import Lexer.TokenType;
import Parser.AST.*;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Interpreter {
    public Object run(Node node, BlockNode parent_statement) throws RuntimeException, ParseException, CloneNotSupportedException {
        if (node instanceof BlockNode typed_node) {
            if (parent_statement != null) typed_node.identifiers.putAll(parent_statement.identifiers);

            for (int i = 0; i < typed_node.nodes.size(); i++) {
                final Node _node = typed_node.nodes.get(i);
                if (_node != null) {
                    final Object result = run(_node, typed_node);

                    if (_node instanceof UnarOperationNode typed__node) {
                        if (typed__node.operator.is(TokenType.RETURN)) {
                            if (result != null) return result;
                        }
                    } else if (_node instanceof IfStatementNode) {
                        if (result != null) return result;
                    }
                }
            }
        } else {
            if (node instanceof UnarOperationNode typed_node) {
                final Token operator = typed_node.operator;
                final Object opperrand = run(typed_node.right, parent_statement);

                if (opperrand == null)
                    throw new RuntimeException("Unar operator must have one opperrand");
                if (operator.is(TokenType.WAIT)) {
                    if (!(opperrand instanceof Number)) throw new RuntimeException("Delay operator can work only with numbers");

                    try {
                        Thread.sleep((long) ((double) opperrand * 1000));
                    } catch (InterruptedException err) {
                        throw new RuntimeException(err);
                    }
                }
                else if (operator.is(TokenType.OUTPUT)) System.out.println(opperrand.toString());
                else if (operator.is(TokenType.RETURN)) return opperrand;
            } else if (node instanceof IdentifierNode typed_node) {
                if (parent_statement.identifiers.containsKey(typed_node.identifier.value)) {
                    return run(parent_statement.identifiers.get(typed_node.identifier.value), parent_statement);
                }

                throw new RuntimeException("ID " + typed_node.identifier.value + " doesn't exist");
            } else if (node instanceof FunctionDefineNode typed_node) {
                parent_statement.identifiers.put(typed_node.id, typed_node);
            } else if (node instanceof FunctionCallNode typed_node) {
                final String fn_id = typed_node.fn_id;
                final List<Node> args = typed_node.args;

                final Node func = parent_statement.identifiers.get(fn_id);

                if (!(func instanceof FunctionDefineNode)) throw new RuntimeException("Function " + fn_id + " doesn't exist");

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
                return typed_node.value.value;
            } else if (node instanceof BooleanNode typed_node) {
                return typed_node.bool;
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
                boolean successful = false;

                ConditionNode main_condition = null;

                for (final ConditionNode condition : typed_node.conditions) {
                    if (condition.context.equals("MAIN")) {
                        main_condition = condition;
                        break;
                    }
                }

                if (main_condition == null)
                    throw new RuntimeException("If-statement must have main condition");
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
                            } else throw new RuntimeException("Variable " + typed_left.identifier.value + " doesn't defined");
                        }
                    } else throw new RuntimeException("Assign operator can only use with identifiers");
                } else {
                    final double left_number = (double) run(left, parent_statement);
                    final double right_number = (double) run(right, parent_statement);

                    try {
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
                    } catch (Exception err) {
                        throw new RuntimeException(err);
                    }
                }
            }
        }

        return null;
    }
}
