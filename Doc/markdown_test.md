This document is natively in the markdown language. If you are not
seeing rendered HTML, you can check out:
    http://daringfireball.net/projects/markdown/

I didn't find this sort of document I had hoped for with this project,
so I'll try to provide it.

# Hello, World!

This document is a beginner's step-by-step guide to getting up to speed
with Eric Czarny's XMLRPC for MacOS/iOS. 

I think XMLRPC is actually a lot easier to use than you might think by
opening the Tools/Sample Client project and trying to understand it, so
this document will walk you through the first steps of making a remote
call via XMLRPC.

## 0. Pre-requisites

I'm not assuming much about your knowledge, but there are a few tools
you will need:

1. Apple's Xcode IDE

    Available from: 
    [iOS Dev Center](http://developer.apple.com/devcenter/ios/index.action)

2. `git` (optional)

    Available from: [GIT download page](http://git-scm.com/download)

3. `ant`

    You probably already have this. You can check with:

        which ant

With those in place, we have everything we need to get going.
Let's jump into XMLRPC...

## 1. Get (or git)  the source distribution

Probably the best way to do this is to simply clone the GitHub repository
using a `git` client.  

To do this from a Unix-like environment such as OSX, you could execute
the following command, after changing to the directory where you want
the distribution to be:

    git clone git://github.com/eczarny/xmlrpc.git

This should create the `xmlrpc/` directory locally. 

## 2. Create a new XCode project and bring in the XMLRPC code.

1. Launch the XCode application

1. Select "Create a new Xcode project"

1. Under "iOS", select "View-based Application" and click "Choose..."

1. Tell XCode what to name your project and where to put it.  I suggest
`SimpleXMLRPCClient` for the project name and will refer to files by
this name. You can store the project under your home directory.

1. Create a new group.

    Eric is distributing XMLRPC as a library, but I was unable to get
    the library properly linked in. I'm going to show you a simple way
    to just pull his source into the project directly. 

    Right-click on the top tree node under "Groups & Filter", select 
    Add -> "New Group".  Name the new group (folder) "XMLRPC".

1. Bring the XMLRPC source into the project.

    Right-click on your new group icon and select Add -> "Existing
    Files..." 
    
    XCode will present you with the file browser. Navigate to the
    directory that you downloaded in step 1 (`.../xmlrpc/`).  Using
    CMD-Shift, select all of the `.m` and `.h` source files that begin
    with any of {XMLRPC, NSData, NSString} (20 files in all not
    including `XMLRPC.pch`). 

    Click the Add button, and in the dialog that is presented, check the
    "Copy items into destination group's folder" item. Again, click the
    "Add" button.

    All the source code we need to make XMLRPC run should now be sitting
    under your XMLRPC group.

## 3. Add custom code to make an XMLRPC call

This project will have no distraction with iPhone interface whatsoever -
there are plenty of other tutorials to teach you that part. This project
simply adds some methods and utilizes the `NSLog()` function to verify 
basic communication with the server.

1. Modify the main XMLRPC header file

    First, let's modify the header file to account for the library
    versus static file issue. Under your new group, in the `XMLRPC.h`
    file, change these 5 lines:

        #import <XMLRPC/XMLRPCConnection.h>
        #import <XMLRPC/XMLRPCConnectionDelegate.h>
        #import <XMLRPC/XMLRPCConnectionManager.h>
        #import <XMLRPC/XMLRPCResponse.h>
        #import <XMLRPC/XMLRPCRequest.h>

    to these 5 lines:

        #import "XMLRPCConnection.h"
        #import "XMLRPCConnectionDelegate.h"
        #import "XMLRPCConnectionManager.h"
        #import "XMLRPCResponse.h"
        #import "XMLRPCRequest.h"


1. Add code in `SimpleXMLRPCClient.h`

    At the top of your app delegate header file, add the following line
    right under the existing `UIKit` import:

        #import "XMLRPC.h"

1. Create a function to make an XMLRPC call.

    Switch to `SimpleXMLRPCClientAppDelegate.m`, and add the following
    method below the definition of
    `application:didFinishLaunchingWithOptions:` (note, this body of
    this method is copied right out of `README.md` in the original
    `xmlrpc` directory):

        - (void)makeRemoteCall
        {
            NSURL *URL = [NSURL URLWithString: @"http://127.0.0.1:8080/"];  
            XMLRPCRequest *request = [[XMLRPCRequest alloc] initWithURL: URL];
            XMLRPCConnectionManager *manager = [XMLRPCConnectionManager sharedManager];
            
            [request setMethod: @"Echo.echo" withParameter: @"Hello, World!"];
            
            NSLog(@"Request body: %@", [request body]);
            
            [manager spawnConnectionWithXMLRPCRequest: request delegate: self];
            
            [request release];
        }


1. Insert a call to our new method.

    Just before the return statement at the end of
    `application:didFinishLaunchingWithOptions:`, add the following call:

        [self makeRemoteCall];

    That's really all there is to the calling side.

    Now, we need to make three changes to the `*AppDelegate.h` file:

1.  Declare ourselves as an `XMLRPCConnectionDelegate`.  

    Did you this call (above? 
    
        [manager spawnConnectionWithXMLRPCRequest: request delegate: self];
        
    We've told the manager to call back into this object when it
    receives a response to our request, so we need to build the
    infrastructure the manager expects so it can do so.

    In the app delegate `.h` file, add the `XMLRPCConnectionDelegate`
    protocol declaration so that the `@interface` line reads:

        @interface SimpleXMLRPCClientAppDelegate : NSObject <UIApplicationDelegate, XMLRPCConnectionDelegate> {

1. Add a declaration for our custom function

    Right above the `@property` lines in `SimpleXMLRPCClient.h`, add a
    declaration for our custom function:

        - (void)makeRemoteCall;

1. Implement the callback interface.

    Switch back to `SimpleXMLRPCClientAppDelegate.m` and add the
    following code to implement the rest of be protocol methods:

        #pragma mark -
        #pragma mark XMLRPCConnectionDelegate

        - (void)request:(XMLRPCRequest *)request
        didReceiveResponse:(XMLRPCResponse *)response
        {
            NSLog(@"%s Line %d: response = %@",
                __PRETTY_FUNCTION__, __LINE__, response);
            // are we getting back a "valid" response?
            if ([response isFault])
                NSLog(@"%@ call FAILED!", [request method]);
            else
                NSLog(@"%@ call SUCCEEDED!", [request method]);
            NSObject *respObj = [response object];
            NSLog(@"Received: \"%@\" from the server!", respObj);
        }

        - (void)request:(XMLRPCRequest *)request 
        didFailWithError: (NSError *)error
        { }

        - (BOOL)request:(XMLRPCRequest *)request 
        canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *)protectionSpace
        {
            return YES;
        }

        - (void)request:(XMLRPCRequest *)request 
        didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge
        { }

        - (void)request:(XMLRPCRequest *)request 
        didCancelAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge
        { }

        #pragma mark -

1. Check the build

    OK, at this point we've added everything we need to run a sample
    client. Build (but don't run) you client to make sure everything is
    clean.  (CMD-Shift-B, CMD-B)

    If you don't have a clean build, fix any errors or review the
    document and redo steps until you get a clean build.

1. Build the sample server

    Eric Czarny has included a complete test server written in Java with
    the XMLRPC distribution.  I think that's awesome, and building and
    running it is a snap. From the directory above where you downloaded
    the XMLRPC distribution, execute the following two commands to build
    and build and launch the server:

        cd "xmlrpc/Tools/Test Server"
        ant

    You should see some output at the command line, and then be
    presented with a window titled "Control Panel" that has a very
    simple interface. 

    Click the "Start" button, and we're ready to go!
    
1. Run your application

    Go back to Xcode, and run your application. The final line of output
    should be:
    
1. Congratulations. Welcome to XMLRPC.

    That's not a very exciting application, but it is a real, genuine
    XMLRPC call. Other tutorials talk about putting XMLRPC to work in
    a more realistic environment.

