# Description
In this project, we will be simulating the physical layer of communication. To do this, I will have a server
running that will send “signals” to you by sending numbers in the range [0, 255] (i.e., unsigned bytes) and
you need to implement the client that properly interprets them.

The numbers will fluctuate but all be either slightly higher or slightly lower than some average. The average
value will be randomly chosen each time you connect. This means to send 10101 directly, It might look like
this one time: 65, 43, 66, 40, 71 and the next time it might be 181, 167, 188, 160, 179. However, to add
another layer of complication I will also be implementing NRZI on top of the actual bits being sent.

When you first connect, the server will send a preamble of 64 alternating high and low signals so that you
can establish the baseline. After this point, it will send you 32 bytes of randomly generated data encoded
using 4B/5B with NRZI. You need to properly decode this data and record the 32 bytes sent to you.

Once you have figured out the 32 bytes I sent, you must reply it back to me directly, don’t use any encoding.
Just send me the array of 32 bytes. Once I receive the response, I will send a final byte (not encoded) that
is either 0 or 1. If it is 0, I didn’t receive the correct 32 bytes. If it’s 1, then I did and your program worked
correctly