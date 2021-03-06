// Autogenerated AST node
package org.python.pydev.parser.jython.ast;
import org.python.pydev.parser.jython.SimpleNode;

public class SetComp extends exprType {
    public exprType elt;
    public comprehensionType[] generators;

    public SetComp(exprType elt, comprehensionType[] generators) {
        this.elt = elt;
        this.generators = generators;
    }

    public SetComp(exprType elt, comprehensionType[] generators, SimpleNode
    parent) {
        this(elt, generators);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("SetComp[");
        sb.append("elt=");
        sb.append(dumpThis(this.elt));
        sb.append(", ");
        sb.append("generators=");
        sb.append(dumpThis(this.generators));
        sb.append("]");
        return sb.toString();
    }

    public Object accept(VisitorIF visitor) throws Exception {
        return visitor.visitSetComp(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (elt != null)
            elt.accept(visitor);
        if (generators != null) {
            for (int i = 0; i < generators.length; i++) {
                if (generators[i] != null)
                    generators[i].accept(visitor);
            }
        }
    }

}
