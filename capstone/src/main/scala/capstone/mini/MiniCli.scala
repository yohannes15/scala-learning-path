package capstone.mini

/** Capstone 1 — tiny CLI (see `capstone/README.md` §1).
  *
  * Replace the body with your project fake “credit band” (score + income →
  * Approved / Declined with reason). Run: `sbt "runMain capstone.mini.MiniCli"`
  */

sealed trait AppError

case class InvalidInput(msg: String) extends AppError

case class CreditInfo(creditScore: Int, income: Double)

@main def MiniCli(args: String*): Unit =
  println("Input your credit_score and income in $ (separated by space or ,)")
  val creditInfo = parseCreditInfo()
  creditInfo match
    case Left(err) =>
      println(s"Got error: ${err.msg}")
    case Right(CreditInfo(credit, income)) =>
      println(s"Your credit score $credit, your income $income")

def parseCreditInfo(): Either[InvalidInput, CreditInfo] =
  val line = scala.io.StdIn.readLine().trim
  // Empty line → `"".split(...)` becomes `Array("")` (length 1), so handle explicitly for a clearer error.
  if line.isEmpty then
    Left(InvalidInput("No input; enter credit score and income (two values)."))
  else
    val tokens: Array[String] = line.split("[,\\s]+")
    if tokens.length != 2 then
      Left(
        InvalidInput(
          s"invalid input. found ${tokens.length} inputs. Only 2 allowed"
        )
      )
    else
      val credit_score = tokens(0).toIntOption match
        case Some(value) => Right(value)
        case _           => Left(InvalidInput("Unable to parse credit_score"))

      val income = tokens(1).toDoubleOption match
        case Some(value) => Right(value)
        case _           => Left(InvalidInput("Unable to parse income"))

      for
        cs <- credit_score
        in <- income
      yield CreditInfo(creditScore = cs, income = in)
