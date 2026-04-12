package capstone.receipt

import geny.Generator
import scala.util.Try
import scala.util.Failure
import scala.util.Success

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

def processReceipt(receipt: Receipt): Unit = 
  println(
    s"description: ${receipt.description}. " +
    s"taxcode: ${receipt.taxCode}. " +
    s"price: ${receipt.price}"
  )

def parseReceipt(stream: Generator[String]): Unit = 
  stream.zipWithIndex.foreach { (line, index) =>
    parseLine(line, index + 1) match
      case Right(receipt) => processReceipt(receipt)
      case Left(err) => println(s"error: ${err.msg}")
  }

def parseLine(line: String, line_no: Int): Either[LineError, Receipt] =
  // `split` takes a regex; `|` is special — escape for a literal pipe.
  val lineTokens = line.split("\\|")
  print(s"$line_no: ")
  if lineTokens.length != 3 then
    Left(LineError(s"Expected format {description}|{taxCode}|{price}. Got: $line"))
  else 
    for
      description <- parseDescription(lineTokens(0))
      taxCode <- parseTaxCode(lineTokens(1))
      price <- parsePrice(lineTokens(2))
    yield
      Receipt(description, taxCode, price)

def parseTaxCode(taxCode: String): Either[LineError, TaxCode] = 
  Try { TaxCode.valueOf(taxCode.trim().capitalize) } match
    case Failure(exception) => Left(LineError(s"Error parsing tax code: ${exception.toString}"))
    case Success(code) => Right(code)
  
def parseDescription(description: String): Either[LineError, String] = 
  val desc = description.trim
  if desc.length > 40 then 
    Left(LineError("description can't be more than 40 characters"))
  else
    Right(desc)

def parsePrice(price: String): Either[LineError, BigDecimal] = 
  price.trim().toDoubleOption match
    case Some(value) => Right(BigDecimal.decimal(value))
    case _ => Left(LineError(s"Unable to parse price. Got: $price"))

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
