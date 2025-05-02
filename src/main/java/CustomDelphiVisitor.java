import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.*;
import java.io.*;

public class CustomDelphiVisitor extends delphiBaseVisitor<Object> {
    private Map<String, Object> globalVariables = new HashMap<>();
    private Map<String, Map<String, Object>> objects = new HashMap<>();
    private Map<String, String> objectClasses = new HashMap<>();
    private Map<String, Map<String, ParseTree>> methods = new HashMap<>();
    private Map<String, Object>localVariables = new HashMap<>(); // For function/procedure curr local variables
    private Scanner scanner = new Scanner(System.in);
    private String currentClass = null;
    private String currentObject = null;
    private String currentFunction = null; // Track current function/procedure
    private boolean breakEncountered = false; //track loop control
    private boolean continueEncountered = false; //track loop control
    private int loopDepth = 0;  // Track nested loop depth because continue/break needs to be inside a loop. if not invalid. 
    private enum LoopControlSignal {
        BREAK,
        CONTINUE
    }

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
        Map<String, Object> targetMap;
        if (currentFunction != null) {
            // Local variables in function/procedure
            targetMap = localVariables;
        }
        else {
            // Global variables
            targetMap = globalVariables;
        }
        
        for (delphiParser.VariableDeclarationContext varDecl : ctx.variableDeclaration()) {
            for (TerminalNode ident : varDecl.identifierList().IDENT()) {
                targetMap.put(ident.getText().toLowerCase(), "");
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
            Object result = visit(stmt);
            if (result instanceof LoopControlSignal) {
                return result;  // Propagate break/continue up
            }
        }
        return null;
    }

    @Override
    public Object visitCompoundStatement(delphiParser.CompoundStatementContext ctx) {
    return visit(ctx.statements());
    }

    @Override
    public Object visitExpr(delphiParser.ExprContext ctx) {
        if (ctx.stringExpr() != null) {
            return visit(ctx.stringExpr());
        }
        if (ctx.arithmeticExpr() != null) {
            return visit(ctx.arithmeticExpr());
        }
        if (ctx.methodCall() != null) {
            return visit(ctx.methodCall());
        }
        return "";
    }

    @Override
    public Object visitStringExpr(delphiParser.StringExprContext ctx) {
        StringBuilder result = new StringBuilder();
        List<delphiParser.ValueContext> values = ctx.value();
        
        // Handle first value
        result.append(visit(values.get(0)));
        
        // Handle subsequent concatenations
        for (int i = 1; i < values.size(); i++) {
            result.append(visit(values.get(i)));
        }
        return result.toString();
    }

    @Override
    public Object visitArithmeticExpr(delphiParser.ArithmeticExprContext ctx) {
    List<delphiParser.ValueContext> values = ctx.value();
    Object result = visit(values.get(0));
    
    for (int i = 1; i < values.size(); i++) {
        // double current = Double.parseDouble(result.toString());
        // double next = Double.parseDouble(visit(values.get(i)).toString());
        int current = Integer.parseInt(result.toString());
        int next = Integer.parseInt(visit(values.get(i)).toString());


        if (ctx.PLUS(i-1) != null) {
            result = String.valueOf(current + next);
        } else if (ctx.MINUS(i-1) != null) {
            result = String.valueOf(current - next);
        }
    }
    return result;
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
        if (ctx.methodCall() != null) return visit(ctx.methodCall());
        if (ctx.whileStatement() != null) return visit(ctx.whileStatement());
        if (ctx.forStatement() != null) return visit(ctx.forStatement());
        if (ctx.breakStatement() != null) return visit(ctx.breakStatement());
        if (ctx.continueStatement() != null) return visit(ctx.continueStatement());
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
        if (localVariables.containsKey(varName)) {
            return localVariables.get(varName);
        }
        else {
            return globalVariables.get(varName);
        } 
    }

    @Override
    public Object visitDestructorBody(delphiParser.DestructorBodyContext ctx) {
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
        if (currentFunction != null) {
            localVariables.put(varName, input);
        } else {
            globalVariables.put(varName, input);
        }
        return null;
    }

    @Override 
    public Object visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx) {
    String varName = ctx.variable().getText().toLowerCase();
    Object value;
    
    if (ctx.expr().methodCall() != null) {
        value = visit(ctx.expr().methodCall());
    } else {
        value = visit(ctx.expr());
    }
    
    if (currentObject != null) {
        // Handle assignment to current object's fields
        Map<String, Object> objectFields = objects.get(currentObject);
        if (objectFields != null) {
            objectFields.put(varName, value);
        }
    } 
    
    else if (currentFunction != null) {
            //System.out.println("here during asignment for varName " + varName + ":" + value + ";" + localVariables);
            localVariables.put(varName, value);
        }
    else {
            globalVariables.put(varName, value);
        }
    
    return null;
   } 

    @Override
    public Object visitFunctionImpl(delphiParser.FunctionImplContext ctx) {
    String functionName = ctx.variable().IDENT().getText().toLowerCase();
    if (ctx.variable() != null && ctx.variable().classIdentifier() != null) {
        // Handle class method
        String className = ctx.variable().classIdentifier().getText().toLowerCase();
        Map<String, ParseTree> classMethods = methods.get(className);
        if (classMethods != null) {
            classMethods.put(functionName, ctx.compoundStatement());
        }
    } else {
        // Handle global function
        methods.putIfAbsent("global", new HashMap<>());
        methods.get("global").put(functionName, ctx.compoundStatement());
    }
    return null;
    }

    @Override
    public Object visitProcedureImpl(delphiParser.ProcedureImplContext ctx) {
        String procedureName = ctx.variable().IDENT().getText().toLowerCase();
        if (ctx.variable() != null && ctx.variable().classIdentifier() != null) {
            // Handle class method
            String className = ctx.variable().classIdentifier().getText().toLowerCase();
            Map<String, ParseTree> classMethods = methods.get(className);
            if (classMethods != null) {
                classMethods.put(procedureName, ctx.compoundStatement());
            }
        } else {
            // Handle global procedure
            methods.putIfAbsent("global", new HashMap<>());
            methods.get("global").put(procedureName, ctx.compoundStatement());
        }
        return null;
    }

    @Override
    public Object visitMethodCall(delphiParser.MethodCallContext ctx) {
    String methodName = ctx.IDENT().getText().toLowerCase();
    String objectName = ctx.classIdentifier() != null ? ctx.classIdentifier().getText().toLowerCase() : null;
    String className = objectName != null ? objectClasses.get(objectName) : "global";

    // Store context
    String prevClass = currentClass;
    String prevObject = currentObject;
    String prevFunction = currentFunction;
    Map<String, Object> prevLocalVars = new HashMap<>(localVariables);

    // Set new context
    currentClass = className;
    currentObject = objectName;
    currentFunction = methodName;

    // Handle parameters
    List<Object> paramValues = new ArrayList<>();
    if (ctx.parameterList() != null) {
        for (delphiParser.ValueContext valueCtx : ctx.parameterList().value()) {
            paramValues.add(visit(valueCtx));
        }
    }

    if (currentObject != null){//this means function is a class method

    if (!objects.containsKey(currentObject)) {
        System.out.println("Error: Object '" + currentObject + "' does not exist");
        throw new RuntimeException("Object not found");
    }
    }


    Object result = null;
    Map<String, ParseTree> methodMap = (currentObject != null) ? 
        methods.get(currentClass) : methods.get("global");

    if (methodMap != null && methodMap.containsKey(methodName)) {
        ParseTree methodBody = methodMap.get(methodName);
        ParseTree parent = methodBody.getParent();
        
        // Check if it's a function or procedure based on parent context
        boolean isFunction = (parent instanceof delphiParser.FunctionImplContext);
        
        // Setup local variables and execute method
        setupMethodParameters(parent, paramValues);
        visit(methodBody);
        
        // Handle return value for functions
        if (isFunction) {
            result = currentObject != null ? 
                objects.get(currentObject).get("result") :
                localVariables.get("result");
        }
    }

    // Restore context
    currentClass = prevClass;
    currentObject = prevObject;
    currentFunction = prevFunction;
    localVariables = prevLocalVars;

    return result;
    }

    private void setupMethodParameters(ParseTree methodParent, List<Object> paramValues) {
    List<delphiParser.FormalParameterSectionContext> params = new ArrayList<>();
    
    if (methodParent instanceof delphiParser.FunctionImplContext) {
        delphiParser.FunctionImplContext funcCtx = (delphiParser.FunctionImplContext) methodParent;
        if (funcCtx.formalParameterList() != null) {
            params = funcCtx.formalParameterList().formalParameterSection();
        }
    } else if (methodParent instanceof delphiParser.ProcedureImplContext) {
        delphiParser.ProcedureImplContext procCtx = (delphiParser.ProcedureImplContext) methodParent;
        if (procCtx.formalParameterList() != null) {
            params = procCtx.formalParameterList().formalParameterSection();
        }
    }

    localVariables.clear();
    for (int i = 0; i < Math.min(params.size(), paramValues.size()); i++) {
        String paramName = params.get(i).identifierList().IDENT(0).getText().toLowerCase();
        localVariables.put(paramName, paramValues.get(i));
    }
    }

    public Object visitWhileStatement(delphiParser.WhileStatementContext ctx) {
    loopDepth++;
    while (evaluateBoolean(ctx.booleanExpr())) {
        Object result = visit(ctx.compoundStatement());
        if (breakEncountered) {
            breakEncountered = false;
            break;
        }
        if (continueEncountered) {
            continueEncountered = false;
            continue;
        }
        // Check if compound statement returned a control signal
        if (result instanceof LoopControlSignal) {
            if (result == LoopControlSignal.BREAK) {
                break;
            }
            if (result == LoopControlSignal.CONTINUE) {
                continue;
            }
        }
    }
    loopDepth--;
    return null;
    }   

    @Override
    public Object visitForStatement(delphiParser.ForStatementContext ctx) {
    loopDepth++;
    String loopVar = ctx.variable().getText().toLowerCase();
    Object startVal = visit(ctx.value(0));
    Object endVal = visit(ctx.value(1));
    
    try {
        int start = Integer.parseInt(startVal.toString());
        int end = Integer.parseInt(endVal.toString());
        
        if (ctx.TO() != null) {
            for (int i = start; i <= end; i++) {
                if (currentFunction != null) {
                    localVariables.put(loopVar, String.valueOf(i));
                } else {
                    globalVariables.put(loopVar, String.valueOf(i));
                }
                Object result = visit(ctx.compoundStatement());
                if (result instanceof LoopControlSignal) {
                    if (result == LoopControlSignal.BREAK) {
                        break;
                    }
                    if (result == LoopControlSignal.CONTINUE) {
                        continue;
                    }
                }
            }
        } else { // DOWNTO
            for (int i = start; i >= end; i--) {
                if (currentFunction != null) {
                    localVariables.put(loopVar, String.valueOf(i));
                } else {
                    globalVariables.put(loopVar, String.valueOf(i));
                }
                Object result = visit(ctx.compoundStatement());
                if (result instanceof LoopControlSignal) {
                    if (result == LoopControlSignal.BREAK) {
                        break;
                    }
                    if (result == LoopControlSignal.CONTINUE) {
                        continue;
                    }
                }
            }
        }
    } catch (NumberFormatException e) {
        System.out.println("Error: Invalid number in for loop range");
        throw new RuntimeException("Invalid number in for loop range");
    }
    loopDepth--;
    return null;
    }


    private boolean evaluateBoolean(delphiParser.BooleanExprContext ctx) {
    Object left = visit(ctx.value(0));
    Object right = visit(ctx.value(1));
    String op = ctx.compareOp().getText();

    // Convert operands to numbers if possible
    int leftNum = 0, rightNum = 0;
    boolean isNumeric = true;
    try {
        leftNum = Integer.parseInt(left.toString());
        rightNum = Integer.parseInt(right.toString());
    } catch (NumberFormatException e) {
        isNumeric = false;
    }

    if (isNumeric) {
        switch (op) {
            case "=": return leftNum == rightNum;
            case "<>": return leftNum != rightNum;
            case "<": return leftNum < rightNum;
            case ">": return leftNum > rightNum;
            case "<=": return leftNum <= rightNum;
            case ">=": return leftNum >= rightNum;
        }
    } else {
        // String comparison
        String leftStr = left.toString();
        String rightStr = right.toString();
        switch (op) {
            case "=": return leftStr.equals(rightStr);
            case "<>": return !leftStr.equals(rightStr);
            case "<": return leftStr.compareTo(rightStr) < 0;
            case ">": return leftStr.compareTo(rightStr) > 0;
            case "<=": return leftStr.compareTo(rightStr) <= 0;
            case ">=": return leftStr.compareTo(rightStr) >= 0;
        }
    }
    return false;
    }

    @Override
    public Object visitBreakStatement(delphiParser.BreakStatementContext ctx) {
        if (loopDepth == 0) {
            throw new RuntimeException("Break statement outside of loop");
        }
        return LoopControlSignal.BREAK;
    }

    @Override
    public Object visitContinueStatement(delphiParser.ContinueStatementContext ctx) {
        if (loopDepth == 0) {
            throw new RuntimeException("Continue statement outside of loop");
        }
        return LoopControlSignal.CONTINUE;
    }

}