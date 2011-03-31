PLUGIN_COMPILER=${top_srcdir}/src/eclipse_plugin_build.sh
RCP_PLUGINS=${top_srcdir}/rcp/plugins
RCP_TEMPLATES=${top_srcdir}/src/eclipse_build_templates
DEST_DIR=$(top_builddir)/src/
SRC_DIR=$(top_srcdir)/src/

dev-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

config-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.config ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

language-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.language ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

database-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.database ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

database-ui-plugin: 
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.database.ui ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

spelleditor-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.spelleditor ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

dicteditor-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.dictionaryeditor ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

database-test-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.database.test ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

remove-generic:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.activity.generic ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

remove-subclipse:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.activity.subclipse ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

remove-pydev:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.activity.pydev ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

control-version:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.control.version ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

documentation-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.documentation ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

semantics-plugin:
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.scheck ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.scheck.ui ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.scheck.gotoc ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.scheck.header ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.scheck.functions ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.scheck.sfunctions ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}
	${PLUGIN_COMPILER} $(SRC_DIR) com.astra.ses.spell.dev.scheck.sfpack1 ${DEST_DIR} ${RCP_TEMPLATES} ${WITH_ECLIPSE} ${RCP_PLUGINS}

all-local: config-plugin        \
           dev-plugin           \
           language-plugin      \
           database-plugin      \
           spelleditor-plugin   \
           database-ui-plugin   \
           dicteditor-plugin    \
           database-test-plugin \
           remove-generic       \
           remove-pydev         \
           remove-subclipse     \
           control-version      \
           documentation-plugin \
		   semantics-plugin

clean-local:
	rm -rf ${DEST_DIR}/lib_plugins
	rm -rf ${DEST_DIR}/obj
	rm -f ${DEST_DIR}/*.log
	rm -f ${DEST_DIR}/*.jar

install-exec-hook:
	mkdir -p ${exec_prefix}/spel-dev
if HAVE_RSYNC
	rsync -av --exclude=.svn ${SRC_DIR}/bin/* ${exec_prefix}/spel-dev/
	rsync -av ${DEST_DIR}/*.jar ${exec_prefix}/spel-dev/plugins/
else
	cp -r ${SRC_DIR}/bin ${exec_prefix}/spel-dev
	find ${exec_prefix}/spel-dev -name ".svn" | xargs rm -rf
	cp ${DEST_DIR}/*.jar ${exec_prefix}/spel-dev/plugins/.
endif


