# ubuntu is missing this
sudo apt-get -y install libghc-zlib-dev

# as per http://erdgeist.org/arts/software/opentracker/
sudo apt-get -y install cvs
cvs -d :pserver:cvs@cvs.fefe.de:/cvs -z9 co libowfat
cd libowfat
make
cd ..
cvs -d:pserver:anoncvs@cvs.erdgeist.org:/home/cvsroot co opentracker
cd opentracker
make
