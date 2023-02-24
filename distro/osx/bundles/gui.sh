# Script to run the Galleon Server on Mac OS X 10.4.x

# The MIT License

# Copyright (c) 2005 Mac Geekery

# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

# 

# Find where we're located and remove any problems with dot directories
export GALLEON_HOME=../
export JAVA_HOME=/Library/Java/Home

export OLDCLASSPATH=$CLASSPATH

export CLASSPATH=$CLASSPATH:$base/conf
for FILE in $GALLEON_HOME/lib/*
	do export CLASSPATH=$CLASSPATH:$FILE;
done;

java -Xms32m -Xmx32m \
	-Xdock:name="Galleon" \
	-Xdock:icon="$GALLEON_HOME/bin/Galleon.icns" \
	-Dcom.apple.mrj.application.apple.menu.about.name="Galleon" \
	-Dapple.laf.useScreenMenuBar=true \
	-Dcom.apple.mrj.application.live-resize=true \
	org.lnicholls.galleon.gui.Galleon &

export CLASSPATH=$OLDCLASSPATH
