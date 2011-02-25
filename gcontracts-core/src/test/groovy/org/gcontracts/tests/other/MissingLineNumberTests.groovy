package org.gcontracts.tests.other

import org.gcontracts.tests.basic.BaseTestClass

/**
 * @author andre.steingress@gmail.com
 */
class MissingLineNumberTests extends BaseTestClass {

  def source = '''
package tests

import org.gcontracts.annotations.*

@Invariant({ str?.size() > 0 })
class MissingLineNumber {

  private String str = "test"

  @Requires({ param1 != null })
  def void operation1(def param1)  {
    // noop
  }

  def void operation2(def param1)  {
    str = param1
  }

}
'''

  def void test_line_number_in_precondition_stacktrace()  {

    def var = create_instance_of(source)

    try {
      var.operation1 null
    } catch (AssertionError ex)  {
      ByteArrayOutputStream output = new ByteArrayOutputStream()
      PrintWriter writer = new PrintWriter(output)

      ex.printStackTrace(writer)
      writer.flush()
      writer.close()

      String stacktrace = new String(output.toByteArray())
      String errorline = ""

      stacktrace.eachLine {
        String line ->
          if (line.contains("MissingLineNumber.operation1("))  { errorline = line }
      }

      println errorline

      assertTrue "error line must not be empty", errorline.size() > 0

      assertFalse "line number of assertion must not be missing", errorline.endsWith("(MissingLineNumber.groovy)")
    }
  }

  def void test_line_number_in_class_invariant_stacktrace()  {

    def var = create_instance_of(source)

    try {
    var.operation2 ""
    } catch (AssertionError ex)  {
      ByteArrayOutputStream output = new ByteArrayOutputStream()
      PrintWriter writer = new PrintWriter(output)

      ex.printStackTrace(writer)
      writer.flush()
      writer.close()

      String stacktrace = new String(output.toByteArray())
      String errorline = ""

      stacktrace.eachLine {
        String line ->
          if (line.contains("MissingLineNumber.operation2("))  { errorline = line }
      }

      println errorline

      assertTrue "error line must not be empty", errorline.size() > 0

      assertFalse "line number of assertion must not be missing", errorline.endsWith("(MissingLineNumber.groovy)")
    }
  }
}