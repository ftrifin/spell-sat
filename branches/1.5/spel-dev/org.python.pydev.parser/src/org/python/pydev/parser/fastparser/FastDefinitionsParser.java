package org.python.pydev.parser.fastparser;

import java.util.ArrayList;
import java.util.List;

import org.python.pydev.core.ICallback;
import org.python.pydev.core.Tuple;
import org.python.pydev.core.docutils.ParsingUtils;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.core.structure.FastStack;
import org.python.pydev.core.structure.FastStringBuffer;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Module;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.exprType;
import org.python.pydev.parser.jython.ast.stmtType;

/**
 * @note: Unfinished
 * 
 * This class should be able to gather the definitions found in a module in a very fast way.
 * 
 * The target is having a performance around 5x faster than doing a regular parse, focusing on getting
 * the name tokens for:
 * 
 * classes, functions, class attributes, instance attributes -- basically the tokens that provide a 
 * definition that can be 'globally' accessed.
 *
 * @author Fabio
 */
public final class FastDefinitionsParser {
    
    /**
     * Set and kept in the constructor
     */
    
    /**
     * The chars we should iterate through.
     */
    final private char[] cs;
    
    /**
     * The length of the buffer we're iterating.
     */
    final private int length;
    
    /**
     * Current iteration index
     */
    private int currIndex = 0;
    
    /**
     * The current column
     */
    private int col;
    
    /**
     * The current row
     */
    private int row = 0;
    
    /**
     * The column where the 1st char was found
     */
    private int firstCharCol = 1;
    
    /**
     * Holds things added to the 'global' module
     */
    private final ArrayList<stmtType> body = new ArrayList<stmtType>();
    
    /**
     * Holds a stack of classes so that we create a new one in each new scope to be filled and when the scope is ended,
     * it should have its body filled with the stackBody contents related to each
     */
    private final FastStack<ClassDef> stack = new FastStack<ClassDef>();
    
    /**
     * For each item in the stack, there's a stackBody that has the contents to be added later to that class.
     */
    private final FastStack<List<stmtType>> stackBody = new FastStack<List<stmtType>>();

    /**
     * Buffer with the contents of a line.
     */
    private final FastStringBuffer lineBuffer = new FastStringBuffer();
    
    /**
     * Should we debug?
     */
    private final static boolean DEBUG = false;
    
    
    /**
     * Constructor
     * 
     * @param cs array of chars that should be filled.
     */
    private FastDefinitionsParser(char[] cs){
        this.cs = cs;
        this.length = cs.length;
    }
    
    
    /**
     * This is the method that actually extracts things from the passed buffer.
     */
    private void extractBody() {
        if(currIndex < length){
            handleNewLine();
        }
        //in the 1st attempt to handle the 1st line, if it had nothing we could actually go backward 1 char
        if(currIndex < 0){
            currIndex=0;
        }
        ParsingUtils parsingUtils = ParsingUtils.create(cs);
        
        for (;currIndex < length; currIndex++, col++) {
            char c = cs[currIndex];
            
            switch (c){
            
                case '\'':
                case '"': 
                    if(DEBUG){
                        System.out.println("literal");
                    }
                    //go to the end of the literal
                    currIndex = parsingUtils.getLiteralEnd(currIndex, c);
                    break;
                    
                    
                    
                case '#': 
                    if(DEBUG){
                        System.out.println("comment");
                    }
                    //go to the end of the comment
                    currIndex++;
                    OUT:
                    while(currIndex < length){
                        c = cs[currIndex];
                        currIndex++;
                        switch(c){
                            case '\r': 
                                if(currIndex < length-1 && cs[currIndex+1] == '\n'){
                                    currIndex++;
                                }
                                /*FALLTHROUGH**/
                            case '\n': 
                                break OUT;
                        }
                    }
                    
                    //after a comment, we'll always be in a new line
                    handleNewLine();
                    
                    break;
                    
                    
                case '{': 
                case '[':
                case '(':
                    //starting some call, dict, list, tuple... those don't count on getting some actual definition
                    currIndex = parsingUtils.eatPar(currIndex, null, c);
                    break;
                
                case '\r': 
                    if(currIndex < length-1 && cs[currIndex+1] == '\n'){
                        currIndex++;
                    }
                    /*FALLTHROUGH**/
                case '\n': 
                    currIndex++;
                    handleNewLine();
                    
                    break;
                    
                
                case '=':
                    if(currIndex < length-1 && cs[currIndex+1] != '='){ 
                        //should not be ==
                        //other cases such as !=, +=, -= are already treated because they don't constitute valid
                        //chars for an identifier.
                        
                        if(DEBUG){
                            System.out.println("Found possible attribute:"+lineBuffer+" col:"+firstCharCol);
                        }
                        
                        //if we've an '=', let's get the whole line contents to analyze...
                        currIndex = parsingUtils.getFullFlattenedLine(currIndex, lineBuffer);
                        currIndex--; //step one back to get the new line and handle it correctly
                        
                        String equalsLine = lineBuffer.toString().trim();
                        lineBuffer.clear();
                        
                        String[] splitted = StringUtils.split(equalsLine, '=');
                        ArrayList<exprType> targets = new ArrayList<exprType>();
                        
                        for(int j=0; j< splitted.length-1; j++){ //we don't want to get the last one.
                            String lineContents = splitted[j].trim(); 
                            if(lineContents.length() == 0){
                                continue;
                            }
                            boolean add=true;
                            for(int i=0;i<lineContents.length();i++){
                                char lineC = lineContents.charAt(i);
                                //can only be made of valid java chars (no spaces or similar things)
                                if(lineC != '.' && !Character.isJavaIdentifierPart(lineC)){
                                    add=false;
                                    break;
                                }
                            }
                            if(add){
                                //only add if it was something valid
                                if(lineContents.indexOf('.') != -1){
                                    String[] dotSplit = StringUtils.dotSplit(lineContents);
                                    if(dotSplit.length == 2 && dotSplit[0].equals("self")){
                                        Attribute attribute = new Attribute(new Name("self", Name.Load, false), new NameTok(dotSplit[1], NameTok.Attrib), Attribute.Load);
                                        targets.add(attribute);
                                    }
                                    
                                }else{
                                    Name name = new Name(lineContents, Name.Store, false);
                                    targets.add(name);
                                }
                            }                        
                        }
                        
                        if(targets.size() > 0){
                            Assign assign = new Assign(targets.toArray(new exprType[targets.size()]), null);
                            assign.beginColumn = this.firstCharCol;
                            assign.beginLine = this.row;
                            addToPertinentScope(assign);
                        }
                    }
                //No default
            }
            lineBuffer.append(c);
        }
        
        while(stack.size() > 0){
            endScope();
        }
    }
    
    
    /**
     * Called when a new line is found. Tries to make the match of function and class definitions.
     */
    private void handleNewLine() {
        if(currIndex >= length-1){
            return;
        }
        
        col = 1;
        row ++;
        if(DEBUG){
            System.out.println("Handling new line:"+row);
        }
        
        lineBuffer.clear();
        char c = cs[currIndex];
        
        while(currIndex < length-1 && Character.isWhitespace(c) && c != '\r' && c != '\n'){
            currIndex ++;
            col++;
            c = cs[currIndex];
        }
        
        
        if (c == 'c' && matchClass()){
            int startClassCol = col;
            currIndex += 6;
            col += 6;
            
            startClass(getNextIdentifier(c), row, startClassCol);
            
        }else if (c == 'd' && matchFunction()){
            int startMethodCol = col;
            currIndex += 4;
            col += 4;
            
            startMethod(getNextIdentifier(c), row, startMethodCol);
        }
        currIndex --;
        firstCharCol = col;
    }


    /**
     * Get the next identifier available.
     * @param c the current char
     * @return the identifier found
     */
    private String getNextIdentifier(char c) {
        c = this.cs[currIndex];
        
        while(currIndex < length && Character.isWhitespace(c)){
            currIndex ++;
            c = this.cs[currIndex];
        }
        
        int currClassNameCol = currIndex;
        while(Character.isJavaIdentifierPart(c)){
            currIndex++;
            if(currIndex >= length){
                break;
            }
            c = this.cs[currIndex];
        }
        return new String(this.cs, currClassNameCol, currIndex-currClassNameCol);
    }

    

    
    
    /**
     * Start a new method scope with the given row and column.
     * @param startMethodRow the row where the scope should start
     * @param startMethodCol the column where the scope should start
     */
    private void startMethod(String name, int startMethodRow, int startMethodCol) {
        NameTok nameTok = new NameTok(name, NameTok.ClassName);
        FunctionDef functionDef = new FunctionDef(nameTok, null, null, null, null);
        functionDef.beginLine = startMethodRow;
        functionDef.beginColumn = startMethodCol;

        addToPertinentScope(functionDef);
    }

    
    
    /**
     * Start a new class scope with the given row and column.
     * @param startClassRow the row where the scope should start
     * @param startClassCol the column where the scope should start
     */
    private void startClass(String name, int startClassRow, int startClassCol) {
        NameTok nameTok = new NameTok(name, NameTok.ClassName);
        ClassDef classDef = new ClassDef(nameTok, null, null, null, null, null, null);
        
        classDef.beginLine = startClassRow;
        classDef.beginColumn = startClassCol;
        
        stack.push(classDef);
        stackBody.push(new ArrayList<stmtType>());
    }
    
    
    /**
     * Finish the current scope in the stack.
     * 
     * May close many scopes in a single call depending on where the class should be added to.
     */
    private void endScope(){
        ClassDef def = stack.pop();
        List<stmtType> body = stackBody.pop();
        def.body = body.toArray(new stmtType[body.size()]);
        addToPertinentScope(def);
    }


    /**
     * This is the definition to be added to a given scope.
     * 
     * It'll find a correct scope based on the column it has to be added to.
     * 
     * @param newStmt the definition to be added
     */
    private void addToPertinentScope(stmtType newStmt) {
        //see where it should be added (global or class scope)
        while(stack.size() > 0){
            ClassDef parent = stack.peek();
            if(parent.beginColumn < newStmt.beginColumn){
                List<stmtType> peek = stackBody.peek();
                
                if(newStmt instanceof FunctionDef){
                    int size = peek.size();
                    if(size > 0){
                        stmtType existing = peek.get(size-1);
                        if(existing.beginColumn < newStmt.beginColumn){
                            //we don't want to add a method inside a method at this point.
                            //all the items added should have the same column.
                            return;
                        }
                    }
                }else if (newStmt instanceof Assign){
                    Assign assign = (Assign) newStmt;
                    exprType target = assign.targets[0];
                    
                    //an assign could be in a method or in a class depending on where we're right now...
                    int size = peek.size();
                    if(size > 0){
                        stmtType existing = peek.get(size-1);
                        if(existing.beginColumn < assign.beginColumn){
                            //add the assign to the correct place
                            if(existing instanceof FunctionDef){
                                FunctionDef functionDef = (FunctionDef) existing;
                                
                                if(target instanceof Attribute){
                                    addAssignToFunctionDef(assign, functionDef);
                                }
                                return;
                            }
                        }
                    }
                    
                    //if it still hasn't returned and it's a name, add it to the global scope.
                    if(target instanceof Name){
                        
                    }
                }
                peek.add(newStmt);
                return;
            }else{
                endScope();
            }
        }
        //if it still hasn't returned, add it to the global
        this.body.add(newStmt);
    }


    /**
     * Adds an assign statement to the given function definition.
     * 
     * @param assign the assign to be added
     * @param functionDef the function definition where it should be added
     */
    private void addAssignToFunctionDef(Assign assign, FunctionDef functionDef) {
        //if it's an attribute at this point, it'll always start with self!
        if(functionDef.body == null){
            if(functionDef.specialsAfter == null){
                functionDef.specialsAfter = new ArrayList<Object>();
            }
            functionDef.body = new stmtType[10];
            functionDef.body[0] = assign;
            functionDef.specialsAfter.add(1); //real len
        }else{
            //already exists... let's add it... as it's an array, we may have to reallocate it
            Integer currLen = (Integer) functionDef.specialsAfter.get(0);
            currLen += 1;
            functionDef.specialsAfter.set(0, currLen);
            if(functionDef.body.length < currLen){
                stmtType[] newBody = new stmtType[functionDef.body.length*2];
                System.arraycopy(functionDef.body, 0, newBody, 0, functionDef.body.length);
                functionDef.body = newBody;
            }
            functionDef.body[currLen-1] = assign;
        }
    }
    
    
    
    /**
     * @return true if we have a match for 'class' in the current index (the 'c' must be already matched at this point)
     */
    private boolean matchClass(){
        if(currIndex + 5 > this.length){
            return false;
        }
        return (this.cs[currIndex+1] == 'l' && this.cs[currIndex+2] == 'a' && 
                this.cs[currIndex+3] == 's' && this.cs[currIndex+4] == 's' && Character.isWhitespace(this.cs[currIndex+5]));
    }
    
    
    /**
     * @return true if we have a match for 'def' in the current index (the 'd' must be already matched at this point)
     */
    private boolean matchFunction(){
        if(currIndex + 3 > this.length){
            return false;
        }
        return (this.cs[currIndex+1] == 'e' && this.cs[currIndex+2] == 'f' && Character.isWhitespace(this.cs[currIndex+3]));
    }

    /**
     * Callbacks called just before returning a parsed object. Used for tests
     */
    public static List<ICallback<Object, Tuple<String, SimpleNode>>> parseCallbacks = 
        new ArrayList<ICallback<Object, Tuple<String, SimpleNode>>>();
    
    
    /**
     * Convenience method for parse(s.toCharArray())
     * @param s the string to be parsed
     * @return a Module node with the structure found
     */
    public static SimpleNode parse(String s, String moduleName) {
        return parse(s.toCharArray(), moduleName);
    }
    
    
    /**
     * This method will parse the char array passed and will build a structure with the contents of the file.
     * @param cs the char array to be parsed
     * @return a Module node with the structure found
     */
    public static SimpleNode parse(char[] cs, String moduleName) {
        FastDefinitionsParser parser = new FastDefinitionsParser(cs);
        parser.extractBody();
        List<stmtType> body = parser.body;
        Module ret = new Module(body.toArray(new stmtType[body.size()]));
        if(parseCallbacks.size() > 0){
            Tuple<String, SimpleNode> arg = new Tuple<String, SimpleNode>(moduleName, ret);
            for(ICallback<Object, Tuple<String, SimpleNode>> c:parseCallbacks){
                c.call(arg);
            }
        }
        return ret;
    }


    
    public static SimpleNode parse(String s) {
        return parse(s.toCharArray(), null);
    }

}
