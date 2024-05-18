import java.util.*;
import java.util.regex.*;

public class TernaryEvaluator {
    public static void main(String[] args) {
        String input = "if (var_1 == 2, 0, if (var_2 == 4, 15, 0)) + if (var_2 == 3, 5, 0) - if (var_4 == 2, 0, 5) + if (var_3 == 3, 5, 0)";
        Map<String, Integer> context = new HashMap<>();
        context.put("var_1", 1);
        context.put("var_2", 4);
        context.put("var_3", 3);
        context.put("var_4", 5);

        TernaryEvaluator evaluator = new TernaryEvaluator();
        int result = evaluator.run(input, context);
        System.out.println(result);
    }

    public int run(String input, Map<String, Integer> context) {
        List<String> tokens = tokenize(input);
        Node expression = parse(tokens);
        return evaluate(expression, context);
    }

    private List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\s*(=>|\\{|\\}|if|\\(|\\)|,|\\+|\\-|\\*|\\/|==|!=|<=|>=|<|>|[0-9]+|[a-zA-Z_][a-zA-Z0-9_]*|[^ \\t\\r\\n])\\s*").matcher(input);
        while (matcher.find()) {
            tokens.add(matcher.group().trim());
        }
        return tokens;
    }

    private Node parse(List<String> tokens) {
        return parseExpression(tokens, new int[]{0});
    }

    private Node parseExpression(List<String> tokens, int[] pos) {
        if (tokens.get(pos[0]).equals("if")) {
            return parseTernary(tokens, pos);
        } else {
            return parsePrimary(tokens, pos);
        }
    }

    private Node parseTernary(List<String> tokens, int[] pos) {
        pos[0]++;  // Skip 'if'
        pos[0]++;  // Skip '('
        Node condition = parseExpression(tokens, pos);
        pos[0]++;  // Skip ','
        Node truthy = parseExpression(tokens, pos);
        pos[0]++;  // Skip ','
        Node falsy = parseExpression(tokens, pos);
        pos[0]++;  // Skip ')'
        return new TernaryNode(condition, truthy, falsy);
    }

    private Node parsePrimary(List<String> tokens, int[] pos) {
        String token = tokens.get(pos[0]++);
        if (token.matches("\\d+")) {
            return new NumberNode(Integer.parseInt(token));
        } else if (token.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            return new VariableNode(token);
        } else {
            throw new IllegalArgumentException("Unexpected token: " + token);
        }
    }

    private int evaluate(Node node, Map<String, Integer> context) {
        if (node instanceof NumberNode) {
            return ((NumberNode) node).value;
        } else if (node instanceof VariableNode) {
            String name = ((VariableNode) node).name;
            if (!context.containsKey(name)) {
                throw new IllegalArgumentException("Variable not found: " + name);
            }
            return context.get(name);
        } else if (node instanceof TernaryNode) {
            TernaryNode ternaryNode = (TernaryNode) node;
            int conditionValue = evaluate(ternaryNode.condition, context);
            if (conditionValue != 0) {
                return evaluate(ternaryNode.truthy, context);
            } else {
                return evaluate(ternaryNode.falsy, context);
            }
        } else {
            throw new IllegalArgumentException("Unknown node type: " + node.getClass().getName());
        }
    }

    private abstract static class Node {}

    private static class NumberNode extends Node {
        int value;
        NumberNode(int value) {
            this.value = value;
        }
    }

    private static class VariableNode extends Node {
        String name;
        VariableNode(String name) {
            this.name = name;
        }
    }

    private static class TernaryNode extends Node {
        Node condition;
        Node truthy;
        Node falsy;
        TernaryNode(Node condition, Node truthy, Node falsy) {
            this.condition = condition;
            this.truthy = truthy;
            this.falsy = falsy;
        }
    }
}
