# TweetkovChain - the tweet Markov-chain text generator

Convert your [tweets](https://www.twitter.com) to awesome fun text.

1. retrieve your tweet from twitter (via the settings dialog)
2. extract them to a local directory 
3. do like the class `TweetkovRunner` does

## General process
1. map tweets from JSON to objects
2. get tweet text from objects
3. create dictionary with a window sized
4. generate funny sentences

## What is a window size? 

Example for a window size of 2

prefix (n=2)|suffixes
------|--------
hello world | \[\]
hello again | \[\]
hello my | \[old\]
my little | \[pony\]
my old | \[friend\]
little pony | \[\]
old friend | \[\]

