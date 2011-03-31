install-exec-hook:
	mkdir -p ${exec_prefix}/doc
if HAVE_RSYNC
	rsync -av --exclude-from=${top_srcdir}/exclude.list ${top_srcdir}/doc/* ${exec_prefix}/doc/
else
	cp -r ${top_srcdir}/doc/* ${exec_prefix}/doc/.
	rm -f ${exec_prefix}/doc/Makefile*
endif

install-data-hook:
	mkdir -p ${exec_prefix}/doc
if HAVE_RSYNC
	rsync -av --exclude-from=${top_srcdir}/exclude.list ${top_srcdir}/doc/* ${exec_prefix}/doc/
else
	cp -r ${top_srcdir}/doc/* ${exec_prefix}/doc/.
	rm -f ${exec_prefix}/doc/Makefile*
	find ${exec_prefix}/doc -name ".svn" | xargs rm -rf
endif


