// Autogenerated AST node
package com.astra.ses.spell.language.model.ast;
import com.astra.ses.spell.language.model.SimpleNode;

public class Num extends exprType implements num_typeType {
    public Object n;
    public int type;
    public String num;

    public Num(Object n, int type, String num) {
        this.n = n;
        this.type = type;
        this.num = num;
    }

    public Num(Object n, int type, String num, SimpleNode parent) {
        this(n, type, num);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Num[");
        sb.append("n=");
        sb.append(dumpThis(this.n));
        sb.append(", ");
        sb.append("type=");
        sb.append(dumpThis(this.type, num_typeType.num_typeTypeNames));
        sb.append(", ");
        sb.append("num=");
        sb.append(dumpThis(this.num));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        return visitor.visitNum(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
    }

}
