#!/bin/sh

BASEPATH=`dirname $0`
DRIVER=$1

[[ -z "$DRIVER" ]] && echo "Must provide the driver name" && exit 1

DRIVER_PATH=$BASEPATH/$DRIVER

[[ ! -d "$DRIVER_PATH" ]] && echo "Cannot find driver directory: $DRIVER_PATH" && exit 1

cd $DRIVER_PATH
zip -r driver_$DRIVER *
cd -
mv $DRIVER_PATH/driver_${DRIVER}.zip $BASEPATH/.

echo "Done."
