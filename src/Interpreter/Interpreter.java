package Interpreter;

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

    public Interpreter(String current_script_path) {
        this.current_script_path = current_script_path;
    }

    private Object main_run(Node node, BlockNode parent_statement) throws FemscriptRuntimeExpection {
        if (parent_statement == null && node instanceof BlockNode && !scripts_paths.containsKey(current_script_path)) {
            scripts_paths.put(current_script_path, (BlockNode) node);
        }

        try {
            if (node instanceof BlockNode typed_node) {
                if (parent_statement != null) {
                    parent_statement.scope.forEach((key, obj) -> {
                        if (!typed_node.scope.containsKey(key)) {
                            typed_node.scope.put(key, obj);
                        }
                    });
                }

                for (int i = 0; i < typed_node.nodes.size(); i++) {
                    final Node _node = typed_node.nodes.get(i);
                    if (_node != null) {
                        final Object result;

                        try {
                            result = run(_node, typed_node);

                            switch (_node) {
                                case UnarOperationNode unar_operation_node when unar_operation_node.operator.is(TokenType.RETURN) -> {
                                    if (result != null) return result;
                                }
                                case ReturnArrowSugarNode _ -> {
                                    if (result != null) return result;
                                }
                                case IfStatementNode _ -> {
                                    if (result != null) return result;
                                }
                                default -> {
                                }
                            }
                        } catch (Exception err) {
                            err.printStackTrace();
                            break;
                        }
                    } else System.out.println("PROGRAM RUN ENDED");
                }
            } else {
                if (parent_statement == null) throw new FemscriptRuntimeExpection("Node must have parent statement");

                switch (node) {
                    case UnarOperationNode typed_node -> {
                        final Token operator = typed_node.operator;
                        final Object opperrand = run(typed_node.right, parent_statement);
                        
                        if (operator.is(TokenType.ASSERT)) {
                            if (opperrand == null || (boolean) opperrand == false) {
                                throw new FemscriptRuntimeExpection("Assertion failed");
                            }
                        }
                        
                        
                        if (opperrand == null && !operator.is(TokenType.OUTPUT)) {
                            throw new FemscriptRuntimeExpection("Unar operator must have one opperrand");
                        }
                        
                        if (operator.is(TokenType.WAIT)) {
                            if (!(opperrand instanceof Number)) throw new FemscriptRuntimeExpection("Delay operator can work only with numbers");
                            
                            try {
                                Thread.sleep((long) ((long) opperrand * 1000));
                            } catch (InterruptedException err) {
                                err.printStackTrace();
                            }
                        }
                        else if (operator.is(TokenType.OUTPUT)) System.out.println(opperrand.toString());
                        else if (operator.is(TokenType.RETURN)) return opperrand;
                    }
                    case UsingNode typed_node -> {
                        final String path = (String) run(typed_node.using, parent_statement);
                        
                        if (path.equals(current_script_path)) throw new FemscriptRuntimeExpection("Script cannot using itself");
                        
                        BlockNode AST = scripts_paths.get(path);
                        
                        if (AST == null) {
                            final List<Token> tokens = new Lexer(false, path).tokenize();
                            AST = new Parser(tokens).parse_all();
                            
                            scripts_paths.put(path, AST);
                            
                            new Interpreter(path).run(AST);
                        }
                        
                        parent_statement.scope.putAll(AST.scope);
                    }
                    case IdentifierNode typed_node -> {
                        if (parent_statement.scope.containsKey(typed_node.identifier.value)) {
                            final Object value = parent_statement.scope.get(typed_node.identifier.value);

                            return value;
                        }
                    }
                    case FunctionDefineNode typed_node -> parent_statement.scope.put(typed_node.id, typed_node);
                    case FunctionCallNode typed_node -> {
                        final String fn_id = typed_node.fn_id;
                        final List<Node> args = typed_node.args;
                        
                        final Object func = parent_statement.scope.get(fn_id);
                        
                        if (!(func instanceof FunctionDefineNode)) throw new FemscriptRuntimeExpection("Function " + fn_id + " doesn't exist");
                        
                        final BlockNode block = ((FunctionDefineNode) func).block.clone();
                        
                        for (int i = 0; i < args.size(); i++) {
                            final Node arg = args.get(i);
                            
                            final IdentifierNode _arg = ((FunctionDefineNode) func).args.get(i);

                            if (_arg != null) {
                                if (!(_arg instanceof IdentifierNode)) throw new FemscriptRuntimeExpection("Argument in function define must be identifier");
                                block.scope.put(_arg.identifier.value, run(arg, parent_statement));
                            }
                        }
                        
                        return run(block, parent_statement);
                    }
                    case NumberNode typed_node -> {
                        return Long.valueOf(typed_node.value.value);
                    }
                    case StringNode typed_node -> {
                        return typed_node.value.value.substring(1, typed_node.value.value.length() - 1);
                    }
                    case BooleanNode typed_node -> {
                        return typed_node.bool;
                    }
                    case NullNode _ -> {
                        return null;
                    }
                    case ReturnArrowSugarNode typed_node -> {
                        return run(typed_node.value, parent_statement);
                    }
                    case ConditionNode typed_node -> {
                        final Object left = run(typed_node.left, parent_statement);
                        final Object right = run(typed_node.right, parent_statement);

                        if (typed_node.operator.is(TokenType.EQ)) {
                            return left == right;
                        } else if (typed_node.operator.is(TokenType.NOTEQ)) {
                            return left != right;
                        } else if (typed_node.operator.is(TokenType.BIGGER)) {
                            if (left instanceof Number && right instanceof Number) return ((long) left) > ((long) right);
                        } else if (typed_node.operator.is(TokenType.SMALLER)) {
                            if (left instanceof Number && right instanceof Number) return ((long) left) < ((long) right);
                        } else if (typed_node.operator.is(TokenType.BIGGER_OR_EQ)) {
                            if (left instanceof Number && right instanceof Number) return ((long) left) >= ((long) right);
                        } else if (typed_node.operator.is(TokenType.SMALLER_OR_EQ)) {
                            if (left instanceof Number && right instanceof Number) return ((long) left) <= ((long) right);
                        }
                    }
                    case IfStatementNode typed_node -> {
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
                            final Set<ConditionNode> successful_conditions = new HashSet<>();
                            
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
                    }
                    case BinaryOperationNode typed_node -> {
                        final Token operator = typed_node.operator;
                        
                        final Node left = typed_node.left;
                        final Node right = typed_node.right;
                        
                        if (operator.is(TokenType.ASSIGN)) {
                            if (left instanceof IdentifierNode typed_left) {
                                final int node_index = parent_statement.nodes.indexOf(node);
                                final Node previous_node = node_index > 0 ? parent_statement.nodes.get(node_index - 1) : null;
                                
                                final Object value = run(right);
                                
                                if (previous_node instanceof VariableDefineNode) {
                                    parent_statement.scope.put(typed_left.identifier.value, value != null ? value : right);
                                } else {
                                    if (parent_statement.scope.containsKey(typed_left.identifier.value)) {
                                        parent_statement.scope.put(typed_left.identifier.value, value != null ? value : right);
                                    } else throw new FemscriptRuntimeExpection("Variable " + typed_left.identifier.value + " doesn't defined");
                                }
                            } else throw new FemscriptRuntimeExpection("Assign operator can only use with identifiers");
                        } else {
                            final long left_number = (long) run(left, parent_statement);
                            final long right_number = (long) run(right, parent_statement);
                            
                            switch (operator.type) {
                                case DIV -> {
                                    return left_number / right_number;
                                }
                                case DEGREE -> {
                                    return Math.pow(left_number, right_number);
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
                    default -> {}
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }


        return null;
    }

    @Override
    public Object run(Node node) {
       return main_run(node, null);
    }

    @Override
    public Object run(Node node, BlockNode parent_statement) {
        return main_run(node, parent_statement);
    }
}
