#!/bin/bash

# Assume current working directory = root of kur-blog repo
./script/compile-md2x.sh
clj -T:build uber

cd release
./deploy.sh dev
cd ..
