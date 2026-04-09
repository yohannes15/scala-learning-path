/* 
A worksheet is a Scala file that is evaluated on save, and the result of
each expression is show in a column to right of your program.

REPL sessions on steriods is another description for a worksheet. They
enjoy 1st class ide support.

The extension is .worksheet.sc. Like this one. You can see the result
of the evaluation of every line on the right as a comment.

Also note that worksheets don’t have a program entry point. Instead, 
top-level statements and expressions are evaluated from top to bottom.
*/

println("Hello, world!")
   
val x = 1
x + x

2
