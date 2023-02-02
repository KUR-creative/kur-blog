#!/bin/bash

sudo systemctl stop blog-updater.service
sudo systemctl disable blog-updater.service

sudo systemctl stop nginx.service
sudo systemctl disable nginx.service
