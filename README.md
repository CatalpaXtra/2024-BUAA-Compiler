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
建立以单词名称为key，单词类别为value的Map，用于单词的识别。使用`getOrDefault` ，默认类别为**IDENFR**

新建`Token`类，含单词内容、单词类别、所在的行数。 词法分析时正常将解析到的每一token存入tokens； 若遇到错误 `a` ，将单词类别改为 `Token.Type.ERRA` ，存入error  
最后若error不为空则报错，否则输出tokens中全部内容

#### debug与重构
出现 '&' 和 '|' 时直接选择忽略，会导致语法分析出错。应该将其当做 '&&' 与 '||' 进行处理，读入`tokens`中

删除`Lexer`中原有的`errors`数组，新增`LexerErrors`类用于存储词法分析中的错误

新增`TokenIterator`类，便于语法分析中逐个读取token与及时回溯指针


### 语法分析
对编译单元`CompUnit`，有 `CompUnit → {Decl} {FuncDef} MainFuncDef`

在前端**frontend**目录下，新建**parser**软件包，以`frontend/parser/Parser.java`为顶层类，按`Decl` `FuncDef` `MainFuncDef`的出现顺序进行语法分析——即在`Parser`中依次进行 `parseDecls()` `parseFuncDefs()` `parseMainFuncDef()`

#### Decl
有 `Decl → ConstDecl | VarDecl` ，新建**declaration**软件包，实现对常量、变量声明的语法分析  
其顶层类为 `frontend/parser/declaration/DeclParser.java` ，负责对`Decl`进行语法分析

##### ConstDecl
有 `ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'`  
基本类型 `BType → 'int' | 'char'`  
常量定义 `ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal`

###### ConstExp<span id="UpConstExp"> </span>
若读到`[`，进行对`ConstExp`的分析，此时需新建软件包**expression**，以支持对表达式的语法分析

**表达式文法中存在左递归，因此需消除左递归后再编程实现**
- `ConstExp → AddExp`
- `AddExp → MulExp { ('+' | '−') MulExp }`
- `MulExp → UnaryExp { ('*' | '/' | '%') UnaryExp }`

再根据后续一系列规则建立语法树
```text
UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp

FuncRParams → Exp { ',' Exp }
UnaryOp → '+' | '−' | '!'
PrimaryExp → '(' Exp ')' | LVal | Number | Character

Exp → AddExp
LVal → Ident ['[' Exp ']']
Number → IntConst
Character → CharConst
```
可以注意到，`UnaryExp`有三种递推情况，因此需建立一个接口，使得解析后的结果均能被`UnaryExp`接受。在此新建接口`UnaryEle`。同理，对`PrimaryExp`，新建接口`PrimaryEle`

在处理到 `Number → IntConst` `Character → CharConst` 时，新建**terminal**软件包，为终结符集合，包括`Ident` `IntConst` `CharConst` `StringConst`

###### ConstInitVal<span id="UpConstInitVal"> </span>
有 `ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst`  
依托前文建立的软件包**expression**，便能轻松完成此任务。此外，仍需新建接口`ConstInitValEle`

##### VarDecl
有 `VarDecl → BType VarDef { ',' VarDef } ';'`  
变量定义 `VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal`

###### ConstExp
同<a href="#UpConstExp">上文提及</a>的处理步骤

###### InitVal
有 `InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst`  
同`ConstInitVal`的递推方式并无二致，故可<a href="#UpConstInitVal">参照上文</a>


#### FuncDef<span id="UpFuncDef"> </span>
有 `FuncDef → FuncType Ident '(' [FuncFParams] ')' Block` ，新建**function**软件包，实现对自定义函数声明的语法分析
其顶层类为 `frontend/parser/function/FuncDefParser.java` ，负责对`FuncDef`进行语法分析

##### Block
`FuncDef`中的分析难点，有 `Block → '{' { BlockItem } '}'`  
Block中的新增文法符号仅有BlockItem，有 `BlockItem → Decl | Stmt`  
由于Decl已处理完毕，此处新增文法仅有Stmt，有
```text
Stmt → LVal '=' Exp ';'
| [Exp] ';'
| Block
| 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt 
| 'break' ';' | 'continue' ';'
| 'return' [Exp] ';'
| LVal '=' 'getint''('')'';'
| LVal '=' 'getchar''('')'';'
| 'printf''('StringConst {','Exp}')'';'
```

`BlockItem`有两种递推情况，因此仍需建立一个接口，使得解析后的结果均能被`BlockItem`接受。在此新建接口`BlockItemEle`  
同理，对`Stmt`，新建接口`StmtEle`  
此处项目结构大致如下
```text
│  Block.java
│  BlockItem.java
│  BlockItemEle.java
│  BlockItemParser.java
│  BlockParser.java
│  
└─statement
    │  Stmt.java
    │  StmtEle.java
    │  StmtParser.java
    │  
    └─stmtVariant
            StmtBreak.java
            StmtBreakParser.java
            StmtContinue.java
            StmtContinueParser.java
            ⋮
```

##### Cond
有 `Cond → LOrExp`
```text
LOrExp → LAndExp | LOrExp '||' LAndExp
LAndExp → EqExp | LAndExp '&&' EqExp
EqExp → RelExp | EqExp ('==' | '!=') RelExp
RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
```

涉及到对软件包**expression**的内容扩充，新增条件判断表达式  
由于`AddExp` `MulExp`与上述表达式结构较为类似，新建`BaseExp`父类，定义为泛型类型，以满足不同表达式存储不同种类的下层表达式的需要。而上述所有表达式均继承此父类，避免重复大段代码  
**expression**软件包结构大致如下
```text
│  ConstExp.java
│  ConstExpParser.java
│  Exp.java
│  ExpParser.java
│  
├─add
├─cond
│      BaseExp.java
│      Cond.java
│      CondParser.java
│      
├─primary
└─unary
```


#### MainFuncDef
实现过程同<a href="#UpFuncDef">FuncDef</a>


#### 错误处理
在实现对`CompUnit`的语法分析后，进行错误处理。在语法分析中新增的错误有：
| 错误类型               | 错误类别码 | 解释                                                               | 对应文法及出错符号                                |
|:---------------------:|:---------:|:------------------------------------------------------------------:|:--------------------------------------------------:|
| 缺少分号             | i       | 报错行号为分号前一个非终结符所在行号                          | Stmt, ConstDecl 及 VarDecl 中的 ';'              |
| 缺少右小括号 ')'     | j       | 报错行号为右小括号前一个非终结符所在行号                      | 函数调用 (UnaryExp)、函数定义 (FuncDef, MainFuncDef)、Stmt 及 PrimaryExp 中的 ')' |
| 缺少右中括号 ']'     | k       | 报错行号为右中括号前一个非终结符所在行号                      | 数组定义 (ConstDef, VarDef, FuncFParam) 和使用 (LVal)中的 ']' |

错误**i**涉及到的情况
```text
Stmt → LVal '=' Exp ';' // i
| Exp ';' // i
| 'break' ';' | 'continue' ';' // i
| 'return' [Exp] ';' // i
| LVal '=' 'getint''('')'';' // i
| LVal '=' 'getchar''('')'';' // i
| 'printf''('StringConst {','Exp}')'';' // i
ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // i
VarDecl → BType VarDef { ',' VarDef } ';' // i
```

错误**j**涉及到的情况
```text
UnaryExp → Ident '(' [FuncRParams] ')' // j
PrimaryExp → '(' Exp ')' // j
FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // j
MainFuncDef → 'int' 'main' '(' ')' Block // j
Stmt → 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // j
| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 未出现相应测试点但仍应覆盖
| LVal '=' 'getint''('')'';' // j
| LVal '=' 'getchar''('')'';' // j
| 'printf''('StringConst {','Exp}')'';' // j
```

错误**k**涉及到的情况
```text
ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal // k
VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal // k
FuncFParam → BType Ident ['[' ']'] // k
LVal → Ident ['[' Exp ']'] // k
```

为此，新建`ErrorHandler`类，用于处理**语法分析**中的错误。将检测到的错误存入`ParserErrors`类中的静态数组`errors`中  
此外，对`Lexer`中的错误检测方案进行重构，同样设置`LexerErrors`类用于存储**词法分析**中的错误

**特别注意：** 在分析 `Stmt → 'return' [Exp] ';'` 的错误时，由于可能遇到代码结尾为 `return }` 的情况，会导致`TokenIterator`越界访问，因此需对此情况作特殊处理

在词法分析、语法分析均结束后，新建`CompErrors`类，存储`lexerErrors`与`parserErrors`。并用于最终的输出

