#!/usr/bin/env bash

if [ "$#" -ne 2 ]; then
    echo "Copy blog contents to /www. Just for dev e2e testing"
    echo "/www/blog/resource/site and /www/blog-base/md should be existing" 
    echo "before running ./release/deploy.sh"
    echo
    echo "Usage: $0 <md-path> <resource-path>"
    echo "Example:"
    echo "$0 /home/dev/outer-brain/blog/md /home/dev/outer-brain/blog/resource"
    exit 1
fi

\cp -rT $1 /www/blog-base/md
\cp -rT $2 /www/blog/resource

echo "Copy complete!"
