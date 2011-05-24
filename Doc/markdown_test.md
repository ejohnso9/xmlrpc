
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


## 2. Create a new XCode project and bring in XMLRPC code.

1. Launch the XCode application

1. Select "Create a new Xcode project"

1. Under "iOS", select "View-based Application" and click "Choose..."

1. Tell XCode what to name your project and where to put it. For example
    "SimpleXMLRPCClient" under your home directory.

1. Create a new group.

Eric is distributing XMLRPC as a library, but I had issues trying to get
the library properly linked in. I'm going to show you a simple way to
simply pull his source into our simple project. This worked for me. If
you have the know-how and it works for you, you can simply link with his
library - I'm not going to cover that here.)

Right-click on the top tree node under "Groups & Filter", select Add ->
"New Group".  Name the new group (folder) "XMLRPC".

1. Bring the XMLRPC source into the project.

Right-click on your new group icon and select Add -> "Existing Files..."
XCode will present you with the file broser. Navigate to directory that
you downloaded in step 1 (`.../xmlrpc/`).  Using CMD-Shift, select all
of the .m and .h source files that begin with one of {XMLRPC, NSData, NSString}
20 files: (skip `XMLRPC.pch`). Click the Add button, and in the
dialog that is presented, check the "Copy items into destination group's
folder" item. Again, click Add button.

All the source code we need to make XMLRPC run should now be sitting
under your XMLRPC group.


## 3. Modify the project to call the Sample Server

This project will have no distraction with iPhone interface whatsoever.
We will put the code in place to run as the application launches, using
the NSLog function to verify basic communication with the server. The
output will will be visible in the console (you can launch that window
with CMD-Shift-R or from the Build menu).

1. Modify header file.

    Let's fix the header file to account for the library versus static file
    issue. In `XMLRPC.h`, change these 5 lines:

        #import <XMLRPC/XMLRPCConnection.h>
        #import <XMLRPC/XMLRPCConnectionDelegate.h>
        #import <XMLRPC/XMLRPCConnectionManager.h>
        #import <XMLRPC/XMLRPCResponse.h>
        #import <XMLRPC/XMLRPCRequest.h>

    to these 10 lines:

        // #import <XMLRPC/XMLRPCConnection.h>
        // #import <XMLRPC/XMLRPCConnectionDelegate.h>
        // #import <XMLRPC/XMLRPCConnectionManager.h>
        // #import <XMLRPC/XMLRPCResponse.h>
        // #import <XMLRPC/XMLRPCRequest.h>

        #import "XMLRPCConnection.h"
        #import "XMLRPCConnectionDelegate.h"
        #import "XMLRPCConnectionManager.h"
        #import "XMLRPCResponse.h"
        #import "XMLRPCRequest.h"


1. Import the XMLRPC source.

    Under the Classes group, you should have `*AppDelegate` `.m` and `.h` files
    (named according to the project name you provided). At the top of your
    *AppDelegate.h file, add the following line right under the existing
    import:

        #import "XMLRPC.h"

1. Create a function to make an XMLRPC call.

    Still within `*AppDelegate.m`, add the following method below the
    definition of `application:didFinishLaunchingWithOptions:` (note this is
    copied right out of `README.md` in the original project):

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


1. Call our new method.

    Just before the return statement at the end of
    `application:didFinishLaunchingWithOptions:`, add the following call:

        [self makeRemoteCall];

    That's really all there is to the calling side.

    Now, we need to make three changes to the `*AppDelegate.h` file:

1.  Declare ourselves as an `XMLRPCConnectionDelegate`.  

    Notice the call above to `[manager spawnConnectionWithXMLRPCRequest:
    request delegate: self];`?  We've told the manager to call back into
    this object when it receives a response to our request, so we need
    to build the infrastructure it expects so it can do so.

    Add the protocol declaration so that the `@interface` line reads:

        @interface SimpleXMLRPCClientAppDelegate : NSObject <UIApplicationDelegate, XMLRPCConnectionDelegate> {

1. Add the `#import` of the XMLRPC headers to `*AppDelegate.h`.

    Add the following line near the top of `*AppDelegate.h` file:

        #import "XMLRPC.h"
        
1. LEFT OFF HERE

1. Implement the callback interface.

    In `*AppDelegate.m` add the following code:

        #pragma mark -
        #pragma mark XMLRPCConnectionDelegate

        - (void)request:(XMLRPCRequest *)request 
        didReceiveResponse:(XMLRPCResponse *)response
        {
            NSLog(@"%s Line %d: response = %@", __PRETTY_FUNCTION__, __LINE__, response);

            // are we getting back a "valid" response?
            if ([response isFault])
                NSLog(@"%@ call FAILED!", [request method]);
            else
                NSLog(@"%@ call SUCCEEDED!", [request method]);

            NSLog(@"return object is: %@", [response object]);
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
