/*
 * Copyright © 2017-2019 Cask Data, Inc.
 * Copyright © 2023 Your Name/Org (if adding significant contributions)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

grammar Directives;

options {
  language = Java;
}

@lexer::header {
/*
 * Copyright © 2017-2019 Cask Data, Inc.
 * Copyright © 2023 Your Name/Org (if adding significant contributions)
 * (You might add imports here later if needed, e.g.:)
 * // import java.util.Map; 
 */
}

@parser::header {
/*
 * (You might add imports here later if needed)
 */
}

/**
 * Parser Grammar for recognizing tokens and constructs of the directives language.
 */
recipe
 : statements EOF
 ;

statements
 :  ( Comment | macro | directive ';' | pragma ';' | ifStatement)*
 ;

directive
 : command
   ( // Start optional argument list - REORDERED and use greedy '*'
     column         // Prioritize single column
     | colList        // Prioritize column list
     | byteSizeArg    // Prioritize our new types
     | timeDurationArg  // Prioritize our new types
     | text
     | number
     | bool
     | identifier     // Less specific types later
     | properties
     | codeblock
     | macro
     | numberList     // Other lists
     | boolList
     | stringList
     | numberRanges
   )* // Use greedy quantifier '*' instead of non-greedy '*?'
 ;

// ... (keep existing rules like ifStatement, expression, etc.) ...
ifStatement
  : ifStat elseIfStat* elseStat? '}'
  ;

ifStat
  : 'if' expression '{' statements
  ;

elseIfStat
  : '}' 'else' 'if' expression '{' statements
  ;

elseStat
  : '}' 'else' '{' statements
  ;

expression
  : '(' (~'(' | expression)* ')'
  ;

forStatement
 : 'for' '(' Identifier '=' expression ';' expression ';' expression ')' '{'  statements '}'
 ;

macro
 : Dollar OBrace (~OBrace | macro | Macro)*? CBrace
 ;

pragma
 : '#pragma' (pragmaLoadDirective | pragmaVersion)
 ;

pragmaLoadDirective
 : 'load-directives' identifierList
 ;

pragmaVersion
 : 'version' Number
 ;

codeblock
 : 'exp' Space* ':' condition
 ;

identifier
 : Identifier
 ;

properties
 : 'prop' ':' OBrace (propertyList)+  CBrace
 | 'prop' ':' OBrace OBrace (propertyList)+ CBrace { notifyErrorListeners("Too many start paranthesis"); }
 | 'prop' ':' OBrace (propertyList)+ CBrace CBrace { notifyErrorListeners("Too many start paranthesis"); }
 | 'prop' ':' (propertyList)+ CBrace { notifyErrorListeners("Missing opening brace"); }
 | 'prop' ':' OBrace (propertyList)+  { notifyErrorListeners("Missing closing brace"); }
 ;

propertyList
 : property (',' property)*
 ;

property
 : Identifier '=' ( text | number | bool )
 ;

numberRanges
 : numberRange ( ',' numberRange)*
 ;

numberRange
 : Number ':' Number '=' value
 ;

value
 : String | Number | Column | Bool
 ;

ecommand
 : '!' Identifier
 ;

config
 : Identifier
 ;

column
 : Column
 ;

text
 : String
 ;

number
 : Number
 ;

bool
 : Bool
 ;

byteSizeArg     // ADDED: New parser rule for byte size args
 : BYTE_SIZE
 ;

timeDurationArg // ADDED: New parser rule for time duration args
 : TIME_DURATION
 ;

keywordArg
  : Identifier Colon ( text           // e.g., time_unit: 'ms'
                     | number         // e.g., threshold: 1.5
                     | bool           // e.g., ignore_case: true
                     | column         // e.g., size_source: :body
                     | byteSizeArg    // e.g., limit: 10MB
                     | timeDurationArg // e.g., timeout: 5s
                     // Add other simple value types here if needed (e.g., | properties)
                     )
  ;

condition
 : OBrace (~CBrace | condition)* CBrace
 ;

command
 : Identifier
 ;

colList
 : Column (','  Column)+
 ;

numberList
 : Number (',' Number)+
 ;

boolList
 : Bool (',' Bool)+
 ;

stringList
 : String (',' String)+
 ;

identifierList
 : Identifier (',' Identifier)*
 ;


/*
 * =============================================================================
 * Following are the Lexer Rules used for tokenizing the recipe.
 * =============================================================================
 */

// --- Fragments (Helper rules, do not create tokens directly) ---
fragment Int
 : '-'? [1-9] Digit*
 | '0'
 ;

fragment Digit
 : [0-9]
 ;

fragment NumberPattern // ADDED: Fragment for number part reuse
 : Int ('.' Digit*)?
 ;

fragment BYTE_UNIT    // ADDED: Fragment for byte units (case-insensitive)
 : [KkMmGgTtPp]? [Bb]
 ;

fragment TIME_UNIT    // ADDED: Fragment for time units (case-insensitive)
 : [Nn][Ss] | [Uu][Ss] | [Mm][Ss] | [Ss] | [Mm][Ii][Nn] | [Hh]
 ;

fragment EscapeSequence
   :   '\\' ([btnfr"'\\] | UnicodeEscape | OctalEscape) // Simplified slightly
   ;

fragment OctalEscape
   :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
   |   '\\' ('0'..'7') ('0'..'7')
   |   '\\' ('0'..'7')
   ;

fragment UnicodeEscape
   :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
   ;

fragment HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;


// --- Tokens (Actual rules creating tokens) ---
OBrace   : '{';
CBrace   : '}';
SColon   : ';';
// ... (keep existing operator/symbol tokens like Or, And, Equals, etc.) ...
Or       : '||';
And      : '&&';
Equals   : '==';
NEquals  : '!=';
GTEquals : '>=';
LTEquals : '<=';
Match    : '=~';
NotMatch : '!~';
QuestionColon : '?:';
StartsWith : '=^';
NotStartsWith : '!^';
EndsWith : '=$';
NotEndsWith : '!$';
PlusEqual : '+=';
SubEqual : '-=';
MulEqual : '*=';
DivEqual : '/=';
PerEqual : '%=';
AndEqual : '&=';
OrEqual  : '|=';
XOREqual : '^=';
Pow      : '^';
External : '!';
GT       : '>';
LT       : '<';
Add      : '+';
Subtract : '-';
Multiply : '*';
Divide   : '/';
Modulus  : '%';
OBracket : '[';
CBracket : ']';
OParen   : '(';
CParen   : ')';
Assign   : '=';
Comma    : ',';
QMark    : '?';
Colon    : ':';
Dot      : '.';
At       : '@';
Pipe     : '|';
BackSlash: '\\';
Dollar   : '$';
Tilde    : '~';


// ADDED: New Tokens - IMPORTANT: Place these BEFORE the 'Number' token rule
BYTE_SIZE      : NumberPattern BYTE_UNIT ;
TIME_DURATION  : NumberPattern TIME_UNIT ;

// Existing Tokens
Bool
 : 'true'
 | 'false'
 ;

// IMPORTANT: Number is placed *after* BYTE_SIZE and TIME_DURATION
Number
 : NumberPattern // Use the fragment for consistency
 ;

Identifier
 : [a-zA-Z_\-] [a-zA-Z_0-9\-]*
 ;

Macro
 : [a-zA-Z_] [a-zA-Z_0-9]*
 ;

Column
 : ':' [a-zA-Z_\-] [:a-zA-Z_0-9\-]*
 ;

String
 : '\'' ( EscapeSequence | ~['\\] )* '\'' // Adjusted non-escape slightly
 | '"'  ( EscapeSequence | ~["\\] )* '"'  // Adjusted non-escape slightly
 ;

Comment
 : ('//' ~[\r\n]* | '/*' .*? '*/' | '--' ~[\r\n]* ) -> skip
 ;

Space
 : [ \t\r\n\u000C]+ -> skip
 ;