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


#### debug与重构
为保证**语法分析出现错误后，能进行语义分析**，将缺失的`';'` `')'` `']'`在相应位置补全，并令其行号为前一个`Token`的行号

其余bug均为错误处理时遇到
- 对于函数定义 `FuncDef → FuncType Ident '(' [FuncFParams] ')' Block`
  若遇到以下错误样例，则原先代码会认为在`'('`之后的不是`')'`而进行`FuncFParams`的解析  
  因此新增if语句特判，当读到的`Token`为`'{'`时，认为缺省`FuncFParams`与`')'`，后续进行`Block`的解析
    ```c
    void f1({

    }
    ```

- 对于语句 `Stmt → LVal '=' Exp ';' | Exp ';'`
  若遇到以下错误样例，则原先代码会无限向后读取符号，直至读到`'='`，错误认为`a b c = 1 `为一个赋值语句  
  因此新增限制，当读到的`Token`行数与`Stmt`所在行数不一致时，便不再往下读取，认为此行为`Exp ';'`
    ```c
    int main (){
    a

    b

    c = 1
    }
    ```



## 中端部分
### 语义分析
要求：识别出定义的常量、变量、函数、形参，输出作用域序号、标识符字符串、类型名称  
在**src**根目录下新建**midend**软件包，以支持语义分析

#### 作用域序号
对语法分析得到的`CompUnit`，有 `CompUnit → {Decl} {FuncDef} MainFuncDef` ，按`Decl` `FuncDef` `MainFuncDef`的顺序进行语义分析

定义**全局作用域序号为1**，因此所有`Decl` `FuncDef`的作用域序号均为1。对`FuncDef`，其形参为所在的作用域为`FuncDef`内部的作用域  
每遇到语句块`Block`，则**作用域数量加1**，同时新建符号表

新建**symbol**软件包
- `Symbol`类，存储作用域序号、类型名称
- `SymbolTable`类，创建符号表`HashMap<String, Symbol> symbols`，以符号的name为key，Symbol为value，一个`Block`对应一个`SymbolTable`

#### 具体实现
对语法分析得到的内容，同样采用递归下降的方式进行语义分析。  
具体地，在`Semantic`类中对特定的文法，创建相应的`visit*()`方法，用于语义分析(如`AddExp`，有`visitAddExp()`方法)

#### 错误处理
在**mid**软件包下新建`ErrorHandler`类，当遇到可能出现语义分析中相应错误的文法递推时，调用`ErrorHandler`类中的`handleError*()`静态方法。  
注意到**每一行中最多只有一个错误**，因此在`visit*()`中，若检测出错误即可立即`return`

##### B
函数名或者变量名在**当前作用域**下重复定义
```text
ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal // b
VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal // b
FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // b
FuncFParam → BType Ident ['[' ']'] // b
```

##### C
使用了未定义的标识符  
标识符在**当前作用域与所有父作用域**中均不存在

```text
LVal → Ident ['[' Exp ']'] // c
UnaryExp → Ident '(' [FuncRParams] ')' // c
```

##### D
函数调用语句中，实参个数与函数定义中的形参个数不匹配  
**数组不参与任何形式的运算**

```text
UnaryExp → Ident '(' [FuncRParams] ')' // d
```

##### E
函数调用语句中，实参类型与函数定义中对应位置的形参类型不匹配

```text
UnaryExp → Ident '(' [FuncRParams] ')' // e
```

##### F
无返回值的函数存在不匹配的`return`语句  
检查`void`类型函数体内每一条`return`语句是否有返回值，若有值报f类错误

```text
Stmt → 'return' [Exp] ';' // f
```

##### G
有返回值的函数缺少`return`语句  
只需要考虑函数体**最后一条**语句，且只判断有没有`return`语句，不需要考虑`return`语句是否有返回值；也不需要检查函数体内其他的`return`语句是否有值

```text
FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // g
MainFuncDef → 'int' 'main' '(' ')' Block // g
```

##### H
`LVal`为常量时，不能对其修改  
不需处理被赋值的是数组整体、函数的情况

```text
Stmt → LVal '=' Exp ';' // h
| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // h
| LVal '=' 'getint''('')'';' // h
| LVal '=' 'getchar''('')'';' // h

ForStmt → LVal '=' Exp // h
```

##### L
printf中格式字符与表达式个数不匹配  
格式字符**只包含** `%d`与`%c` ，其他C语言中的格式字符，如`%f`都当做普通字符原样输出

```text
Stmt → 'printf''('StringConst {','Exp}')'';' // l
```

##### M
在非循环块中使用`break`和`continue`语句

对自定义函数、main函数进行语义分析时，对其中的`analyzeBlock`方法中的形参`isInFor`赋初始值`false`；若后续遇到`StmtFor`，分析其内的`Block`时，为`isInFor`赋`true`

```text
Stmt → 'break' ';' | 'continue' ';' // m
```


#### debug与重构
错误处理
- e
    ```c
    int a;
    func(a[0]);
    ```
- g
  若函数语句块为空，查询`Block`中最后一个`Stmt`时有数组越界访问的风险


### LLVM中间代码生成
#### 变量
将`char`改为`i8`，将`int`改为`i32`  
将`char`类型初值改为`int`型，如`int c = 'a'`，等价为`int c = 97`

全局
- 对于 `InitVal` 初值，需要**直接算出**其具体的值
- 使用全局标识符 `@`
- 格式为`@<Ident> = dso_local global i32 <InitVal, default=0>`

局部
- 使用标识符 `%`
- 首先需要通过 `alloca` 指令分配一块内存，才能对其进行 `load/store` 操作

对 `Const` 变量，存储其不可修改值。便于后续引用时的代值计算

新建`RetValue`类，记录`visit*Exp`返回值为数字还是为寄存器类型

例如：
```c
int b = 3;

int main() {
    int c = b + 4;
    return 0;
}
```

其llvm代码为：
```llvm
@b = dso_local global i32 3

define dso_local i32 @main() {
    %1 = alloca i32          ; 分配 c
    %2 = load i32, i32* @b   ; 加载 b 到内存
    %3 = add nsw i32 %2, 4   ; b + 4
    store i32 %3, i32* %1    ; c = b + 4
    %4 = add i32 0, 0
    ret i32 0
}
```

将对应的全局标识符/虚拟寄存器加入到变量类`Symbol`中，便于取出并参与计算

#### 函数
声明
- 使用全局标识符 `@`
- 基本块占用一个编号，进入 Block 后需要跳过一个基本块入口的编号
- 对传入的形参，应`alloca`内存空间并`store`

调用
- 对无返回值`void`类函数，仅在 `Stmt → Exp;` 中出现
- 对有返回值`int`, `char`，出现范围较广。在参与运算时需申请寄存器用于存值

例如：
```c
int a = 1000;

int foo(int a, int b){
    return a + b;
}

void bar() {
    a = 1200;
    return;
}

int main() {
    bar();
    int b = a;
    a = getint();
    b = foo(a,b);
    return 0;
}
```

其llvm代码为：
```llvm
declare i32 @getint()
declare i32 @getchar()
declare void @putint(i32)
declare void @putch(i32)
declare void @putstr(i8*)

@a = dso_local global i32 1000

@.str = private unnamed_addr constant [2 x i8] c"\0A\00", align 1

define dso_local i32 @foo(i32 %0, i32 %1) {
    %3 = alloca i32
    %4 = alloca i32
    store i32 %0, i32* %3
    store i32 %1, i32* %4
    %5 = load i32, i32* %3
    %6 = load i32, i32* %4
    %7 = add nsw i32 %5, %6
    ret i32 %7
}

define dso_local void @bar() {
    store i32 1200, i32* @a
    ret void
}

define dso_local i32 @main() {
    call void @bar()
    %1 = alloca i32
    %2 = load i32, i32* @a
    store i32 %2, i32* %1
    %3 = call i32 @getint()
    store i32 %3, i32* @a
    %4 = load i32, i32* @a
    %5 = load i32, i32* %1
    %6 = call i32 @foo(i32 %4, i32 %5)
    store i32 %6, i32* %1
    ret i32 0
}
```

#### 条件判断
对`Cond`有如下递推关系：
```text
Cond → LOrExp
LOrExp → {LAndExp '||'} LAndExp
LAndExp → {EqExp '&&'} EqExp
EqExp → {RelExp ('==' | '!=')} RelExp
RelExp → {AddExp ('<' | '>' | '<=' | '>=')} AddExp
```

对`RetValue`类，新建属性以表明其下的表达式为单一还是多元
- 将`LOrExp` `LAndExp`分为一类，对其每个递推表达式需单独设置`label`寄存器用来决定是否跳转。返回类型可为单一可为多元
- 将`EqExp` `RelExp`分为一类，计算完表达式的值后返回寄存器\数字。返回类型为多元

实现细节：
- 跳转到之后还未构建的基本块，采取**回填操作**
- 注意**短路求值**，`LOrExp`中若出现非0数字则为真，提前终止；`LAndExp`中若出现0则为假，提前终止

for语句，`break` 跳转到 `NextStmt`，而 `continue` 跳转到 `ForStmt2`

例如：
```c
int a = 1000;

int foo(int a, int b){
    return a + b;
}

void bar() {
    a = 1200;
    return;
}

int main() {
    bar();
    int b = a;
    a = getint();
    b = foo(a,b);
    return 0;
}
```

其llvm代码为：
```llvm
declare i32 @getint()
declare i32 @getchar()
declare void @putint(i32)
declare void @putch(i8)
declare void @putstr(i8*)


define dso_local i32 @main() {
%1 = alloca i32
store i32 1, i32* %1
%2 = alloca i32
%3 = load i32, i32* %1
store i32 %3, i32* %2
%4 = alloca i32
%5 = alloca i32
%6 = alloca i32
%7 = call i32 @getint()
store i32 %7, i32* %5
%8 = load i32, i32* %1
%9 = load i32, i32* %1
%10 = mul i32 %8, %9
store i32 %10, i32* %6
br label %11

11:
%12 = load i32, i32* %6
%13 = load i32, i32* %5
%14 = add i32 %13, 1
%15 = icmp slt i32 %12, %14
br i1 %15, label %16, label %37

16:
%17 = load i32, i32* %2
store i32 %17, i32* %4
%18 = load i32, i32* %1
%19 = load i32, i32* %2
%20 = add i32 %18, %19
store i32 %20, i32* %2
%21 = load i32, i32* %4
store i32 %21, i32* %1
br label %22

22:
%23 = load i32, i32* %6
%24 = srem i32 %23, 2
%25 = icmp eq i32 %24, 1
br i1 %25, label %26, label %27

26:
br label %34
br label %27

27:
%28 = alloca i32
store i32 10086, i32* %28
br label %29

29:
%30 = load i32, i32* %6
%31 = icmp sgt i32 %30, 19
br i1 %31, label %32, label %33

32:
br label %37
br label %33

33:
br label %34

34:
%35 = load i32, i32* %6
%36 = add i32 %35, 1
store i32 %36, i32* %6
br label %11

37:
ret i32 0
}
```

#### 数组
##### 定义
- 全局数组
    在自定义函数声明前定义
    ```c
    int a[1 + 2 + 3 + 4] = { 1, 1 + 1, 1 + 3 - 1, 0, 0, 0, 0, 0, 0, 0 };
    int b[20];
    char c[8] = "foobar";
    ```
    ```llvm
    @a = dso_local global [10 x i32] [i32 1, i32 2, i32 3, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0]
    @b = dso_local global [20 x i32] zeroinitializer
    @c = dso_local global [8 x i8] c"foobar\00\00", align 1
    ```
- 局部数组
    逐一初始化元素，往对应位置中存值。**char**与**int**同理，需将初始值转为int形式
    ```c
    int c[3] = {1, 2, 3};
    ```
    ```llvm
    %1 = alloca [3 x i32]
    %2 = getelementptr inbounds [3 x i32], [3 x i32]* %1, i32 0, i32 0
    store i32 1, i32* %2
    %3 = getelementptr inbounds i32, i32* %2, i32 1
    store i32 2, i32* %3
    %4 = getelementptr inbounds i32, i32* %3, i32 1
    store i32 3, i32* %4
    ```

##### 访问地址
如获取`a[3]`的地址，有以下方法
```llvm
%1 = getelementptr [5 x i32], [5 x i32]* @a, i32 0, i32 3

%2 = getelementptr [5 x i32], [5 x i32]* @a, i32 0
%3 = getelementptr i32, i32* %2, i32 3

%3 = getelementptr i32, i32* @a, i32 3
```
后续进行`store` `load`操作

##### 函数传参
以指针形式传入函数中
```c
void foo(int y[]) {
    return;
}

int main() {
    int c[3];
    foo(c);
    return 0;
}
```

```llvm
define dso_local void @foo(i32* %0) {
    %2 = alloca i32*
    store i32* %0, i32** %2
    ret void
}

define dso_local i32 @main() {
    %1 = alloca [3 x i32]
    %2 = getelementptr inbounds [3 x i32], [3 x i32]* %1, i32 0, i32 0
    call void @foo(i32* %2)
    ret i32 0
}
```

#### 类型转换
- 函数传参
- 函数返回值(getchar)
- `cond`中`EqExp` `RelExp`返回值从i1转到i32

#### debug与重构
Done:
- [x] 无返回值函数最后需要返回
- [x] `cond`未及时更新nextLabel的值，跳转到非label的临时寄存器
- [x] 语句块中若出现`break` `continue` `return`，则停止生成此语句块的后续内容，直接转到下一语句块

- [x] 忽视`!`出现在`cond`中的情况
    返回 `retValue == 0` 判断情况即可
- [x] 数组访问格式问题
    对传入函数的指针，采取`%3 = getelementptr i32, i32* %2, i32 3`
    对定义的数组，采取`%1 = getelementptr [5 x i32], [5 x i32]* @a, i32 0, i32 3`
    数组偏移时，无论指针类型为`i8`或`i32`，最后均应为`i32`

- [x] `CharConst`转义字符的输出，如转义符`'\t' = 9`
- [x] 有初始值的字符数组，空余部分应全部补0
- [x] `char`数组初始化有整数需用`i8`形式存储
- [x] 函数调用(putch)传参的时候对参数进行类型转换

- [x] 重构：原先将字符串存在`Module`中，后用一周时间逐渐重构，改为**一切皆value**的架构

TODO:
- 回填改为使用 LLVM IR 中的 SlotTracker


## 后端部分
### Mips目标代码生成
#### .data段
- 声明的全局变量，均采用 `.word` 形式存储
- `printf`语句的字符串常量，均采用 `.asciiz` 形式存储

例如有以下代码：
```c
int a;
int b[10];
int c[10] = {1, 2};
char d;
char e[10] = {'a', 'b'};
char f[10] = "hello";

int main()
{
    printf("Hello");
    return 0;
}
```

其mips的data段为：
```mips
.data
    a: .word 0
    b: .word 0:10
    c: .word 1, 2, 0, 0, 0, 0, 0, 0, 0, 0
    d: .word 0
    e: .word 97, 98, 0, 0, 0, 0, 0, 0, 0, 0
    f: .word 104, 101, 108, 108, 111, 0, 0, 0, 0, 0
    .str: .asciiz "Hello"
```

#### .text段
存放函数指令


#### debug与重构
- 全局变量声明，若并未全部对齐，即未全部采用 `.word` ，可能导致取址错误、无法访问对象

