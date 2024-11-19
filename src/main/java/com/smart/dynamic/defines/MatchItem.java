package com.smart.dynamic.defines;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @Description
 * @Author H.Y.F
 * @Date 2024/4/19 10:08
 * @Version V1.0
 */
@Getter
@Setter
public class MatchItem {
    private String formula;
    private String sql;

    public boolean match(Map<String,Object> variables) {
        return evaluateFormula(formula,variables);
    }

    public static boolean evaluateFormula(String formula,Map<String,Object> variables) {
        List<String> rpn = convertToRPN(formula);
        Stack<Boolean> stack = new Stack<>();

        for(String token : rpn) {
            if(isOperator(token)) {
                boolean operand2 = stack.pop();
                boolean operand1 = stack.pop();
                boolean result = applyOperator(token,operand1,operand2);
                stack.push(result);
            } else {
                boolean result;
                boolean round = false;
                String[] nameAndVal = token.split("=");
                //处理!的情况,反转结果
                String name = nameAndVal[0].trim();
                if(name.endsWith("!")) {
                    round = true;
                    name = name.substring(0,name.length() - 1);
                }

                //匹配
                String callVal = nameAndVal[1].trim();
                Object value = variables.get(name);
                //null值处理
                if(callVal.equals("null")) {
                    result = value == null;
                } else {
                    if(callVal.startsWith("'") && callVal.endsWith("'")) {
                        callVal = callVal.substring(1,callVal.length() - 1);
                    }
                    result = callVal.equals(String.valueOf(value));
                }
                stack.push(round != result);
            }
        }
        return stack.pop();
    }

    private static List<String> convertToRPN(String formula) {
        List<String> rpn = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        String[] tokens = formula.split("(?=[&|()])|(?<=[&|()])");
        for(String token : tokens) {
            token = token.trim();
            if(isOperator(token)) {
                while (!stack.isEmpty() && !stack.peek().equals("(")
                        && getOperatorPrecedence(stack.peek()) >= getOperatorPrecedence(token)) {
                    rpn.add(stack.pop());
                }
                stack.push(token);
            } else if(token.equals("(")) {
                stack.push(token);
            } else if(token.equals(")")) {
                while (!stack.peek().equals("(")) {
                    rpn.add(stack.pop());
                }
                stack.pop(); // Discard the "("
            } else if(!token.isEmpty()) {
                rpn.add(token);
            }
        }

        while (!stack.isEmpty()) {
            rpn.add(stack.pop());
        }

        return rpn;
    }

    private static boolean isOperator(String token) {
        return token.equals("&") || token.equals("|");
    }

    private static int getOperatorPrecedence(String operator) {
        if(operator.equals("&")) {
            return 2;
        } else if(operator.equals("|")) {
            return 1;
        }
        return 0;
    }

    private static boolean applyOperator(String operator,boolean operand1,boolean operand2) {
        if(operator.equals("&")) {
            return operand1 && operand2;
        } else if(operator.equals("|")) {
            return operand1 || operand2;
        }
        return false;
    }


//    public static void main(String[] args) {
//        Map<String,Object> variables = new HashMap<>();
//        variables.put("a",1);
//        variables.put("b","BZJz");
//        variables.put("c",2.23);
//        String formula = "a=1&(b='BZJX'|c=2.23)";
//        boolean result = evaluateFormula(formula,variables);
//        System.out.println(result);
//    }

//    public static void main(String[] args) {
//        String formula = "[BGTZ_BZ01|BGTZ_BZ02]";
//        Pattern pattern = Pattern.compile(formula);
//        Matcher matcher = pattern.matcher("B");
//        System.out.println(matcher.find());
//    }
}
