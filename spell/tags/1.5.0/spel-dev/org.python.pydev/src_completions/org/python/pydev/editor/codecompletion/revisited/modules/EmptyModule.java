/*
 * Created on Dec 20, 2004
 *
 * @author Fabio Zadrozny
 */
package org.python.pydev.editor.codecompletion.revisited.modules;

import java.io.File;
import java.io.Serializable;

import org.python.pydev.core.ICodeCompletionASTManager;
import org.python.pydev.core.ICompletionCache;
import org.python.pydev.core.ICompletionState;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.IToken;
import org.python.pydev.editor.codecompletion.revisited.visitors.Definition;

/**
 * @author Fabio Zadrozny
 */
public class EmptyModule extends AbstractModule implements Serializable {

    private static final long serialVersionUID = 1L;
    public File f;
    
    @Override
    public File getFile() {
        return f;
    }

    /**
     * @param f
     */
    public EmptyModule(String name, File f) {
        super(name);
        this.f = f;
    }

    /**
     * @see org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule#getWildImportedModules()
     */
    public IToken[] getWildImportedModules() {
        throw new RuntimeException("Not intended to be called");
    }

    /**
     * @see org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule#getTokenImportedModules()
     */
    public IToken[] getTokenImportedModules() {
        throw new RuntimeException("Not intended to be called");
    }

    /**
     * @see org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule#getGlobalTokens()
     */
    public IToken[] getGlobalTokens() {
        throw new RuntimeException("Not intended to be called");
    }

    /**
     * @see org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule#getDocString()
     */
    public String getDocString() {
        throw new RuntimeException("Not intended to be called");
    }

    /**
     * @see org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule#getGlobalTokens(java.lang.String)
     */
    public IToken[] getGlobalTokens(ICompletionState state, ICodeCompletionASTManager manager) {
        throw new RuntimeException("Not intended to be called");
    }

    /**
     * @see org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule#findDefinition(java.lang.String, int, int)
     */
    public Definition[] findDefinition(ICompletionState state, int line, int col, IPythonNature nature) throws Exception {
        throw new RuntimeException("Not intended to be called");
    }

    @Override
    public boolean isInDirectGlobalTokens(String tok, ICompletionCache completionCache) {
        throw new RuntimeException("Not intended to be called");
    }

    public boolean isInDirectImportTokens(String tok) {
        throw new RuntimeException("Not implemented");
    }

}
