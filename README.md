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

The window size (also known as the order of a Markov Chain) determines the number of tokens in the prefix which are 
examined for the search of an existing suffix. The mapping from prefix-&gt;suffix is called a dictionary. 

While a window size of one suffices for a small text base the textual stringency rises
with the window size because more prefix tokens are taken into consideration. And while this CAN lead to a better
textual stringency it also means that the word histogram MAY look totally different in terms of probable suffix
selection. Exactly one suffix for a prefix has a general probability of p=1.0 for selection which in turn leads to
a very high probability to re-generate already existing sentences.

Given these sentences the window size the significance gets a bit clearer when you look at the two examples below.

### Example for a window size of 2

prefix (window size=1)|suffixes
------|--------
hello | \[world, again, my\]
world | \[\]
again | \[\]
my | \[old, little\]
little | \[pony\]
old | \[friend\]
pony | \[\]
friend | \[\]

### Example for a window size of 2

prefix (window size=1)|suffixes
------|--------
hello world | \[\]
hello again | \[\]
hello my | \[old\]
my little | \[pony\]
my old | \[friend\]
little pony | \[\]
old friend | \[\]

