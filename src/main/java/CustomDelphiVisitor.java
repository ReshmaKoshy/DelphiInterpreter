import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.*;
import java.io.*;

public class CustomDelphiVisitor extends delphiBaseVisitor<Object> {
    private Map<String, Object> variables = new HashMap<>();
    private Map<String, Map<String, Object>> objects = new HashMap<>();
    private Map<String, String> objectClasses = new HashMap<>();
    private Map<String, Map<String, ParseTree>> methods = new HashMap<>();
    private Scanner scanner = new Scanner(System.in);
    private String currentClass = null;
    private String currentObject = null;

    @Override
    public Object visitProgram(delphiParser.ProgramContext ctx) {
        return visit(ctx.block());
    }

    @Override
    public Object visitBlock(delphiParser.BlockContext ctx) {
        for (ParseTree child : ctx.children) {
            visit(child);
        }
        return null;
    }

    @Override
    public Object visitVariableDeclarationPart(delphiParser.VariableDeclarationPartContext ctx) {
        for (delphiParser.VariableDeclarationContext varDecl : ctx.variableDeclaration()) {
            for (TerminalNode ident : varDecl.identifierList().IDENT()) {
                variables.put(ident.getText().toLowerCase(), "");
            }
        }
        return null;
    }

    @Override
    public Object visitClassDeclaration(delphiParser.ClassDeclarationContext ctx) {
        String className = ctx.IDENT().getText().toLowerCase();
        objects.put(className, new HashMap<>());
        methods.put(className, new HashMap<>());
        return null;
    }

    @Override
    public Object visitStatements(delphiParser.StatementsContext ctx) {
        for (delphiParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        return null;
    }

    @Override
    public Object visitCompoundStatement(delphiParser.CompoundStatementContext ctx) {
    return visit(ctx.statements());
    }

     @Override
    public Object visitExpr(delphiParser.ExprContext ctx) {
        StringBuilder result = new StringBuilder();
        for (delphiParser.ValueContext valueCtx : ctx.value()) {
            Object value = visit(valueCtx);
            if (value != null) {
                result.append(value);
            }
        }
        return result.toString();
    }

    @Override
    public Object visitValue(delphiParser.ValueContext ctx) {
        if (ctx.variable() != null) {
            return visit(ctx.variable());
        } else if (ctx.STRING_LITERAL() != null) {
            String text = ctx.STRING_LITERAL().getText();
            return text.substring(1, text.length() - 1);
        } else if (ctx.NUMBER() != null) {
            return ctx.NUMBER().getText();
        }
        return "";
    }

    @Override
    public Object visitStatement(delphiParser.StatementContext ctx) {
        if (ctx.assignmentStatement() != null) return visit(ctx.assignmentStatement());
        if (ctx.consoleStatement() != null) return visit(ctx.consoleStatement());
        if (ctx.destructorCall() != null) return visit(ctx.destructorCall());
        if (ctx.objectCreation() != null) return visit(ctx.objectCreation());
        return null;
    }

    @Override
    public Object visitVariable(delphiParser.VariableContext ctx) {
    if (ctx.classIdentifier() != null) {
        String objectName = ctx.classIdentifier().getText().toLowerCase();
        String fieldName = ctx.IDENT().getText().toLowerCase();
        Map<String, Object> object = objects.get(objectName);
        if (object != null) {
            return object.get(fieldName);
        }
    }
    String varName = ctx.getText().toLowerCase();
    if (currentObject != null) {
        Map<String, Object> objectFields = objects.get(currentObject);
        if (objectFields != null && objectFields.containsKey(varName)) {
            return objectFields.get(varName);
        }
    }
    return variables.get(varName);
}

    @Override
    public Object visitDestructorBody(delphiParser.DestructorBodyContext ctx) {
        // Visit all statements in the destructor body  
        for (delphiParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        return null;
    }

    @Override
    public Object visitConstructorImpl(delphiParser.ConstructorImplContext ctx) {
        String className = ctx.classIdentifier().getText().toLowerCase();
        String methodName = "create";
        Map<String, ParseTree> classMethods = methods.get(className);
        if (classMethods != null) {
            classMethods.put(methodName, ctx.compoundStatement());
        }
        return null;
    }

    @Override
    public Object visitDestructorImpl(delphiParser.DestructorImplContext ctx) {
        String className = ctx.classIdentifier().getText().toLowerCase();
        String methodName = "destroy";
        Map<String, ParseTree> classMethods = methods.get(className);
        if (classMethods != null) {
            classMethods.put(methodName, ctx.destructorBody());
        }
        return null;
    }

    
    @Override
    public Object visitObjectCreation(delphiParser.ObjectCreationContext ctx) {
    String className = ctx.classIdentifier().getText().toLowerCase();
    String objectName = ctx.IDENT().getText().toLowerCase();
    currentClass = className;
    currentObject = objectName;
    
    List<Object> paramValues = new ArrayList<>();
    if (ctx.parameterList() != null) {
        for (delphiParser.ValueContext valueCtx : ctx.parameterList().value()) {
            paramValues.add(visit(valueCtx));
        }
    }

    Map<String, Object> objectFields = new HashMap<>();
    objects.put(objectName, objectFields);
    objectClasses.put(objectName, className);
    
    // Call the constructor
    String constructorName = "create";
    Map<String, ParseTree> classMethods = methods.get(className);
    if (classMethods != null && classMethods.containsKey(constructorName)) {
        ParseTree constructorBody = classMethods.get(constructorName);
        
        // Get constructor parameters
        delphiParser.ConstructorImplContext constructorCtx = (delphiParser.ConstructorImplContext) constructorBody.getParent();
        List<delphiParser.FormalParameterSectionContext> params = constructorCtx.formalParameterList().formalParameterSection();
        
        // Assign parameter values to fields
        for (int i = 0; i < Math.min(params.size(), paramValues.size()); i++) {
            String paramName = params.get(i).identifierList().IDENT(0).getText().toLowerCase();
            objectFields.put(paramName, paramValues.get(i));
        }
        
        visit(constructorBody);
    }

    currentClass = null;
    currentObject = null;
    return null;
    }

    @Override
    public Object visitDestructorCall(delphiParser.DestructorCallContext ctx) {
    String objectName = ctx.objectIdentifier().getText().toLowerCase();
    String className = objectClasses.get(objectName);
    currentClass = className;
    currentObject = objectName;
    
    Map<String, ParseTree> classMethods = methods.get(className);
    if (classMethods != null && classMethods.containsKey("destroy")) {
        visit(classMethods.get("destroy"));
    }
    objects.remove(objectName);
    objectClasses.remove(objectName);
    currentClass = null;
    currentObject = null;
    return null;
    }


    @Override
    public Object visitWriteStatement(delphiParser.WriteStatementContext ctx) {
    StringBuilder output = new StringBuilder();
    
    if (ctx.STRING_LITERAL() != null) {
        for (TerminalNode str : ctx.STRING_LITERAL()) {
            String text = str.getText();
            text = text.substring(1, text.length() - 1);
            output.append(text);
        }
    }

    if (ctx.variable() != null) {
        for (delphiParser.VariableContext var : ctx.variable()) {
            Object value = visit(var);
            if (value != null) {
                output.append(value);
            }
        }
    }
    
    if (ctx.WRITELN() != null) {
        System.out.println(output.toString());
    } else {
        System.out.print(output.toString());
    }
    return null;
    }


    @Override
    public Object visitReadStatement(delphiParser.ReadStatementContext ctx) {
        String varName = ctx.variable().getText().toLowerCase();
        String input = scanner.nextLine();
        variables.put(varName, input);
        return null;
    }

    @Override 
    public Object visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx) {
    String varName = ctx.variable().getText().toLowerCase();
    Object value;
    
    if (ctx.expr().functionCall() != null) {
        value = visit(ctx.expr().functionCall());
    } else {
        value = visit(ctx.expr());
    }
    
    if (currentObject != null) {
        // Handle assignment to current object's fields
        Map<String, Object> objectFields = objects.get(currentObject);
        if (objectFields != null) {
            objectFields.put(varName, value);
        }
    } else {
        // Handle assignment to global variables
        variables.put(varName, value);
    }
    
    return null;
   } 

    @Override
    public Object visitFunctionImpl(delphiParser.FunctionImplContext ctx) {
    String className = ctx.variable().classIdentifier().getText().toLowerCase();
    String methodName = ctx.variable().IDENT().getText().toLowerCase();
    Map<String, ParseTree> classMethods = methods.get(className);
    if (classMethods != null) {
        classMethods.put(methodName, ctx.compoundStatement());
    }
    return null;
    }

    @Override
    public Object visitFunctionCall(delphiParser.FunctionCallContext ctx) {
    String objectName = ctx.classIdentifier().getText().toLowerCase();
    String methodName = ctx.IDENT().getText().toLowerCase();
    String className = objectClasses.get(objectName);
    if (!objects.containsKey(objectName)) {
            System.out.println("Error: Object '" + objectName + "' does not exist");
            throw new RuntimeException("Object not found");
        }
    Map<String, ParseTree> classMethods = methods.get(className);
    if (classMethods != null && classMethods.containsKey(methodName)) {
        currentObject = objectName; //since we are assuming functions are only class methods
        Object r = visit(classMethods.get(methodName));
        Map<String, Object> objectFields = objects.get(currentObject);
        currentObject = null;
        return objectFields.get("result");
    }
    return null;
    }
}