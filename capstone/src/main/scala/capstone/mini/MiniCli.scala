package capstone.mini

/** Capstone 1 — **credit band** CLI: parse **credit score** + **income**, validate, then
  *  **`Approved`** or **`Declined`** with reasons (thresholds on score and income — see
  *  [[CreditInfo]]). [[InvalidInput]] / [[Either]] for bad input; full write-up in repo
  *  `capstone/README.md` §1.
  *
  *  Run from the **repo root** (`scala-tutorial` project — `capstone/` sources are compiled in):
  *
  *    - Interactive: `sbt "runMain capstone.mini.MiniCli"`
  *    - One string arg: `sbt 'runMain capstone.mini.MiniCli "500 10000"'`
  */
sealed trait AppError
case class InvalidInput(msg: String) extends AppError

/** Only built through [[CreditInfo.apply]] after validation (private ctor). */
case class CreditInfo private (creditScore: Int, income: Double)

sealed trait Decision:
  def name: String
  def reason: String

case class Approved(name: String = "Approved", reason: String = "NA")
    extends Decision
case class Declined(name: String = "Declined", reason: String = "NA")
    extends Decision

object CreditInfo:
  final val CREDIT_MIN_THRESHOLD = 450
  final val INCOME_MIN_THRESHOLD = 7500
  /** Smart constructor. Inside the companion, `CreditInfo(cs, in)` would call
    * *this* `apply` again (recursive `Either`). After validation, build the
    * value with `new` so you get a plain [[CreditInfo]], not a nested `Either`.
    */
  def apply(
      creditScore: Int,
      income: Double
  ): Either[InvalidInput, CreditInfo] =
    for
      cs <- validateScore(creditScore)
      in <- validateIncome(income)
    yield new CreditInfo(cs, in)

  def validateScore(creditScore: Int): Either[InvalidInput, Int] =
    if (creditScore <= 900 && creditScore >= 250) then Right(creditScore)
    else
      Left(InvalidInput("FICO scores need to be between 250 and 900 inclusive"))

  def validateIncome(income: Double): Either[InvalidInput, Double] =
    if (income >= 0) then Right(income)
    else Left(InvalidInput("Negative income not allowed"))

  def makeDecision(creditInfo: CreditInfo): Decision =
    if creditInfo.creditScore < CREDIT_MIN_THRESHOLD then
      Declined(reason = s"Credit below threshold $CREDIT_MIN_THRESHOLD")
    else if creditInfo.income < INCOME_MIN_THRESHOLD then
      Declined(reason = s"Income below threshold $INCOME_MIN_THRESHOLD")
    else Approved(reason = "Above threshold")


@main def MiniCli(creditIncomeString: String = ""): Unit =
  val line = 
    if creditIncomeString.isEmpty then getInput() 
    else creditIncomeString.trim()

  val creditInfo = parseCreditInfo(line)
  creditInfo match
    case Left(err) =>
      println(s"Got error: ${err.msg}")
    case Right(ci @ CreditInfo(credit, income)) =>
      println(s"credit score: $credit, your income: ${'$'}$income")
      val decision = CreditInfo.makeDecision(ci)
      println(s"${decision.name}: ${decision.reason}")

def getInput(): String = 
  println("Input your credit_score and income in $ (separated by space or ,)")
  scala.io.StdIn.readLine().trim

def parseCreditInfo(line: String): Either[InvalidInput, CreditInfo] =
  // Empty line → `"".split(...)` becomes `Array("")` (length 1),
  // so handle explicitly for a clearer error.
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
        info <- CreditInfo(cs, in)
      yield info
