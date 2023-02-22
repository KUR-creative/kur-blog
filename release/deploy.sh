#!/usr/bin/env bash

# Copy the release directory to remote, 
# and then run this script in remote ssh
# Assume current working directory = release
if [ `basename "$PWD"` != 'release' ]
then
    echo current working directory is not release
    exit 1
fi

if [[ $1 = dev ]]
then
    publisher_dir=dev-publisher
    updater_unit=dev-blog-updater.service
else
    publisher_dir=publisher
    updater_unit=blog-updater.service
fi

# Create /www (NOTE: could be extracted to a script)
sudo mkdir -p /www/blog/html
sudo mkdir -p /www/blog-base/md
sudo chown -R $USER:$USER /www

# Copy frontend script files to /www/blog/resource/
\cp -r updater/target/src/js /www/blog/resource/site/
\cp -r updater/target/src/css /www/blog/resource/site/

# Deploy updater
cp -r updater/target/* /www/blog-base
cp -r updater/config /www/blog-base/

sudo cp updater/$updater_unit /etc/systemd/system/blog-updater.service
sudo systemctl daemon-reload 
sudo systemctl enable blog-updater.service
sudo systemctl stop blog-updater.service
sudo systemctl start blog-updater.service

echo Deploy publisher
sudo cp -r $publisher_dir/conf.d/* /etc/nginx/conf.d
sudo cp -r $publisher_dir/nginx.conf /etc/nginx/nginx.conf
sudo systemctl enable nginx.service
sudo systemctl stop nginx.service
sudo systemctl start nginx.service
