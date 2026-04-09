package math

import org.scalatest.funsuite.AnyFunSuite 

/* 
This code demonstrates the ScalaTest `AnyFunSuite` approach. 
A few important points:

    - Your test class should extend AnyFunSuite
    - You create tests as shown, by giving each test a unique name
    - At the end of each test you should call assert to test that a condition
      has been satisfied
    - When you know you want to write a test, but you don’t want to write it 
      right now, create the test as “pending,” with the syntax shown

Using ScalaTest like this is similar to JUnit, so if you’re coming to Scala from Java,
this looks similar. Now you can run these tests with the sbt test command.
 */

class MathUtilsTests extends AnyFunSuite:

    test("'double' should handle zero") {
        val result = MathUtils.double(0)
        assert(result == 0)
    }

    test("'double' should handle 1") {
        val result = MathUtils.double(1)
        assert(result == 2)
    }

    test("test with Int.MaxValue") (pending)

end MathUtilsTests
