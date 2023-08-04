## Initialization Guide
# After 
#  git clone https://github.com/KUR-creative/kur-blog.git
#  cd kur-blog

# Copy blog contents first
./script/copy-blog-contents.sh $1 $2
if [ "$?" -ne 0 ]; then
    exit 1
fi

# Init md2x first.
cd md2x
npm install
# Now if you want develop md2x, follow the md2x/README.md!

# Install nginx
sudo apt update
sudo apt install nginx
