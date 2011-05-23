
Another environment to try to get up to speed in.

I don't know markdown - seems like GitHub makes the conversion
automatically and displays markdown.

Home page for markdown language seems to be:
    http://daringfireball.net/projects/markdown/


# Hello, XMLRPC!

A Beginner's step-by-step guide to getting up to speed using Eric
Czarny's XMLRPC for MacOS/iOS. 

I didn't really find the sort of documentation with this project I had
hoped for, so I'll try to provide it.

I think XMLRPC is actually a lot easier to use than you might think by
opening the Tools/Sample Client project, so I will attempt to walk you
through the first steps of getting things actually going.


## 1. Git the source distribution

Probably the best way to do this is to simply clone the GitHub
repository using 'git'. You'll need a git client for this, which can be
found at the [GIT download page](http://git-scm.com/download) 

To do this from a Unix-like environment, you would execute the
following command:

    git clone git://github.com/eczarny/xmlrpc.git

This should create the `xmlrpc/` directory locally. 

Assuming you already have Apple's XCode IDE installed, we have
everything we need to complete this project.  (If not, you can probably
handle things from here: 
[iOS Dev Center](http://developer.apple.com/devcenter/ios/index.action) )


## 2. Create the XCode project

1. Launch the XCode application
1. Select "Create a new Xcode project"
1. Under "iOS", select "View-based Application" and click "Choose..."
1. Tell XCode what to name your project and where to put it. For example
    "SimpleXMLRPCClient" under your home directory.



