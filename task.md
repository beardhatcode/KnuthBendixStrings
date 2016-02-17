Task: Knuth-Bendix for strings
==============================

Write a Java program that computes the size of a group given by a presentation,
using the methods explained in section 1.2 of the course notes (and during the lectures).

Use the *shortlex* termination order: first compare the length of two words 
and in case of equal length, use lexicographic order.

Develop the program in subsequent steps:

* Apply a rewrite rule to a word
* Reduce a word until it is in normal form
* Compute critical pairs(s) from two rules
* Compute a *complete* set of rules from a given set
* Compute all possible normal forms for the complete set of rules
* Improve the speed of the program: remove rules that can be obtained from newer rules, use heuristics
  for chosing the next critical pair, …

The program should return a result in reasonable time for groups with less than 1000 elements. You should
test it on the examples provided: [example-10.txt](example-10.txt) of size 10,
[example-12.txt](example-12.txt) of size 12
and [example-testcase.txt](example-testcase.txt) (compare the size you obtain 
with the results of your colleagues).

Specification
-------------
Your code should be based on the [source code provided](src). Use the class `Main` 
as main program class, and only change the `sizeOfGroup` method. Leave the class `Parser` unchanged
but read its documentation to understand the structure of the parameter which is 
handed to `sizeOfGroup`.

Develop your own representation of group elements and/or products. Do *not* use `Parser.Result` 
or `Parser.Element` (these will be too slow). Your first step will therefore probably consist
of converting the argument to `sizeOfGroup` into your own data structure.

**Note**: the parser also allows inverses of group elements in the input files. You need however
 not support this feature. 
 Your program will not be tested on input files that contain inverses or
 negative powers. The `Parser.Element`-objects provided to you 
 will always have `inverted` equal to `false`.
 



