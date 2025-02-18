import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.antlr.v4.runtime.misc.ParseCancellationException;

public class Main {
    public static void main(String[] args) throws IOException {
        try {

            if (args.length < 1) {
                System.err.println("Usage: java -cp \"bin:lib/antlr-4.13.2-complete.jar\" Main <filename>");
                System.exit(1);
                }

            // Read input file
            String inputFile = String.format("test/delphi/%s", args[0]);
            String input = new String(Files.readAllBytes(Paths.get(inputFile)));

            // Create a CharStream from input
            CharStream charStream = CharStreams.fromString(input);

            // Create a lexer and token stream
            delphiLexer lexer = new delphiLexer(charStream);
            lexer.removeErrorListeners(); // Remove default error listener
            lexer.addErrorListener(new ThrowingErrorListener()); // Add custom error listener

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // Create a parser
            delphiParser parser = new delphiParser(tokens);
            parser.removeErrorListeners(); // Remove default error listener
            parser.addErrorListener(new ThrowingErrorListener()); // Add custom error listener

            // Parse and generate the AST
            ParseTree tree = parser.program();
            if (tree == null) {
                throw new ParseCancellationException("Failed to generate AST: Parse tree is null");
            }
            System.out.println("AST:");
            System.out.println("-".repeat(20));
            System.out.println(tree.toStringTree(parser));  // Print the AST

            System.out.println("");
            System.out.println("Visitor Interpretor executing program:");
            System.out.println("-".repeat(20));
            CustomDelphiVisitor visitor = new CustomDelphiVisitor();
            visitor.visit(tree);

        } catch (ParseCancellationException e) {
            System.err.println("Parsing failed: " + e.getMessage());
            //throw e;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            //throw e;
        }
    }
}

// Custom error listener that throws exceptions on syntax errors
class ThrowingErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, 
                          int line, int charPositionInLine, String msg, RecognitionException e) {
        throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
    }
}
