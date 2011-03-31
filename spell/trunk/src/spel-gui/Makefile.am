include ${top_srcdir}/src/common.mk

DEST_DIR=$(top_builddir)/src/spel-gui/
SRC_DIR=$(top_srcdir)/src/spel-gui

prepare:
	if [ ! -d ${WITH_ECLIPSE} ]; \
        then \
            echo "FATAL: cannot find eclipse installation at ${WITH_ECLIPSE}";\
            exit -1; \
        fi

core-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.core ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

language-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.language ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

procs-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.procs ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

print-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.print ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

gui-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

codeview-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.codeview ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

textview-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.textview ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

shellview-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.shellview ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

commskt-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.commsocket ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

shell-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.shell ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

preferences-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.preferences ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

preferences-gui-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.preferences.ui ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

callstack-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.callstack ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

wvariables-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.watchvariables ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

outline-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.outline ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

doc-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.documentation ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

hifly-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.hifly ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

test-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.unittest ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

test-procs-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.gui.test.procs ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}
 

all-local: prepare \
	   core-plugin \
	   language-plugin \
	   preferences-plugin \
	   preferences-gui-plugin \
	   procs-plugin \
	   print-plugin \
	   gui-plugin \
	   codeview-plugin \
	   textview-plugin \
       shellview-plugin \
	   commskt-plugin \
	   shell-plugin \
       callstack-plugin \
       wvariables-plugin \
       outline-plugin \
	   hifly-plugin \
       doc-plugin \
       test-plugin \
       test-procs-plugin

clean-local:
	rm -rf ${DEST_DIR}/lib_plugins
	rm -rf ${DEST_DIR}/obj
	rm -f ${DEST_DIR}/*.log
	rm -f ${DEST_DIR}/*.jar

install-exec-hook:
	mkdir -p ${exec_prefix}/spel-gui
if HAVE_RSYNC
	rsync -av --exclude=.svn ${SRC_DIR}/bin/* ${exec_prefix}/spel-gui/
	rsync -av ${DEST_DIR}/*.jar ${exec_prefix}/spel-gui/plugins/
else
	cp -rvf ${SRC_DIR}/bin ${exec_prefix}/spel-gui
	find ${exec_prefix}/spel-gui -name ".svn" | xargs rm -rf
	cp -vf ${DEST_DIR}/*.jar ${exec_prefix}/spel-gui/plugins/.
endif

