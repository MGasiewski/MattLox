package com.matt.lox;

import static com.matt.lox.TokenType.*;

import java.util.ArrayList;
import java.util.List;

class Parser {
    private static class ParseError extends RuntimeException{}

    private final List<Token> tokens;
    private int current = 0;
    
    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    List<Stmt> parse(){
        var statements = new ArrayList<Stmt>();
        while(!isAtEnd()){
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt statement(){
        if(match(PRINT)){
            return printStatement();
        }
        if(match(LEFT_BRACE)){
            return new Stmt.Block(block());
        }
        return expressionStatement();
    }

    private Stmt printStatement(){
        var value = expression();
        consume(SEMICOLON, "Expect ';' after value");
        return new Stmt.Print(value);
    }

    private Stmt varDeclaration(){
        var name = consume(IDENTIFIER, "Expect variable name");

        Expr initializer = null;
        if(match(EQUAL)){
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt expressionStatement(){
        var expr = expression();
        consume(SEMICOLON, "Expect ';' after expression");
        return new Stmt.Expression(expr); 
    }

    private List<Stmt> block(){
        var statements = new ArrayList<Stmt>();

        while(!check(RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Expr assignment(){
        var expr = equality();
        if(match(EQUAL)){
            var equals = previous();
            var value = assignment();

            if(expr instanceof Expr.Variable){
                var name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr expression(){
        return assignment();
    }

    private Stmt declaration(){
        try{
            if(match(VAR)){
                return varDeclaration();
            }
            return statement();
        }
            catch(ParseError error){
                synchronize();
                return null;
            }
        }
    

    private Expr equality(){
        var expr = comparison();

        while(match(BANG_EQUAL, EQUAL_EQUAL)){
            var operator = previous();
            var right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison(){
        var expr = term();

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            var operator = previous();
            var right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term(){
        var expr = factor();

        while(match(MINUS, PLUS)){
            var operator = previous();
            var right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor(){
        var expr = unary();

        while(match(SLASH, STAR)){
            var operator = previous();
            var right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary(){
        if(match(BANG, MINUS)){
            var operator = previous();
            var right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary(){
        if(match(FALSE)){
            return new Expr.Literal(false);
        }
        if(match(TRUE)){
            return new Expr.Literal(true);
        }
        if(match(NIL)){
            return new Expr.Literal(null);
        }

        if(match(NUMBER, STRING)){
            return new Expr.Literal(previous().literal);
        }

        if(match(IDENTIFIER)){
            return new Expr.Variable(previous());
        }

        if(match(LEFT_PAREN)){
            var expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types){
        for(var type: types){
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message){
        if(check(type)){
            return advance();
        }

        throw error(peek(), message);
    }

    

    private boolean check(TokenType type){
        if(isAtEnd()){
            return false;
        }
        return peek().type == type;
    }

    private Token advance(){
        if(!isAtEnd()){
            current++;
        }
        return previous();
    }

    private boolean isAtEnd(){
        return peek().type == EOF;
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token previous(){
        return tokens.get(current-1);
    }

    private ParseError error(Token token, String message){
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize(){
        advance();

        while(!isAtEnd()){
            if(previous().type == SEMICOLON){
                return;
            }

            switch(peek().type){
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }

}
