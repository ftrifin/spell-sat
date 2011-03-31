include $(top_srcdir)/common.mk

bin_PROGRAMS = SPELL-Listener

SPELL_Listener_SOURCES = src/SPELLlistenerMain.C

SPELL_Listener_LDFLAGS = -lxerces-c -lpthread -lrt -lutil -ldl $(AM_LDFLAGS)

SPELL_Listener_LDADD = ${top_builddir}/lib/SPELL_UTIL/libSPELL_UTIL.la \
                       ${top_builddir}/lib/SPELL_IPC/libSPELL_IPC.la  \
                       ${top_builddir}/lib/SPELL_WS/libSPELL_WS.la   \
                       ${top_builddir}/lib/SPELL_SYN/libSPELL_SYN.la  \
                       ${top_builddir}/lib/SPELL_WRP/libSPELL_WRP.la  \
                       ${top_builddir}/lib/SPELL_CFG/libSPELL_CFG.la  \
                       ${top_builddir}/lib/SPELL_PRD/libSPELL_PRD.la  \
                       ${top_builddir}/lib/SPELL_EXC/libSPELL_EXC.la  \
                       ${top_builddir}/lib/SPELL_CIF/libSPELL_CIF.la  \
                       ${top_builddir}/lib/SPELL_CIFS/libSPELL_CIFS.la  \
                       ${top_builddir}/lib/SPELL_LST/libSPELL_LST.la


