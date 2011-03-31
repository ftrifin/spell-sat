// Autogenerated AST node
package org.python.pydev.parser.jython.ast;
import org.python.pydev.parser.jython.SimpleNode;

public class aliasType extends SimpleNode {
    public NameTokType name;
    public NameTokType asname;

    public aliasType(NameTokType name, NameTokType asname) {
        this.name = name;
        this.asname = asname;
    }

    public aliasType(NameTokType name, NameTokType asname, SimpleNode
    parent) {
        this(name, asname);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("alias[");
        sb.append("name=");
        sb.append(dumpThis(this.name));
        sb.append(", ");
        sb.append("asname=");
        sb.append(dumpThis(this.asname));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        traverse(visitor);
        return null;
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (name != null)
            name.accept(visitor);
        if (asname != null)
            asname.accept(visitor);
    }

}
