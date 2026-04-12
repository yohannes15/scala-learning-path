package capstone.receipt

import geny.Generator

sealed trait AppError
case class LineError(msg: String = "error processing line") extends AppError
case class FileError(msg: String = "error reading file") extends AppError

enum TaxCode:
  case EXEMPT, STANDARD, REDUCED

case class Receipt(description: String, taxCode: TaxCode, price: BigDecimal)

/** Capstone 1 — boss project (see `capstone/README.md` — Boss project).
  *
  * Run: `sbt "runMain capstone.receipt.ReceiptApp capstone/samples/receipt-good.txt"`
  */
@main def ReceiptApp(textFile: String): Unit =
  println("==================== ReceiptApp =================")
  getFileStream(textFile) match
    case Left(err) => println(s"error: $err.msg")
    // Generator is lazy: effects (println) only run when the stream is traversed — use foreach, not map alone.
    case Right(g) => parseReceipt(g)

def parseReceipt(stream: Generator[String]) = 
  stream.zipWithIndex.foreach { (line, index) =>
    parseReceiptLine(line, index + 1) match
      case Right(receipt) =>
        println(
          s"description: ${receipt.description}. " +
            s"taxcode: ${receipt.taxCode}. " +
            s"price: ${receipt.price}"
        )
      case Left(err) => println(s"error: ${err.msg}")
  }

def parseReceiptLine(line: String, line_no: Int): Either[LineError, Receipt] =
  // `split` takes a regex; `|` is special — escape for a literal pipe.
  val lineTokens = line.split("\\|")
  if lineTokens.length != 3 then
    Left(LineError(s"Expected format {description}|{taxCode}|{price}. Got: $line"))
  else
    val description = lineTokens(0).trim()
    val taxCode = TaxCode.valueOf(lineTokens(1).trim().capitalize)
    lineTokens(2).trim().toDoubleOption match
      case Some(value) => Right(Receipt(description, taxCode, BigDecimal.decimal(value)))
      case _ => Left(LineError(s"Invalid price value on $line_no. "))

def getFileStream(textFile: String): Either[FileError, Generator[String]] = 
  var samplesDir = os.pwd /  "capstone" / "samples"
  // return a generator
  if !os.exists(samplesDir / textFile) then
    Left(FileError(s"Non existent file: $textFile in path $samplesDir"))
  else
    Right(
      os.read.lines.stream(samplesDir / textFile)
      .map(_.trim).dropWhile(x => x.isEmpty())
    )
