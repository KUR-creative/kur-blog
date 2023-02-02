# KUR BLOG
## Run REPL
Just run `Calva: Start a Project REPL and Connect (aka Jack-In)`. \
If you wnat CLJS REPL, see `md2x/README.md`

## Local test
REPL: `(clojure.test/run-tests)` \
End to End: `setup-dev-local.sh`, `teardown-dev-local.sh` 

## Release process
1. Run `release.sh` to create `release` directory with new version tag
2. Push `master` branch and new tag to Github repository (then Github action do the follow-ups)
3. Copy `release` directory to remote(Lightsail Instance) 
4. run `deploy.sh`. It setups configs, start and enable publisher(nginx), updater services.

## Scripts (`./script/`)
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

## Rules and Conventions
Use singular (directory) names rather than plural ones