# KUR BLOG
## Run things
### Run REPL
Just run `Calva: Start a Project REPL and Connect (aka Jack-In)`. \
If you wnat CLJS REPL, see `md2x/README.md`

### Local test
REPL: `(clojure.test/run-tests)` \
End to End: `setup-dev-local.sh`, `teardown-dev-local.sh` 

### Release process
1. Run `release.sh` to create `release` directory with new version tag
2. Push `master` branch and new tag to Github repository (then Github action do the follow-ups)
3. Copy `release` directory to remote(Lightsail Instance) 
4. run `deploy.sh`. It setups configs, start and enable publisher(nginx), updater services.

### Scripts (`./script/`)
`compile-md2x.sh` \
Compile md2x cljs src to generate md2x.js, then save it into `src/js`

`release.sh <tag> <tag-message>` \
Release current version
1. Build target from clj src 
2. Save version metadata to `release/version.yml`
3. Commit and set tag

`setup-dev-local.sh` \
Deploy publisher and updater into local machine. \
Differences between dev vs release:
1. `dev-publisher/nginx.conf` - Comment out L5: user, L8: pid
2. `dev-publisher/blog.conf ` - Remove SSL codes
3. `dev-blog-updater.service` - Set user:gruop as kur:kur

`teardown-dev-local.sh` \
Stop and Disable publisher/updater services

`./script/copy-blog-contents.sh <obrain-md-path> <obrain-resource-path>` \
The updater needs non-empty `/www/blog-base/md`, and 
The `./release/deploy.sh` needs `/www/blog/resource/site`. 
This script copies `<obrain-md-path> -> /www/blog-base/md`, `<obrain-resource-path> -> /www/blog/resource/site`

`./script/init-ubuntu22.sh` \
Initiailize dev environment after `git clone https://github.com/KUR-creative/kur-blog.git`.

## Development
### Code structure and namespace explanation
Check `$ tree src`.

```
kur.util           project agnostic utility functions

kur.blog.main      Entry point
kur.blog.state     The only state of entire app
kur.blog.updater   State update logic
kur.blog.monitor   Monitor directory(md, ..) and call update-fn
                   to change state when an event occurs

kur.blog.policy    My own rules
kur.blog.obsidian  Imitation implementation of Obsidian features
kur.blog.page      Data entity and logics for each page
kur.blog.look      View of each page. It use src/js and src/css
```

### Rules and Conventions
Use singular (directory) names rather than plural ones
