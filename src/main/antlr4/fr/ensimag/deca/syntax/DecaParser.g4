parser grammar DecaParser;

options {
    // Default language but name it anyway
    //
    language  = Java;

    // Use a superclass to implement all helper
    // methods, instance variables and overrides
    // of ANTLR default methods, such as error
    // handling.
    //
    superClass = AbstractDecaParser;

    // Use the vocabulary generated by the accompanying
    // lexer. Maven knows how to work out the relationship
    // between the lexer and parser and will build the
    // lexer before the parser. It will also rebuild the
    // parser if the lexer changes.
    //
    tokenVocab = DecaLexer;

}

// which packages should be imported?
@header {
    import fr.ensimag.deca.tree.*;
    import java.io.PrintStream;
    import fr.ensimag.deca.tools.SymbolTable;
    import fr.ensimag.deca.DecacCompiler;
}

@members {
    //private static SymbolTable symbol_table = new SymbolTable();
    @Override
    protected AbstractProgram parseProgram() {
        return prog().tree;
    }
}

prog returns[AbstractProgram tree]
    : list_classes main EOF {
            assert($list_classes.tree != null);
            assert($main.tree != null);
            $tree = new Program($list_classes.tree, $main.tree);
            setLocation($tree, $list_classes.start);
        }
    ;

main returns[AbstractMain tree]
    : /* epsilon */ {
            $tree = new EmptyMain();
        }
    | block {
            assert($block.decls != null);
            assert($block.insts != null);
            $tree = new Main($block.decls, $block.insts);
            setLocation($tree, $block.start);
        }
    ;

block returns[ListDeclVar decls, ListInst insts]
    : OBRACE list_decl list_inst CBRACE {
            assert($list_decl.tree != null);
            assert($list_inst.tree != null);
            $decls = $list_decl.tree;
            $insts = $list_inst.tree;
            //setLocation($decls, $list_decl.start);
            //setLocation($insts, $list_inst.start);
        }
    ;

list_decl returns[ListDeclVar tree]
@init   {
            $tree = new ListDeclVar();
        }
    : decl_var_set[$tree]*
    ;

decl_var_set[ListDeclVar l]
    : type list_decl_var[$l,$type.tree] SEMI;

list_decl_var[ListDeclVar l, AbstractIdentifier t]
    : dv1=decl_var[$t] {
        assert($dv1.tree!=null);
        $l.add($dv1.tree);
        } (COMMA dv2=decl_var[$t] {
            assert($dv2.tree!=null);
            $l.add($dv2.tree);
        }
      )*
    ;

decl_var[AbstractIdentifier t] returns[AbstractDeclVar tree]
@init   {
    AbstractInitialization init = new NoInitialization();

        }
    : i=ident {
        assert($i.tree!=null);
        }
      (EQUALS e=expr  {
        assert($e.tree!=null);
        init = new Initialization($e.tree);
        setLocation(init,$e.start);
        }
      )? {
        assert(t!= null);
        $tree = new DeclVar(t,$i.tree, init);
        setLocation($tree,$i.start);
        }
    ;

list_inst returns[ListInst tree]
@init {
    $tree = new ListInst();
}
    : (inst {
        assert($inst.tree != null);
        $tree.add($inst.tree);
        setLocation($tree, $inst.start);
        }
      )*
    ;

inst returns[AbstractInst tree]
    : e1=expr SEMI {
            assert($e1.tree != null);
            $tree = $e1.tree;
            setLocation($tree, $e1.start);
        }
    | SEMI {
        $tree = new NoOperation();
        setLocation($tree, $SEMI);
        }
    | PRINT OPARENT list_expr CPARENT SEMI {
            assert($list_expr.tree != null);
            $tree = new Print(false,$list_expr.tree);
            setLocation($tree, $list_expr.start);
        }
    | PRINTLN OPARENT list_expr CPARENT SEMI {
            assert($list_expr.tree != null);
            $tree = new Println(false,$list_expr.tree);
            setLocation($tree, $list_expr.start);
        }
    | PRINTX OPARENT list_expr CPARENT SEMI {
            assert($list_expr.tree != null);
            $tree = new Print(true,$list_expr.tree);
            setLocation($tree, $list_expr.start);
        }
    | PRINTLNX OPARENT list_expr CPARENT SEMI {
            assert($list_expr.tree != null);
            $tree = new Println(true,$list_expr.tree);
            setLocation($tree, $list_expr.start);
        }
    | if_then_else { 
            assert($if_then_else.tree != null);
            $tree = $if_then_else.tree;
            setLocation($tree, $if_then_else.start);
        }
    | WHILE OPARENT condition=expr CPARENT OBRACE body=list_inst CBRACE {
            assert($condition.tree != null);
            assert($body.tree != null);
            $tree = new While($condition.tree, $body.tree);
            setLocation($tree, $condition.start);
            setLocation($tree, $body.start);
        }
    | RETURN expr SEMI {
            assert($expr.tree != null);
            $tree = new Return($expr.tree);
            setLocation($tree, $expr.start);
        }
    ;

if_then_else returns[IfThenElse tree]
@init {
    ListInst elseBranch = new ListInst();
    IfThenElse elifBranch = null;
}
    : if1=IF OPARENT condition=expr CPARENT OBRACE li_if=list_inst CBRACE {
            assert($condition.tree != null);
            assert($li_if.tree != null);
            $tree = new IfThenElse($condition.tree, $li_if.tree, elseBranch);
            setLocation($tree, $condition.start);
            setLocation($tree, $li_if.start);
        }
      (ELSE elsif=IF OPARENT elsif_cond=expr CPARENT OBRACE elsif_li=list_inst CBRACE {
          assert($elsif_cond.tree != null);
          assert($elsif_li.tree != null);
          elifBranch = new IfThenElse($elsif_cond.tree,$elsif_li.tree, new ListInst());
          elseBranch.add(elifBranch);
          elseBranch = elifBranch.getElseBranch();
          setLocation($tree,$elsif_cond.start);
          setLocation($tree,$elsif_li.start);
        }
      )*
      (ELSE OBRACE li_else=list_inst CBRACE {
          assert($li_else.tree != null);
          if(elifBranch == null){$tree.setElseBranch($li_else.tree);}
          else{elifBranch.setElseBranch($li_else.tree);}
          setLocation($tree,$li_else.start);
        }
      )? {
      }
    ;

list_expr returns[ListExpr tree]
@init   {
    $tree = new ListExpr();
        }
    : (e1=expr {
        assert($e1.tree != null);
        $tree.add($e1.tree);
        setLocation($tree, $e1.start);
        }
       (COMMA e2=expr {
           assert($e2.tree != null);
           $tree.add($e2.tree);
           setLocation($tree, $e2.start);
        }
       )* )?
    ;

expr returns[AbstractExpr tree]
    : assign_expr {
            assert($assign_expr.tree != null);
            $tree = $assign_expr.tree;
            setLocation($tree, $assign_expr.start);
        }
    ;

assign_expr returns[AbstractExpr tree]
    : e=or_expr (
        /* condition: expression e must be a "LVALUE" */ {
            if (! ($e.tree instanceof AbstractLValue)) {
                throw new InvalidLValue(this, $ctx);
            }
        }
        EQUALS e2=assign_expr {
            assert($e.tree != null);
            assert($e2.tree != null);
            $tree = new Assign((AbstractLValue)$e.tree, $e2.tree);
            setLocation($tree, $e.start);
            setLocation($tree, $e2.start);
        }
      | /* epsilon */ {
            assert($e.tree != null);
            $tree = $e.tree;
            setLocation($tree, $e.start);
        }
      )
    ;

or_expr returns[AbstractExpr tree]
    : e=and_expr {
            assert($e.tree != null);
            $tree = $e.tree;
            setLocation($tree, $e.start);
        }
    | e1=or_expr OR e2=and_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Or($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
            setLocation($tree, $e2.start);
       }
    ;

and_expr returns[AbstractExpr tree]
    : e=eq_neq_expr {
            assert($e.tree != null);
            $tree = $e.tree;
            setLocation($tree, $e.start);
        }
    |  e1=and_expr AND e2=eq_neq_expr {
            assert($e1.tree != null);                         
            assert($e2.tree != null);
            $tree = new And($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
            setLocation($tree, $e2.start);
        }
    ;

eq_neq_expr returns[AbstractExpr tree]
    : e=inequality_expr {
            assert($e.tree != null);
            $tree = $e.tree;
            setLocation($tree, $e.start);
        }
    | e1=eq_neq_expr EQEQ e2=inequality_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Equals($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
            setLocation($tree, $e2.start);
        }
    | e1=eq_neq_expr NEQ e2=inequality_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new NotEquals($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
            setLocation($tree, $e2.start);
        }
    ;

inequality_expr returns[AbstractExpr tree]
    : e=sum_expr {
            assert($e.tree != null);
            $tree = $e.tree;
            setLocation($tree, $e.start);
        }
    | e1=inequality_expr LEQ e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new LowerOrEqual($e1.tree, $e2.tree);   
            setLocation($tree, $e1.start);
            setLocation($tree, $e2.start);       
        }
    | e1=inequality_expr GEQ e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new GreaterOrEqual($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
            setLocation($tree, $e2.start);
        }
    | e1=inequality_expr GT e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Greater($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
            setLocation($tree, $e2.start);
        }
    | e1=inequality_expr LT e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Lower($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
            setLocation($tree, $e2.start);
        }
    | e1=inequality_expr INSTANCEOF type {
            assert($e1.tree != null);
            assert($type.tree != null);
            $tree = new InstanceOf($e1.tree, $type.tree);
            setLocation($tree, $e1.start);
        

        }
    ;


sum_expr returns[AbstractExpr tree]
    : e=mult_expr {
            assert($e.tree != null);
            $tree = $e.tree;
            setLocation($tree, $e.start);
        }
    | e1=sum_expr PLUS e2=mult_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Plus($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
            setLocation($tree, $e2.start);
        }
    | e1=sum_expr MINUS e2=mult_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Minus($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
            setLocation($tree, $e2.start);
        }
    ;

mult_expr returns[AbstractExpr tree]
    : e=unary_expr {
            assert($e.tree != null);
            $tree = $e.tree;
            setLocation($tree, $e.start);
        }
    | e1=mult_expr TIMES e2=unary_expr {
            assert($e1.tree != null);                                         
            assert($e2.tree != null);
            $tree = new Multiply($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
            setLocation($tree, $e2.start);
        }
    | e1=mult_expr SLASH e2=unary_expr {
            assert($e1.tree != null);                                         
            assert($e2.tree != null);
            $tree = new Divide($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
            setLocation($tree, $e2.start);
        }
    | e1=mult_expr PERCENT e2=unary_expr {
            assert($e1.tree != null);                                                                          
            assert($e2.tree != null);
            $tree = new Modulo($e1.tree, $e2.tree);
            setLocation($tree, $e1.start);
            setLocation($tree, $e2.start);
        }
    ;

unary_expr returns[AbstractExpr tree]
    : op=MINUS e=unary_expr {
            assert($e.tree != null);
            $tree = new UnaryMinus($e.tree);
            setLocation($tree, $e.start);
        }
    | op=EXCLAM e=unary_expr {
            assert($e.tree != null);
            $tree = new Not($e.tree);
            setLocation($tree, $e.start);
        }
    | select_expr {
            assert($select_expr.tree != null);
            $tree = $select_expr.tree;
            setLocation($tree, $select_expr.start);
        }
    ;

select_expr returns[AbstractExpr tree]
    : e=primary_expr {
            assert($e.tree != null);
            $tree = $e.tree;
            setLocation($tree, $e.start);
        }
    | e1=select_expr DOT i=ident {
            assert($e1.tree != null);
            assert($i.tree != null);

        }
        (o=OPARENT args=list_expr CPARENT {
            // we matched "e1.i(args)"
            assert($args.tree != null);
            assert($e1.tree!=null);
            $tree = new MethodCall($e1.tree, $i.tree, $args.tree);
            setLocation($tree, $args.start);
        }
        | /* epsilon */ {
            $tree = new Selection($e1.tree, $i.tree);
            setLocation($tree, $e1.start);
        }
        )
    ;

primary_expr returns[AbstractExpr tree]
@init{
    This t = new This(true);                           
    }

    : ident {
            assert($ident.tree != null);
            $tree = $ident.tree;
            setLocation($tree, $ident.start);
        }
    | m=ident OPARENT args=list_expr CPARENT {
            assert($args.tree != null);
            assert($m.tree != null);
            $tree = new MethodCall(t, $m.tree, $args.tree);
            setLocation($tree, $m.start);
            
        }
    | OPARENT expr CPARENT {
            assert($expr.tree != null);
            $tree = $expr.tree;
            setLocation($tree, $expr.start);
        }
    | READINT OPARENT CPARENT {
        $tree = new ReadInt();
        setLocation($tree, $READINT);
        }
    | READFLOAT OPARENT CPARENT {
        $tree = new ReadFloat();
        setLocation($tree, $READFLOAT);
        }
    | NEW ident OPARENT CPARENT {
            assert($ident.tree != null);
            $tree = new New($ident.tree);
            setLocation($tree, $ident.start);
            
        }
    | cast=OPARENT type CPARENT OPARENT expr CPARENT {
            assert($type.tree != null);
            assert($expr.tree != null);
            $tree = new Cast($type.tree, $expr.tree);
            setLocation($tree, $type.start);
            setLocation($tree, $expr.start);
        }
    | literal {
            assert($literal.tree != null);
            $tree = $literal.tree;
            setLocation($tree, $literal.start);
        }
    ;

type returns[AbstractIdentifier tree]
    : ident {
            assert($ident.tree != null);
            $tree = $ident.tree;
            setLocation($tree, $ident.start);
        }
    ;

literal returns[AbstractExpr tree]
    : INT {
            try{
                $tree = new IntLiteral(Integer.parseInt($INT.text));
            } catch (NumberFormatException e){
                $tree = null;
            }
            setLocation($tree, $INT);
        } {$tree != null}?
    | fd=FLOAT {
            try{
                $tree = new FloatLiteral(Float.parseFloat($fd.text));
            } catch (NumberFormatException e){
                $tree = null;
            }
            setLocation($tree, $fd);
        } {$tree != null}?
    | s=STRING {
            try{
                String str = new String();
                str = $s.text.substring(1,$s.text.length()-1);
                $tree = new StringLiteral(str);
            } catch (NumberFormatException e){
                $tree = null;
            }
            setLocation($tree, $s);
        } {$tree != null}?
    | TRUE {
        try{
                $tree = new BooleanLiteral(true);
            } catch (NumberFormatException e){
                $tree = null;
            }
            setLocation($tree, $TRUE);
        } {$tree != null}?
    | FALSE {
            try{
                $tree = new BooleanLiteral(false);
            } catch (NumberFormatException e){
                $tree = null;
            }
            setLocation($tree, $FALSE);
        } {$tree != null}?
    | THIS {
        try{
                $tree = new This(false);
            } catch (NumberFormatException e){
                $tree = null;
            }
            setLocation($tree, $THIS);
        } {$tree != null}? 
    | NULL {
        try{
                $tree = new Null();
            } catch (NumberFormatException e){
                $tree = null;
            }
            setLocation($tree, $NULL);
        } {$tree != null}?
    ;

ident returns[AbstractIdentifier tree]
    : IDENT {
        
        $tree = new Identifier(getDecacCompiler().getSymbolTable().create($IDENT.text));
        setLocation($tree, $IDENT);
        } 
    ;

/****     Class related rules     ****/

list_classes returns[ListDeclClass tree]
    @init {
        $tree = new ListDeclClass();
     }
    :
      (c1=class_decl {
          assert($c1.tree != null);
          $tree.add($c1.tree);
        }
      )*
    ;

class_decl returns [AbstractDeclClass tree]
    : CLASS name=ident superclass=class_extension OBRACE class_body CBRACE {
        assert($name.tree != null);
        assert($superclass.tree != null);
        assert($class_body.listMeth!= null);
        assert($class_body.listField!= null);
        $tree = new DeclClass($name.tree, $superclass.tree, $class_body.listField, $class_body.listMeth);
        setLocation($tree, $CLASS);
        }
    ;

class_extension returns[AbstractIdentifier tree]
    : EXTENDS ident {
        assert($ident.tree != null);
        $tree = $ident.tree;
        setLocation($tree, $EXTENDS);
        }
    | /* epsilon */ {
        $tree = new Identifier(getDecacCompiler().getSymbolTable().create("Object"));
        }
    ;

class_body returns [ListDeclMethod listMeth, ListDeclField listField]
@init{
    $listMeth = new ListDeclMethod();
    $listField = new ListDeclField();
    }
    : (m=decl_method {
        assert($m.tree != null);
        $listMeth.add($m.tree);
        }
      | decl_field_set[$listField]
      )*
    ;

decl_field_set[ListDeclField l]
    : v = visibility t = type list_decl_field[$l, $v.tree, $t.tree] SEMI;

visibility returns [Visibility tree]
    : /* epsilon */ {
        $tree = Visibility.PUBLIC;
        }
    | PROTECTED {
        $tree = Visibility.PROTECTED;
        }
    ;

list_decl_field[ListDeclField l, Visibility v, AbstractIdentifier t]
    : dv1=decl_field[$v, $t]{
        assert($dv1.tree!=null);
        $l.add($dv1.tree);
        }(COMMA dv2=decl_field[$v, $t] {
            assert($dv2.tree!=null);
            $l.add($dv2.tree);
        }
      )*
    ;

decl_field[Visibility v, AbstractIdentifier t] returns [AbstractDeclField tree]
@init {
    AbstractInitialization init = new NoInitialization();
}
    : i=ident {
        assert($i.tree != null);
        }
      (EQUALS e=expr {
          assert($e.tree != null);
          init = new Initialization($e.tree);
          setLocation(init, $e.start);
        }
      )? {
          assert(t != null);
          $tree = new DeclField(v, t, $i.tree, init);
          setLocation($tree, $i.start);
        }
    ;

decl_method returns [AbstractDeclMethod tree]
@init {
    AbstractMethodBody methbody;
    StringLiteral string;
    }
    : type ident OPARENT params=list_params CPARENT (b=block {
        assert($b.decls != null);
        assert($b.insts != null);
        methbody = new MethodBody($b.decls, $b.insts);
        setLocation(methbody, $b.start);
        }
      | ASM OPARENT code=multi_line_string CPARENT SEMI {
          assert($code.text != null);
          string = new StringLiteral($code.text.substring(1,$code.text.length()-1));
          methbody = new MethodBodyAsm(string);
          setLocation(methbody, $code.start);
        }
      ) {
          assert($ident.tree != null);
          assert($type.tree != null);
          assert($params.tree != null);
          $tree = new DeclMethod($type.tree, $ident.tree, $params.tree, methbody);
          setLocation($tree, $type.start);
        }
    ;

list_params returns [ListDeclParam tree]
@init {
    $tree = new ListDeclParam();
}
    : (p1=param {
        //assert($p1.tree != null);
        $tree.add($p1.tree);
        } (COMMA p2=param {
            //assert($p2.tree != null);
            $tree.add($p2.tree);
        }
      )*)?
    ;
    
multi_line_string returns[String text, Location location]
    : s=STRING {
            $text = $s.text;
            $location = tokenLocation($s);
        }
    | s=MULTI_LINE_STRING {
            $text = $s.text;
            $location = tokenLocation($s);
        }
    ;

param returns [AbstractDeclParam tree]
    : type ident {
        assert($type.tree != null);
        assert($ident.tree!= null);
        $tree = new DeclParam($type.tree,$ident.tree);
        setLocation($tree, $type.start);
        }
    ;
