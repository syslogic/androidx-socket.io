#!/usr/bin/env bash
PORT=3000
if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null ; then
    exit 1
else
    exit 0
fi
