grammar delphi;

options {
    caseInsensitive = true;
}

// Basic program structure
program
    : programHeading block DOT EOF
    ;

programHeading
    : PROGRAM IDENT (LPAREN identifierList RPAREN)? SEMI
    ;

// Block structure (from original grammar)
block
    : (
        variableDeclarationPart
        | classDeclarationPart
        | procedureImpl
        | functionImpl
        | constructorImpl
        | destructorImpl
    )* compoundStatement
    ;


formalParameterList
    : LPAREN formalParameterSection (SEMI formalParameterSection)* RPAREN
    ;

formalParameterSection
    : parameterModifier? identifierList COLON simpleType
    ;

parameterModifier
    : CONST
    | VAR
    ;

identifierList
    : IDENT (COMMA IDENT)*
    ;
    
// Constructor implementation (outside class)
constructorImpl
    : CONSTRUCTOR (classIdentifier DOT constructorIdentifier) (formalParameterList)? SEMI compoundStatement SEMI
    ;

// Destructor implementation (outside class)
destructorImpl
    : DESTRUCTOR (classIdentifier DOT IDENT) SEMI destructorBody SEMI
    ;

destructorBody
    : BEGIN
      statement*
      inheritedDestructorCall?     // Optional but should be last if present
      END
    ;

inheritedDestructorCall
    : INHERITED (destructorIdentifier)? SEMI
    ;

procedureImpl
    : PROCEDURE variable (formalParameterList)? SEMI compoundStatement SEMI
    ;

functionImpl
    : FUNCTION variable (formalParameterList)? COLON resultType SEMI compoundStatement SEMI
    ;

resultType
    : simpleType
    ;

// Add classIdentifier rule
classIdentifier
    : IDENT
    ;

objectIdentifier
    : IDENT
    ; 

destructorIdentifier
    : IDENT
    ;

constructorIdentifier
    : CREATE
    ;

compoundStatement
    : BEGIN statements END
    ;


statements
    : (statement)*
    ;

classDeclarationPart
    : TYPE classDeclaration (SEMI classDeclaration)* SEMI
    ;

classDeclaration
    : IDENT EQUAL classType
    ;

variableDeclarationPart
    : VAR variableDeclaration (SEMI variableDeclaration)* SEMI
    ;

variableDeclaration
    : identifierList COLON type_
    ;

// Class-related keywords
CLASS       : 'CLASS';
PRIVATE     : 'PRIVATE';
PROTECTED   : 'PROTECTED';
PUBLIC      : 'PUBLIC';
CONSTRUCTOR : 'CONSTRUCTOR';
DESTRUCTOR  : 'DESTRUCTOR';
OVERRIDE    : 'OVERRIDE';
INHERITED   : 'INHERITED';
CREATE      : 'CREATE';
PROPERTY    : 'PROPERTY';

// Console I/O keywords
WRITELN    : 'WRITELN';
READLN     : 'READLN';
WRITE      : 'WRITE';
READ       : 'READ';

// Class definition
classType
    : CLASS 
      (classVisibility | classBody)  // classBody is Default - if no visibility section (public by default)
      END
    ;

// Visibility sections
classVisibility
    : (PRIVATE classBody)?
      (PROTECTED classBody)?
      (PUBLIC classBody)?
    ;

// Class body
classBody
    : (classField
    | classMethod
    | constructorDecl
    | destructorDecl
    | propertyDecl)*
    ;


// Field declaration
classField
    : IDENT COLON type_ SEMI
    ;

// Method declaration
classMethod
    : procedureDecl
    | functionDecl
    ;


// Constructor declaration
constructorDecl
    : CONSTRUCTOR constructorIdentifier (formalParameterList)? SEMI
    ;

// Destructor declaration
destructorDecl
    : DESTRUCTOR destructorIdentifier SEMI (OVERRIDE SEMI)?
    ;

procedureDecl
    : PROCEDURE IDENT (formalParameterList)? SEMI
    ;

functionDecl
    : FUNCTION IDENT (formalParameterList)? COLON resultType SEMI
    ;

propertyDecl
    : PROPERTY IDENT COLON type_
      (READ IDENT)?
      (WRITE IDENT)?
      SEMI
    ;

// Console I/O statements
consoleStatement
    : writeStatement
    | readStatement
    ;

writeStatement
    : (WRITE | WRITELN) LPAREN (variable | STRING_LITERAL) (COMMA (variable | STRING_LITERAL))* RPAREN
    ;

readStatement
    : (READ | READLN) LPAREN variable RPAREN
    ;


// Object instantiation
objectCreation
    : IDENT ASSIGN classIdentifier DOT constructorIdentifier (LPAREN parameterList RPAREN)?
    ;

// Basic statements
statement
    : 
    (assignmentStatement
    | consoleStatement
    | objectCreation
    //| constructorCall
    | destructorCall
    | functionCall
    | procedureCall) SEMI
    ;

// Assignment statement rule
assignmentStatement
    : variable ASSIGN expr
    ;

// Variable can be a simple IDENT or field access
variable
    : IDENT
    | classIdentifier DOT IDENT  // For field access like FName or object.field
    ;

expr 
    : value (CONCAT value)*
    | functionCall
    | procedureCall
    ;

// Value can be number, string, or another variable
value
    : variable          // Another variable (b in a:=b)
    | NUMBER
    | STRING_LITERAL
    ;

parameterList
    :(value (COMMA value)*)?
    ;

// destructorCall rule
destructorCall
    : objectIdentifier DOT (FREE | destructorIdentifier)
    ;

//Constructor call
functionCall
    : classIdentifier DOT IDENT (LPAREN parameterList RPAREN)?
    ;

//Procedure call
procedureCall
    : classIdentifier DOT IDENT (LPAREN parameterList RPAREN)?
    ;

// Type definitions
type_
    : simpleType
    | classType
    | IDENT
    ;

simpleType
    : INTEGER
    | REAL
    | STRING
    | BOOLEAN
    ;

// Tokens
INTEGER    : 'INTEGER';
REAL      : 'REAL';
STRING    : 'STRING';
BOOLEAN   : 'BOOLEAN';
VAR       : 'VAR';
BEGIN     : 'BEGIN';
END       : 'END';
PROGRAM   : 'PROGRAM';
PROCEDURE : 'PROCEDURE';
FUNCTION  : 'FUNCTION';
TYPE      : 'TYPE';
FREE      : 'FREE';
CONST     : 'CONST';
CONCAT    : '+';
DOT       : '.';
SEMI      : ';';
ASSIGN    : ':=';
COLON     : ':';
COMMA     : ',';
LPAREN    : '(';
RPAREN    : ')';
EQUAL     : '=';

// Basic literals
IDENT         : [a-z][a-z0-9_]*;
STRING_LITERAL: '\'' (~['])* '\'';

// Whitespace and comments
WS           : [ \t\r\n]+ -> skip;
NUMBER       : [0-9]+ ('.' [0-9]+)?;
COMMENT      : '{' .*? '}' -> skip;