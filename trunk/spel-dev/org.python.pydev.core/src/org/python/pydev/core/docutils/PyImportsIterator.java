package org.python.pydev.core.docutils;

import java.util.Iterator;

import org.eclipse.jface.text.IDocument;

/**
 * Iterator through imports that yields tuples with the import itself, the initial line of the import
 * and the final line of the import.
 *
 * @author Fabio
 */
public class PyImportsIterator implements Iterator<ImportHandle> {

    /**
     * Helper to iterate in the document
     */
    private PyDocIterator docIterator;

    /**
     * Variable holding whether hasNext should return true or not
     */
    private boolean hasNext = true;
    
    /**
     * Variable holding the next return value
     */
    private ImportHandle nextImport;

    /**
     * Delimiter to be used to add new lines in the imports found.
     */
    private String delimiter;

    /**
     * Document used in the iteration
     */
    private IDocument doc;
    
    /**
     * Constructor
     * 
     * @param doc the document from where the import should be gathered.
     */
    public PyImportsIterator(IDocument doc) {
        this.doc = doc;
        delimiter = PySelection.getDelimiter(doc);
        this.docIterator = new PyDocIterator(doc, false, false, false, true);
        //gather the 1st import
        calcNext();
    }
    
    /**
     * Pre-calculates the next return value and whether there is a next value to be returned.
     */
    private void calcNext(){
        if(!hasNext){
            //only pre-calculate if there's something to pre-calculate.
            return;
        }
        String importFound = null;
        int startFoundLine=-1;
        int endFoundLine=-1;
        
        
        while(docIterator.hasNext()){
            String str = docIterator.next();
            
            if((str.startsWith("import ") || str.startsWith("from "))){
                startFoundLine = docIterator.getLastReturnedLine();
                
                if(str.indexOf('(') != -1){ //we have something like from os import (pipe,\nfoo)
                    while(docIterator.hasNext() && str.indexOf(')') == -1){
                        str += delimiter+docIterator.next();
                    }
                }
                if(WordUtils.endsWith(str, '\\')){
                    while(docIterator.hasNext() && WordUtils.endsWith(str, '\\')){
                        str += delimiter+docIterator.next();
                    }
                }
                importFound = str;
                endFoundLine = docIterator.getLastReturnedLine();
                break; //ok, import found
            }
        }
        
        hasNext = importFound != null;
        if(hasNext){
            nextImport = new ImportHandle(doc, importFound, startFoundLine, endFoundLine);
        }else{
            nextImport = null;
        }
    }

    /**
     * From the iterator interface
     */
    public boolean hasNext() {
        return this.hasNext;
    }

    /**
     * From the iterator interface
     */
    public ImportHandle next() {
        ImportHandle ret = this.nextImport;
        calcNext(); //pre-compute the next step
        return ret;
    }

    /**
     * From the iterator interface (not implemented)
     */
    public void remove() {
        throw new RuntimeException("Not implemented");
    }

}
