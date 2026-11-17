rm -f Yylex.java
rm -f parser.java
rm -f sym.java
java JLex.Main Scanner.lex
java java_cup.Main Parser.cup
mv Scanner.lex.java Yylex.java
javac *.java
