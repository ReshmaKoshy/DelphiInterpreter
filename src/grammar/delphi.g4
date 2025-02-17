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
    : identifierList COLON simpleType
    ;

identifierList
    : IDENT (COMMA IDENT)*
    ;
    
// Constructor implementation (outside class)
constructorImpl
    : CONSTRUCTOR variable (formalParameterList)? SEMI compoundStatement
    ;

// Destructor implementation (outside class)
destructorImpl
    : DESTRUCTOR variable SEMI destructorBody
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
    : PROCEDURE variable (formalParameterList)? SEMI compoundStatement
    ;

functionImpl
    : FUNCTION variable (formalParameterList)? COLON resultType SEMI compoundStatement
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
    : IDENT
    ;

compoundStatement
    : BEGIN statements END
    ;


statements
    : (statement)* //(SEMI statement)* SEMI
    ;


// Add a new rule for class declarations
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
// CREATE      : 'CREATE';
PROPERTY    : 'PROPERTY';
// READ        : 'READ';
// WRITE       : 'WRITE';
DEFAULT     : 'DEFAULT';
STORED      : 'STORED';
IMPLEMENTS  : 'IMPLEMENTS';

// Console I/O keywords
WRITELN    : 'WRITELN';
READLN     : 'READLN';
WRITE      : 'WRITE';
READ       : 'READ';

// Class definition
classType
    : CLASS 
      (classVisibility | classBody)  // classBody is Default - if no visibility section (public by default in Delphi)
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
    : procedureDeclaration
    | functionDeclaration
    ;


// Constructor declaration (inside class)
constructorDecl
    : CONSTRUCTOR constructorIdentifier (formalParameterList)? SEMI
    ;

// Destructor declaration (inside class)
destructorDecl
    : DESTRUCTOR destructorIdentifier SEMI (OVERRIDE SEMI)? (destructorBody)?
    ;

procedureDeclaration
    : PROCEDURE IDENT (formalParameterList)? SEMI (compoundStatement)?
    ;

functionDeclaration
    : FUNCTION IDENT (formalParameterList)? COLON resultType SEMI (compoundStatement)?
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
    : IDENT ASSIGN classIdentifier DOT IDENT (LPAREN parameterList RPAREN)?
    ;

// Basic statements
statement
    : 
    (assignmentStatement
    | consoleStatement
    | objectCreation
    | constructorCall
    | destructorCall) SEMI
    ;

// Assignment statement rule
assignmentStatement
    : variable ASSIGN value
    ;

// Variable can be a simple IDENT or field access
variable
    : IDENT
    | classIdentifier DOT IDENT  // For field access like FName or object.field
    ;

// Value can be number, string, or another variable
value
    : IDENT          // Another variable (b in a:=b)
    | NUMBER             // Number literal (42 in a:=42)
    | STRING_LITERAL     // String literal ('hello' in a:='hello')
    ;

parameterList
    :value
    ;
// // Variable declaration
// variableDeclaration
//     : VAR? IDENT COLON type_ (ASSIGN value)? SEMI
//     ;

// Constructor call
constructorCall
    : classIdentifier DOT constructorIdentifier (LPAREN parameterList RPAREN)?
    ;

// destructorCall rule
destructorCall
    : objectIdentifier DOT (FREE | destructorIdentifier)
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
NUMBER        : [0-9]+ ('.' [0-9]+)?;

// Whitespace and comments
WS           : [ \t\r\n]+ -> skip;
COMMENT      : '{' .*? '}' -> skip;