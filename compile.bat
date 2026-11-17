@echo off
if exist Yylex.java del Yylex.java
if exist parser.java del parser.java
if exist sym.java del sym.java
java JLex.Main Scanner.lex
java java_cup.Main Parser.cup
ren Scanner.lex.java Yylex.java
javac *.java
