AUTOMAKE_OPTIONS=foreign

AM_CPPFLAGS = -I$(top_srcdir)/include $(PYTHON_CPPFLAGS) -O2 -fPIC -fno-strict-aliasing -g3 -Wall -c -fmessage-length=0 -MMD -MP
AM_LDFLAGS = $(PYTHON_LDFLAGS) $(PYTHON_EXTRA_LIBS) $(PYTHON_EXTRA_LDFLAGS)
RSYNC=rsync -av --exclude-from=${top_srcdir}/exclude.list 

