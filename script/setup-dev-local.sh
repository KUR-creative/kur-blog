#!/bin/bash

# Assume current working directory = root of kur-blog repo
./script/compile-md2x.sh
echo ""

clj -T:build clean
clj -T:build copy-dirs

# ./script/setup-dev-local.sh nouber # then no compiling uberjar
if [[ $# == 0 ]]
then
    clj -T:build uber
fi

cd release
./deploy.sh dev
cd ..
