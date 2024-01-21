package com.matt.lox;

import com.matt.lox.Expr.Binary;
import com.matt.lox.Expr.Grouping;
import com.matt.lox.Expr.Literal;
import com.matt.lox.Expr.Unary;

class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr){
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Literal expr) {
        if(expr.value == null){
            return "nil";
        }
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr... exprs){
        StringBuilder builder = new StringBuilder(100);
        builder.append("(").append(name);
        for(var expr: exprs){
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    public static void main(String[] args){
        var expression = new Expr.Binary(
            new Expr.Unary(
                new Token(TokenType.MINUS, "-", null, 1), 
                new Expr.Literal(123)), 
            new Token(TokenType.STAR, "*", null, 1),
            new Expr.Grouping(
                new Expr.Literal(45.67)
            ));

        System.out.println(new AstPrinter().print(expression));
    }
}
