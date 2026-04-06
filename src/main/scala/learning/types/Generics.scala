package learning.types

/*
************************************************
*************************************************
Generics
----------------------
- Generic classes (or traits) take a type as a parameter within [...]
- The scala convention is to use a single letter (like `A`) to name
  those type parameters.
- The type can then be used inside the class as needed for method
  instance parameters, or on return types

This implementation of a Stack class takes any type as a parameter.
The beauty of generics is that you can now create a Stack[Int],
Stack[String], and so on, allowing you to reuse your implementation
of a Stack for arbitrary element types.
*/

class Stack[A]:

    private var elements: List[A] = Nil

    def push(x: A): Unit =
        elements = elements.prepended(x)

    def peek: A = elements.head

    def pop(): A =
        val currentTop = peek
        elements = elements.tail
        currentTop

def genericsExample() =
    val stack = Stack[Int]
    stack.push(1)
    stack.push(5)
    println(s"popped element ${stack.pop()} from stack") // 5
    println(s"popped element ${stack.pop()} from stack") // 1
