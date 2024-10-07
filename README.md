# BUAA-2024-Compiler

## 前端部分
### 词法分析
#### 大致思路
跳过读到的空白符`' '`，换行符`'\n'`<br>
单行注释，忽略后续内容。多行注释，出现 `/*` 则一直忽略内容，直至读到 `*/` 为止

数字，以 `'0'-'9'` 开头<br>
字符，以 `'\''` 开头 ；字符串，以 `'\"'` 开头。若读到转义符 `'\\'`，则需将转义字符一并读入字符或字符串中<br>
单词，以 `'a'-'z' || 'A'-'Z' || '_'` 开头 ，若单词未经特殊命名，则统一识别为**IDENFR**

最后进行符号识别，如有必要向后读取一个字符（如有）即可

#### 相关操作
建立以单词名称为key，单词类别为value的Map，用于单词的识别。使用 `getOrDefault` ，默认类别为**IDENFR**

新建 `Token` 类，含单词内容、单词类别、所在的行数。 词法分析时正常将解析到的每一token存入tokens； 若遇到错误 `a` ，将单词类别改为 `Token.Type.ERRA` ，存入error<br>
最后若error不为空则报错，否则输出tokens中全部内容

### 语法分析
按 `CompUnit → {Decl} {FuncDef} MainFuncDef` 的顺序，首先对声明 `Decl` 进行分析
有 `Decl → ConstDecl | VarDecl` 

对`ConstDecl`，有 `ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'`
基本类型 `BType → 'int' | 'char'`
常量定义 `ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal`
若读到`[`，进行对ConstExp的分析，在读到`=`之后进行对ConstInitVal的分析

对ConstExp，有ConstExp → AddExp
AddExp → MulExp | AddExp ('+' | '−') MulExp 
MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp 
UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp

PrimaryExp → '(' Exp ')' | LVal | Number | Character
Exp → AddExp
LVal → Ident ['[' Exp ']']
Number → IntConst 
Character → CharConst 

FuncRParams → Exp { ',' Exp }

UnaryOp → '+' | '−' | '!'
