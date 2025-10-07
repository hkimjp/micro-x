#!/usr/bin/env bash
kill `lsof -ti:3040`
PORT=3040 AYEAR=2025 SUBJ=python-b UHOUR=tue2 L22="http://192.168.0.15:3022" \
java -jar micro-x.jar >log/micro-x 2>&1 &

