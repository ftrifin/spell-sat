/*
 * Created on 20/08/2005
 */
package org.python.pydev.plugin.preferences;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.resource.StringConverter;
import org.osgi.service.prefs.Preferences;
import org.python.pydev.builder.PyDevBuilderPrefPage;
import org.python.pydev.builder.todo.PyTodoPrefPage;
import org.python.pydev.editor.codefolding.PyDevCodeFoldingPrefPage;
import org.python.pydev.editor.commentblocks.CommentBlocksPreferences;
import org.python.pydev.editor.correctionassist.docstrings.DocstringsPrefPage;
import org.python.pydev.editor.hover.PyHoverPreferencesPage;
import org.python.pydev.editor.preferences.PydevEditorPrefs;
import org.python.pydev.parser.PyParserManager;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.pyunit.preferences.PyunitPrefsPage;
import org.python.pydev.ui.filetypes.FileTypesPreferencesPage;
import org.python.pydev.ui.importsconf.ImportsPreferencesPage;
import org.python.pydev.ui.interpreters.PythonInterpreterManager;

public class PydevPrefsInitializer  extends AbstractPreferenceInitializer{

	private static String PYTHON_EXEC_FILE;
	
	/**
	 * Set python executable name according to the operating system
	 */
	static {
    	//executable name depends on the operating system
    	String executableFileName = "python";
    	String osName = System.getProperty("os.name");
    	if (osName.startsWith("Windows")) {
    		executableFileName = "python.exe";
    	}
    	PYTHON_EXEC_FILE = executableFileName;
	}

    @Override
    public void initializeDefaultPreferences() {
        Preferences node = new DefaultScope().getNode(PydevPlugin.DEFAULT_PYDEV_SCOPE);

        //text
        node.putBoolean(PydevEditorPrefs.SMART_INDENT_PAR, PydevEditorPrefs.DEFAULT_SMART_INDENT_PAR);
        node.putBoolean(PydevEditorPrefs.AUTO_PAR, PydevEditorPrefs.DEFAULT_AUTO_PAR);
        node.putBoolean(PydevEditorPrefs.AUTO_INDENT_TO_PAR_LEVEL, PydevEditorPrefs.DEFAULT_AUTO_INDENT_TO_PAR_LEVEL);
        node.putBoolean(PydevEditorPrefs.AUTO_DEDENT_ELSE, PydevEditorPrefs.DEFAULT_AUTO_DEDENT_ELSE);
        node.putBoolean(PydevEditorPrefs.AUTO_COLON, PydevEditorPrefs.DEFAULT_AUTO_COLON);
        node.putBoolean(PydevEditorPrefs.AUTO_BRACES, PydevEditorPrefs.DEFAULT_AUTO_BRACES);
        node.putBoolean(PydevEditorPrefs.AUTO_WRITE_IMPORT_STR, PydevEditorPrefs.DEFAULT_AUTO_WRITE_IMPORT_STR);
    
        node.putInt(PydevEditorPrefs.TAB_WIDTH, PydevEditorPrefs.DEFAULT_TAB_WIDTH);
        
        //comment blocks
        node.put(CommentBlocksPreferences.MULTI_BLOCK_COMMENT_CHAR, CommentBlocksPreferences.DEFAULT_MULTI_BLOCK_COMMENT_CHAR);
        node.putBoolean(CommentBlocksPreferences.MULTI_BLOCK_COMMENT_SHOW_ONLY_CLASS_NAME, CommentBlocksPreferences.DEFAULT_MULTI_BLOCK_COMMENT_SHOW_ONLY_CLASS_NAME);
        node.putBoolean(CommentBlocksPreferences.MULTI_BLOCK_COMMENT_SHOW_ONLY_FUNCTION_NAME, CommentBlocksPreferences.DEFAULT_MULTI_BLOCK_COMMENT_SHOW_ONLY_FUNCTION_NAME);
        node.put(CommentBlocksPreferences.SINGLE_BLOCK_COMMENT_CHAR, CommentBlocksPreferences.DEFAULT_SINGLE_BLOCK_COMMENT_CHAR);
        node.putBoolean(CommentBlocksPreferences.SINGLE_BLOCK_COMMENT_ALIGN_RIGHT, CommentBlocksPreferences.DEFAULT_SINGLE_BLOCK_COMMENT_ALIGN_RIGHT);
        
        //checkboxes
        node.putBoolean(PydevEditorPrefs.SUBSTITUTE_TABS, PydevEditorPrefs.DEFAULT_SUBSTITUTE_TABS);
        node.putBoolean(PydevEditorPrefs.AUTO_ADD_SELF, PydevEditorPrefs.DEFAULT_AUTO_ADD_SELF);
        node.putBoolean(PydevEditorPrefs.GUESS_TAB_SUBSTITUTION, PydevEditorPrefs.DEFAULT_GUESS_TAB_SUBSTITUTION);
        
        //matching
        node.putBoolean(PydevEditorPrefs.USE_MATCHING_BRACKETS, PydevEditorPrefs.DEFAULT_USE_MATCHING_BRACKETS);
        node.put(PydevEditorPrefs.MATCHING_BRACKETS_COLOR, StringConverter.asString(PydevEditorPrefs.DEFAULT_MATCHING_BRACKETS_COLOR));
        node.putInt(PydevEditorPrefs.MATCHING_BRACKETS_STYLE, PydevEditorPrefs.DEFAULT_MATCHING_BRACKETS_STYLE);
        
        //colors
        node.put(PydevEditorPrefs.CODE_COLOR,StringConverter.asString(PydevEditorPrefs.DEFAULT_CODE_COLOR));
        node.put(PydevEditorPrefs.NUMBER_COLOR,StringConverter.asString(PydevEditorPrefs.DEFAULT_NUMBER_COLOR));
        node.put(PydevEditorPrefs.DECORATOR_COLOR,StringConverter.asString(PydevEditorPrefs.DEFAULT_DECORATOR_COLOR));
        node.put(PydevEditorPrefs.KEYWORD_COLOR,StringConverter.asString(PydevEditorPrefs.DEFAULT_KEYWORD_COLOR));
        node.put(PydevEditorPrefs.SELF_COLOR,StringConverter.asString(PydevEditorPrefs.DEFAULT_SELF_COLOR));
        node.put(PydevEditorPrefs.STRING_COLOR,StringConverter.asString(PydevEditorPrefs.DEFAULT_STRING_COLOR));
        node.put(PydevEditorPrefs.COMMENT_COLOR,StringConverter.asString(PydevEditorPrefs.DEFAULT_COMMENT_COLOR));
        node.put(PydevEditorPrefs.BACKQUOTES_COLOR,StringConverter.asString(PydevEditorPrefs.DEFAULT_BACKQUOTES_COLOR));
        node.put(PydevEditorPrefs.CLASS_NAME_COLOR, StringConverter.asString(PydevEditorPrefs.DEFAULT_CLASS_NAME_COLOR));
        node.put(PydevEditorPrefs.FUNC_NAME_COLOR,  StringConverter.asString(PydevEditorPrefs.DEFAULT_FUNC_NAME_COLOR));
        //for selection colors see initializeDefaultColors()
        
        //font style
        node.putInt(PydevEditorPrefs.CODE_STYLE, PydevEditorPrefs.DEFAULT_CODE_STYLE);
        node.putInt(PydevEditorPrefs.NUMBER_STYLE, PydevEditorPrefs.DEFAULT_NUMBER_STYLE);
        node.putInt(PydevEditorPrefs.DECORATOR_STYLE, PydevEditorPrefs.DEFAULT_DECORATOR_STYLE);
        node.putInt(PydevEditorPrefs.KEYWORD_STYLE, PydevEditorPrefs.DEFAULT_KEYWORD_STYLE);
        node.putInt(PydevEditorPrefs.SELF_STYLE, PydevEditorPrefs.DEFAULT_SELF_STYLE);
        node.putInt(PydevEditorPrefs.STRING_STYLE, PydevEditorPrefs.DEFAULT_STRING_STYLE);
        node.putInt(PydevEditorPrefs.COMMENT_STYLE, PydevEditorPrefs.DEFAULT_COMMENT_STYLE);
        node.putInt(PydevEditorPrefs.BACKQUOTES_STYLE, PydevEditorPrefs.DEFAULT_BACKQUOTES_STYLE);
        node.putInt(PydevEditorPrefs.CLASS_NAME_STYLE, PydevEditorPrefs.DEFAULT_CLASS_NAME_STYLE);
        node.putInt(PydevEditorPrefs.FUNC_NAME_STYLE, PydevEditorPrefs.DEFAULT_FUNC_NAME_STYLE);
        
        //no UI
        node.putInt(PydevEditorPrefs.CONNECT_TIMEOUT, PydevEditorPrefs.DEFAULT_CONNECT_TIMEOUT);
        
        
        //pydev todo tasks
        node.put(PyTodoPrefPage.PY_TODO_TAGS, PyTodoPrefPage.DEFAULT_PY_TODO_TAGS);
        
        //builders
        node.putBoolean(PyDevBuilderPrefPage.USE_PYDEV_BUILDERS, PyDevBuilderPrefPage.DEFAULT_USE_PYDEV_BUILDERS);
        node.putBoolean(PyParserManager.USE_PYDEV_ANALYSIS_ONLY_ON_DOC_SAVE, PyDevBuilderPrefPage.DEFAULT_USE_PYDEV_ONLY_ON_DOC_SAVE);
        node.putInt(PyParserManager.PYDEV_ELAPSE_BEFORE_ANALYSIS, PyDevBuilderPrefPage.DEFAULT_PYDEV_ELAPSE_BEFORE_ANALYSIS);
        node.putBoolean(PyDevBuilderPrefPage.ANALYZE_ONLY_ACTIVE_EDITOR, PyDevBuilderPrefPage.DEFAULT_ANALYZE_ONLY_ACTIVE_EDITOR);
        
        //code folding 
        node.putBoolean(PyDevCodeFoldingPrefPage.USE_CODE_FOLDING, PyDevCodeFoldingPrefPage.DEFAULT_USE_CODE_FOLDING);
        node.putBoolean(PyDevCodeFoldingPrefPage.FOLD_IF, PyDevCodeFoldingPrefPage.DEFAULT_FOLD_IF);
        node.putBoolean(PyDevCodeFoldingPrefPage.FOLD_WHILE, PyDevCodeFoldingPrefPage.DEFAULT_FOLD_WHILE);
        node.putBoolean(PyDevCodeFoldingPrefPage.FOLD_FOR, PyDevCodeFoldingPrefPage.DEFAULT_FOLD_FOR);
        node.putBoolean(PyDevCodeFoldingPrefPage.FOLD_CLASSDEF, PyDevCodeFoldingPrefPage.DEFAULT_FOLD_CLASSDEF);
        node.putBoolean(PyDevCodeFoldingPrefPage.FOLD_FUNCTIONDEF, PyDevCodeFoldingPrefPage.DEFAULT_FOLD_FUNCTIONDEF);
        node.putBoolean(PyDevCodeFoldingPrefPage.FOLD_COMMENTS, PyDevCodeFoldingPrefPage.DEFAULT_FOLD_COMMENTS);
        node.putBoolean(PyDevCodeFoldingPrefPage.FOLD_STRINGS, PyDevCodeFoldingPrefPage.DEFAULT_FOLD_STRINGS);
        node.putBoolean(PyDevCodeFoldingPrefPage.FOLD_WITH, PyDevCodeFoldingPrefPage.DEFAULT_FOLD_WITH);
        node.putBoolean(PyDevCodeFoldingPrefPage.FOLD_TRY, PyDevCodeFoldingPrefPage.DEFAULT_FOLD_TRY);
        node.putBoolean(PyDevCodeFoldingPrefPage.FOLD_IMPORTS, PyDevCodeFoldingPrefPage.DEFAULT_FOLD_IMPORTS);
        

        //coding style
        node.putBoolean(PyCodeStylePreferencesPage.USE_LOCALS_AND_ATTRS_CAMELCASE, PyCodeStylePreferencesPage.DEFAULT_USE_LOCALS_AND_ATTRS_CAMELCASE);
        
        //code formatting
        node.putBoolean(PyCodeFormatterPage.USE_ASSIGN_WITH_PACES_INSIDER_PARENTESIS, PyCodeFormatterPage.DEFAULT_USE_ASSIGN_WITH_PACES_INSIDE_PARENTESIS);
        node.putBoolean(PyCodeFormatterPage.USE_OPERATORS_WITH_SPACE, PyCodeFormatterPage.DEFAULT_USE_OPERATORS_WITH_SPACE);
        node.putBoolean(PyCodeFormatterPage.USE_SPACE_AFTER_COMMA,    PyCodeFormatterPage.DEFAULT_USE_SPACE_AFTER_COMMA);
        node.putBoolean(PyCodeFormatterPage.USE_SPACE_FOR_PARENTESIS, PyCodeFormatterPage.DEFAULT_USE_SPACE_FOR_PARENTESIS);

        //initialize pyunit prefs
        node.putInt(PyunitPrefsPage.PYUNIT_VERBOSITY, PyunitPrefsPage.DEFAULT_PYUNIT_VERBOSITY);
        node.put(PyunitPrefsPage.PYUNIT_TEST_FILTER, PyunitPrefsPage.DEFAULT_PYUNIT_TEST_FILTER);
        
        // Docstrings
        node.put(DocstringsPrefPage.P_DOCSTRINGCHARACTER, DocstringsPrefPage.DEFAULT_P_DOCSTRINGCHARACTER);
        node.put(DocstringsPrefPage.P_TYPETAGGENERATION, DocstringsPrefPage.DEFAULT_P_TYPETAGGENERATION);
        node.put(DocstringsPrefPage.P_DONT_GENERATE_TYPETAGS, DocstringsPrefPage.DEFAULT_P_DONT_GENERATE_TYPETAGS);
        
        //file types
        node.put(FileTypesPreferencesPage.VALID_SOURCE_FILES, FileTypesPreferencesPage.DEFAULT_VALID_SOURCE_FILES);
        node.put(FileTypesPreferencesPage.FIRST_CHOICE_PYTHON_SOURCE_FILE, FileTypesPreferencesPage.DEFAULT_FIRST_CHOICE_PYTHON_SOURCE_FILE);
        
        //imports
        node.putBoolean(ImportsPreferencesPage.GROUP_IMPORTS, ImportsPreferencesPage.DEFAULT_GROUP_IMPORTS);
        node.putBoolean(ImportsPreferencesPage.MULTILINE_IMPORTS, ImportsPreferencesPage.DEFAULT_MULTILINE_IMPORTS);
        node.put(ImportsPreferencesPage.BREAK_IMPORTS_MODE, ImportsPreferencesPage.DEFAULT_BREAK_IMPORTS_MODE);
        
        //hover
        node.putBoolean(PyHoverPreferencesPage.SHOW_DOCSTRING_ON_HOVER, PyHoverPreferencesPage.DEFAULT_SHOW_DOCSTRING_ON_HOVER);
        node.putBoolean(PyHoverPreferencesPage.SHOW_DEBUG_VARIABLES_VALUES_ON_HOVER, PyHoverPreferencesPage.DEFAULT_SHOW_DEBUG_VARIABLES_VALUES_ON_HOVER);
        
        /*
         * Set a defualt python interpreter if noone is defined
         */
        String currentPythonPath = node.get(PythonInterpreterManager.PYTHON_INTERPRETER_PATH, "");
        if (!currentPythonPath.isEmpty()) {
        	return;
        }
        
        /*
         * Set default python interpreter as preference has not been defined
         */
		String executable = getPythonExecutablePath();
		
		/*
		 * Dummy monitor
		 */
		IProgressMonitor monitor = new NullProgressMonitor();
		
		try {
			String defaultInterpreter = PythonInterpreterManager.doCreateInterpreterInfo(executable, monitor, false).toString();
			//initialize python interpreters
	        node.put(PythonInterpreterManager.PYTHON_INTERPRETER_PATH, defaultInterpreter);
		} catch (CoreException e) {
			e.printStackTrace();
		}	
    }
    
    /**
     * Get python executable path inside spell
     * @return
     */
    private String getPythonExecutablePath() {
    	String spellHome = System.getenv("SPELL_COTS");
    	if (spellHome != null) 
    	{
        	File spellHomeFile = new File(spellHome);
        	String pythonExecutable = lookForPythonExecutable(spellHomeFile);
        	return pythonExecutable;
    	}
    	return null;
    }
    
    /**
     * Check if this file contains the python interpreter executable
     * @param file
     * @return
     */
    private String lookForPythonExecutable(File file) {   	
    	//search for the executable file
    	if (file.isFile()) {
    		String fileName = file.getName();
    		if (fileName.equals(PYTHON_EXEC_FILE) && file.canExecute()) {
    			return file.getAbsolutePath();
    		}
    	} else {
    		for (File children : file.listFiles()) {
    			String result = lookForPythonExecutable(children);
    			if (result != null) {
    				return result;
    			}
    		}
    	}
    	return null;
    }
}