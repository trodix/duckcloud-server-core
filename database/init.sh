#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
INIT_DIR=$SCRIPT_DIR/sql

for file in $(ls "$INIT_DIR" | sort)
do
  su postgres -c "psql -U postgres -f $INIT_DIR/$file"
done