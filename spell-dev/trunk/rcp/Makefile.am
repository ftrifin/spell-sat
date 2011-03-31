install-exec-hook:
	mkdir -p ${exec_prefix}/rcp
if HAVE_RSYNC
	rsync -av --exclude-from=${top_srcdir}/exclude.list ${top_srcdir}/rcp/* ${exec_prefix}/rcp/
else
	cp -r ${top_srcdir}/rcp/* ${exec_prefix}/rcp/.
	rm -f ${exec_prefix}/rcp/Makefile*
endif
