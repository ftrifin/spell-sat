AUTOMAKE_OPTIONS = foreign
SUBDIRS=spell

if WITH_EXECUTOR
SUBDIRS += executor server
endif

if WITH_EXECUTOR_CMD
SUBDIRS += executorcmd
endif

if WITH_SHELL
SUBDIRS += shell
endif

if WITH_LISTENER
SUBDIRS += listener
endif

if WITH_CONTEXT
SUBDIRS += context
endif

if WITH_GUI
SUBDIRS += spel-gui
endif


