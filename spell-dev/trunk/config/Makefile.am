AUTOMAKE_OPTIONS = foreign

install-exec-hook:
	mkdir -p ${exec_prefix}/config
if HAVE_RSYNC
	rsync -av --exclude-from=${top_srcdir}/exclude.list ${top_srcdir}/config/dev/* ${exec_prefix}/config/
else
	cp -r ${top_srcdir}/config/dev/* ${exec_prefix}/config/.
	find ${exec_prefix}/config -name ".svn" | xargs rm -rf
endif

install-data-hook:
	mkdir -p ${exec_prefix}/config
if HAVE_RSYNC
	rsync -av --exclude-from=${top_srcdir}/exclude.list ${top_srcdir}/config/dev/* ${exec_prefix}/config/
else
	cp -r ${top_srcdir}/config/dev/* ${exec_prefix}/config/.
	find ${exec_prefix}/config -name ".svn" | xargs rm -rf
endif


