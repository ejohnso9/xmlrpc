    File: ExpandingAPI_tutorial.md
    Date: 2011.MAY.25
    Author: Erik Johnson

This document is natively in the markdown language. If you are not
seeing rendered HTML, check out:
    http://daringfireball.net/projects/markdown/

[Tutorial Index](tutorialIndex.md)

# Expanding the API

OK, so you got through the "Hello, World!" tutorial, but is this whole
XMLRPC thing really going to meet your needs? Ultimately, you'll have to
answer that, but this tutorial will take you through the next few
simple steps to adding some more objects and methods on the server side,
and calling them from the client side.


## 0. Pre-requisites / notes

This tutorial picks up where where 
[the previous one](HelloWorld_tutorial.md) 
left off, so I'm assuming you've been able to make that one work properly.

`.` and `..` are actual directory arguments that Unix understands, but
in several places I may refer to directories as `.../whatever` and here
I mean for you to fill in `...` with the logical leading path (and not
try to use `...` literally). For example, 

    .../xmlrpc/

would be wherever you unpacked the source distribution directory.

    .../Test Server/

would actually be

    .../xmlrpc/Tools/Test Server


## 1. "!dlroW ,elloH": expanding the Java API

Let's add a function of our own and see if we are able to call it.
From the `xmlrpc` distribution directory, get down into the directory
where the server-side API is implemented:

    cd ".../xmlrpc/Tools/Sample Server"
    cd src/com/divisiblebyzero/xmlrpc/model/handlers

here you will see the Java source file for the `Echo` class:

    cat Echo.java

First, let's add another method into this same object, and verify we can
call our new function. Using your favorite editor, insert the following
method into the `Echo.java` source file.

    /**
     * Reverses the argument String.
     *
     * @param input - the String to be reversed
     * @return 'input' in reverse order
     */
    public String reverse(String input) 
    {
        int len = input.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = len - 1; i >= 0; i--)
            sb.append(input.charAt(i));
        return sb.toString();
    }


## 2. Test the new method

0. If your server is still running, end that application.

1. Rebuild the server 

    get to `.../xmlrpc/Tools/Test Server` and simply run `ant` again:

        cd ../../../../../..
        ant

    Again, you should see some output from `ant` and then your server
    should launch.  Just click the "Start" button, and we're ready to
    call our modified server. (I love that: so simple.  Beautiful!)

2. Modify the client to call our new method

    Get back to Xcode and the `SimpleXMLRPCClientAppDelegate.m' file.
    Change the call line in `makeRemoteCall` from

        [request setMethod: @"Echo.echo" withParameter: @"Hello, World!"];

    to

        [request setMethod: @"Echo.reverse" withParameter: @"Hello, World!"];

    Build and run your client: (CMD-Shift-B, CMD-Enter) In the Debugger
    Console window, you should have output similar to the following:

        2011-05-25 09:56:59.243 SimpleXMLRPCClient[359:207] Echo.reverse call SUCCEEDED!
        2011-05-25 09:56:59.244 SimpleXMLRPCClient[359:207] Received: "!dlroW ,olleH" from the server!

    OK... so, there's the first baby step. 


## 3. Install a new handler class

Let's continue building our confidence and skills by providing an
entirely separate implementation class.

In `.../model/handlers/`, install this source file as `SimpleMath.java`:

    package com.divisiblebyzero.xmlrpc.model.handlers; 

    public class SimpleMath 
    {
        public int add(int a, int b) {
            return a + b;
        }

        public int sub(int a, int b) {
            return a - b;
        }

        public int mul(int a, int b) {
            return a * b;
        }

        public int div(int a, int b) {
            return a / b;
        }
    }


You might think that at this point you could simply re-compile the
server and call it.  Feel free to try it.  You will find that the client
can't find any such API. Why? Because there is a file that maps the
names by which your classes are known externally (i.e., via XMLRPC) to
the actual Java class. We need to add a mapping for our new class to
the property file:

    cd .../Test Server/resources
    vim handlers.properties

add this line:

    SimpleMath=com.divisiblebyzero.xmlrpc.model.handlers.SimpleMath

Now, we can rebuild and run our server:

    cd .../Test Server/
    ant

Start your server (i.e, click the "Start" button).

## 4. Call the new API from our client

I've modified `makeRemoteCall` to this:

    - (void)makeRemoteCall
    {
        NSURL *URL = [NSURL URLWithString: @"http://127.0.0.1:8080/"];  
        XMLRPCRequest *request = [[XMLRPCRequest alloc] initWithURL: URL];
        XMLRPCConnectionManager *manager = [XMLRPCConnectionManager sharedManager];

        NSArray *args = [NSArray arrayWithObjects:
                         [NSNumber numberWithInt:6],
                         [NSNumber numberWithInt:7],
                         nil];
        [request setMethod: @"SimpleMath.mul" withParameters: args];
        NSLog(@"Request body: %@", [request body]);
        [manager spawnConnectionWithXMLRPCRequest: request delegate: self];
        [request release];
    }

and `request:didReceiveResponse:` to this:

    - (void)request:(XMLRPCRequest *)request
    didReceiveResponse:(XMLRPCResponse *)response
    {
        NSLog(@"%s Line %d: response = %@", __PRETTY_FUNCTION__, __LINE__, response);
        
        // are we getting back a "valid" response?
        if ([response isFault])
            NSLog(@"%@ call FAILED!", [request method]);
        else
            NSLog(@"%@ call SUCCEEDED!", [request method]);
        
        NSObject *respObj = [response object];
        NSLog(@"Response body: %@", [response body]);
        
        if ([respObj isKindOfClass:[NSString class]])
            NSLog(@"Received: \"%@\" from the server!", respObj);
        else
            NSLog(@"Received: %@ from the server!", respObj);
    }

Make the changes, build and run your client. You should get output similar to this:

    2011-05-25 12:01:24.646 SimpleXMLRPCClient[801:207] Request body: <?xml version="1.0"?><methodCall><methodName>SimpleMath.mul</methodName><params><param><value><i4>6</i4></value></param><param><value><i4>7</i4></value></param></params></methodCall>
    2011-05-25 12:01:24.695 SimpleXMLRPCClient[801:207] -[SimpleXMLRPCClientAppDelegate request:didReceiveResponse:] Line 56: response = <XMLRPCResponse: 0x4b1d140>
    2011-05-25 12:01:24.696 SimpleXMLRPCClient[801:207] SimpleMath.mul call SUCCEEDED!
    2011-05-25 12:01:24.701 SimpleXMLRPCClient[801:207] Response body: <?xml version="1.0" encoding="UTF-8"?><methodResponse><params><param><value><i4>42</i4></value></param></params></methodResponse>
    2011-05-25 12:01:24.701 SimpleXMLRPCClient[801:207] Received: 42 from the server!


OK, I feel like I'm starting to get somewhere. (Hopefully you do too.)


## 5. Vector arguments and return values

Above, I essentially passed a vector argument from the client side, but
this was mapped into a method on the server side taking a fixed number
of arguments. What happens if I pass the wrong number of arguments from
the client?  What happens if I set up the parameter(s) like this:

    [request setMethod: @"SimpleMath.mul" withParameter: args];

instead of this?

    [request setMethod: @"SimpleMath.mul" withParameters: args];

(These are both left as exercizes for the reader.)

Next, we'll test a method that takes a variable number of arguments (an
array), and another, that returns an array. It was not obvious to me how
such things should be implemented or called.

It looks to me like there is no way, for example, to overload a call
with different argument types and have the proper function called in the
server.  Nor, will a declaration like this even work:

    public int vectorAdd(Integer[] intArray)
    {
        Integer sum = 0;
        for (Integer i : intArray) {
            sum += i;
        }

        return sum;
    }


Java is happy to compile it, but XMLRPC doesn't know how to call it. 
If you try, you will get back a fault response, such as this:

    2011-05-25 15:08:24.688 SimpleXMLRPCClient[1646:207] SimpleMath.vectorAdd call FAILED!
    2011-05-25 15:08:24.689 SimpleXMLRPCClient[1646:207] Response body: <?xml version="1.0" encoding="UTF-8"?><methodResponse><fault><value><struct><member><name>faultCode</name><value><i4>0</i4></value></member><member><name>faultString</name><value>No method matching arguments: [Ljava.lang.Object;</value></member></struct></value></fault></methodResponse>
    2011-05-25 15:08:24.690 SimpleXMLRPCClient[1646:207] Received: {
        faultCode = 0;
        faultString = "No method matching arguments: [Ljava.lang.Object;";
    } from the server!


Here are two methods that do work, however. Put these into
`SimpleMath.java`

    public int vectorAdd(Object[] objArray)
    {
        System.out.println("vectorAdd(Integer[])");
        Integer i;
        Integer sum = 0;
        for (Object obj : objArray) {
            i = (Integer)obj;

            // DEBUG
            //System.out.print("obj = " + obj);
            //System.out.println(" [" + obj.getClass().getName() + "]");

            sum += i;
        }

        return sum;
    }


    public List listEcho(List inputList)
    {
        ArrayList outputList = new ArrayList();
        for (Object obj : inputList) {
            outputList.add(obj);

            // DEBUG
            System.out.print("obj = " + obj);
            System.out.println(" [" + obj.getClass().getName() + "]");
        }

        return outputList;
    }


and then rebuild/relaunch your server:

    cd .../Test Server
    ant


## 6. Test the new vector functions

Here's a `makeRemoteCall` implementation you can use to call the two new methods:

    - (void)makeRemoteCall
    {
        NSURL *URL = [NSURL URLWithString: @"http://127.0.0.1:8080/"];  
        XMLRPCRequest *request = [[XMLRPCRequest alloc] initWithURL: URL];
        XMLRPCConnectionManager *manager = [XMLRPCConnectionManager sharedManager];

        NSMutableArray *args = [NSMutableArray arrayWithCapacity:7];
        for (int i = 3; i < 10; i++) {
            [args addObject:[NSNumber numberWithInt:i]];
        }
                         
        [request setMethod: @"SimpleMath.vectorAdd" withParameter: args];
        NSLog(@"Request body: %@", [request body]);
        [manager spawnConnectionWithXMLRPCRequest: request delegate: self];
        [request release];
        
        // make a second call
        request = [[XMLRPCRequest alloc] initWithURL: URL];
        [request setMethod: @"SimpleMath.listEcho" withParameter: args];
        NSLog(@"Request body: %@", [request body]);
        [manager spawnConnectionWithXMLRPCRequest: request delegate: self];
        [request release];
    }

Install that function, rebuild and run your client. You should output similar to the following:

    [Session started at 2011-05-25 15:21:37 -0600.]
    2011-05-25 15:21:39.165 SimpleXMLRPCClient[1750:207] Request body: <?xml version="1.0"?><methodCall><methodName>SimpleMath.vectorAdd</methodName><params><param><value><array><data><value><i4>3</i4></value><value><i4>4</i4></value><value><i4>5</i4></value><value><i4>6</i4></value><value><i4>7</i4></value><value><i4>8</i4></value><value><i4>9</i4></value></data></array></value></param></params></methodCall>
    2011-05-25 15:21:39.170 SimpleXMLRPCClient[1750:207] Request body: <?xml version="1.0"?><methodCall><methodName>SimpleMath.listEcho</methodName><params><param><value><array><data><value><i4>3</i4></value><value><i4>4</i4></value><value><i4>5</i4></value><value><i4>6</i4></value><value><i4>7</i4></value><value><i4>8</i4></value><value><i4>9</i4></value></data></array></value></param></params></methodCall>
    2011-05-25 15:21:39.250 SimpleXMLRPCClient[1750:207] -[SimpleXMLRPCClientAppDelegate request:didReceiveResponse:] Line 64: response = <XMLRPCResponse: 0x4b14b60>
    2011-05-25 15:21:39.251 SimpleXMLRPCClient[1750:207] SimpleMath.vectorAdd call SUCCEEDED!
    2011-05-25 15:21:39.260 SimpleXMLRPCClient[1750:207] Response body: <?xml version="1.0" encoding="UTF-8"?><methodResponse><params><param><value><i4>42</i4></value></param></params></methodResponse>
    2011-05-25 15:21:39.261 SimpleXMLRPCClient[1750:207] Received: 42 from the server!
    2011-05-25 15:21:39.262 SimpleXMLRPCClient[1750:207] -[SimpleXMLRPCClientAppDelegate request:didReceiveResponse:] Line 64: response = <XMLRPCResponse: 0x4b4d530>
    2011-05-25 15:21:39.264 SimpleXMLRPCClient[1750:207] SimpleMath.listEcho call SUCCEEDED!
    2011-05-25 15:21:39.264 SimpleXMLRPCClient[1750:207] Response body: <?xml version="1.0" encoding="UTF-8"?><methodResponse><params><param><value><array><data><value><i4>3</i4></value><value><i4>4</i4></value><value><i4>5</i4></value><value><i4>6</i4></value><value><i4>7</i4></value><value><i4>8</i4></value><value><i4>9</i4></value></data></array></value></param></params></methodResponse>
    2011-05-25 15:21:39.265 SimpleXMLRPCClient[1750:207] Received: (
        3,
        4,
        5,
        6,
        7,
        8,
        9
    ) from the server!


OK! That's the end of this tutorial.
