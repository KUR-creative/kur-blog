#!/bin/bash

# Assume current working directory = root of kur-blog repo
clj -T:build uber

cd release
./deploy.sh dev
cd ..
