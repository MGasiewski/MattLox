package com.matt.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Lox{
    static boolean hadError = false;
    public static void main(String args[] ) throws IOException{
        if(args.length>1){
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }else if(args.length==1){
            runFile(args[0]);
        }else{
            runPrompt();
        }
    }

    private static void runPrompt() throws IOException{
        var input = new InputStreamReader(System.in);
        var reader = new BufferedReader(input);
        for(;;){
            System.out.println("> ");
            var line = reader.readLine();
            if(line==null)break;
            run(line);
        }
    }

    private static void run(String source){
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();
        for(var token: tokens){
            System.out.println(token);
        }
    }

    static void error(int line, String message){
        report(line, "", message);
    }

    private static void report(int line, String where, String message){
        System.err.println(
            "[line " + line + "] Error " + where + ": " + message
        );
        hadError = true;
    }
}