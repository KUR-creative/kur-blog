#!/usr/bin/env bash

# Copy the release directory to remote, 
# and then run this script in remote ssh
# Assume current working directory = release
if [ `basename "$PWD"` != 'release' ]
then
    echo current working directory is not release
    exit 1
fi

# Create /www (NOTE: could be extracted to a script)
sudo mkdir -p /www/blog/html
sudo mkdir -p /www/blog-base/md
sudo chown -R $USER:$USER /www

# Deploy updater
cp -r updater/target/* /www/blog-base
cp -r updater/config /www/blog-base/

sudo cp updater/blog-updater.service /etc/systemd/system
sudo systemctl daemon-reload 
sudo systemctl enable blog-updater.service
sudo systemctl start blog-updater.service

# Deploy publisher
sudo cp -r publisher/conf.d /etc/nginx/conf.d
sudo cp -r publisher/nginx.conf /etc/nginx/nginx.conf
sudo systemctl enable nginx.service
sudo systemctl start nginx.service
