# BUAA-2024-Compiler

## 前端部分
### 词法分析
#### 大致思路
跳过读到的空白符`' '`，换行符`'\n'`  
单行注释，忽略后续内容。多行注释，出现 `/*` 则一直忽略内容，直至读到 `*/` 为止

数字，以 `'0'-'9'` 开头  
字符，以 `'\''` 开头 ；字符串，以 `'\"'` 开头。若读到转义符 `'\\'`，则需将转义字符一并读入字符或字符串中  
单词，以 `'a'-'z' || 'A'-'Z' || '_'` 开头 ，若单词未经特殊命名，则统一识别为**IDENFR**

最后进行符号识别，如有必要向后读取一个字符（如有）即可

#### 相关操作
建立以单词名称为key，单词类别为value的Map，用于单词的识别。使用 `getOrDefault` ，默认类别为**IDENFR**

新建 `Token` 类，含单词内容、单词类别、所在的行数。 词法分析时正常将解析到的每一token存入tokens； 若遇到错误 `a` ，将单词类别改为 `Token.Type.ERRA` ，存入error  
最后若error不为空则报错，否则输出tokens中全部内容



### 语法分析
新建**parser**软件包，以`frontend/parser/Parser.java`为接口，按 `CompUnit → {Decl} {FuncDef} MainFuncDef` 顺序进行语法分析。  
即在`Parser`中依次进行 `parseDecls() parseFuncDefs() parseMainFuncDef()`

新建**terminal**软件包，为终结符集合，包括`Ident`,`IntConst`,`CharConst`,`StringConst`类

#### Decl
有 `Decl → ConstDecl | VarDecl` ，新建**declaration**软件包，实现对常量、变量声明的语法结构分析  
可参照 `frontend/parser/declaration/DeclParser.java` ，对`Decl`进行语法分析

##### ConstDecl
有 `ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'`  
基本类型 `BType → 'int' | 'char'`  
常量定义 `ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal`

###### ConstExp<span id="ConstExp"> </span>
若读到`[`，进行对`ConstExp`的分析，有`ConstExp → AddExp`，此时需新建软件包**expression**，以支持对表达式的语法分析  
**表达式文法中存在左递归，因此需消除左递归后再编程实现**  
即有`AddExp → MulExp { ('+' | '−') MulExp }`与`MulExp → UnaryExp { ('*' | '/' | '%') UnaryExp }`

再根据后续一系列规则建立语法树
```c
UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp

PrimaryExp → '(' Exp ')' | LVal | Number | Character
Exp → AddExp
LVal → Ident ['[' Exp ']']
Number → IntConst
Character → CharConst

FuncRParams → Exp { ',' Exp }

UnaryOp → '+' | '−' | '!'
```
可以注意到，`UnaryExp`有三种递推情况，因此需建立一个接口，使得解析后的结果均能被`UnaryExp`接受，在此，新建接口为`UnaryEle`。同理，对`PrimaryExp`有`PrimaryEle`

###### ConstInitVal<span id="ConstInitVal"> </span>
在读到`=`之后进行对`ConstInitVal`的分析，有 `ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst`  
依托前文建立的软件包`expression`，便能轻松完成此任务，并新建接口`ConstInitValEle`


##### VarDecl
有 `VarDecl → BType VarDef { ',' VarDef } ';'`  
变量定义 `VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal`

###### ConstExp
同<a href="#ConstExp">上文提及</a>的处理步骤

###### InitVal
若读到`=`，对`InitVal`进行分析，有 `InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst`  
同<a href="#ConstInitVal">上文提及</a>的处理步骤无太大区别

