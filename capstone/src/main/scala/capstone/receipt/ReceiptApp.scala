package capstone.receipt

import geny.Generator

enum TaxCode:
  case EXEMPT, STANDARD, REDUCED

case class Receipt(description: String, taxCode: TaxCode, price: BigDecimal)

/** Capstone 1 — boss project (see `capstone/README.md` — Boss project).
  *
  * Run: `sbt "runMain capstone.receipt.ReceiptApp capstone/samples/receipt-good.txt"`
  */
@main def ReceiptApp(text_file: String): Unit =
  val stream: Generator[String] = getFileStream(text_file)
  // Generator is lazy: effects (println) only run when the stream is traversed — use foreach, not map alone.
  stream.zipWithIndex.foreach { (line, index) =>
    parseReceipt(line, index + 1) match
      case Right(receipt) =>
        println(
          s"description: ${receipt.description}. " +
            s"taxcode: ${receipt.taxCode}. " +
            s"price: ${receipt.price}"
        )
      case Left(err) => println(s"error: $err")
  }
  

def parseReceipt(line: String, line_no: Int): Either[String, Receipt] =
  // `split` takes a regex; `|` is special — escape for a literal pipe.
  val lineTokens = line.split("\\|")
  if lineTokens.length != 3 then
    Left(s"Expected format {description}|{taxCode}|{price}. Got: $line")
  else
    val description = lineTokens(0).trim()
    val taxCode = TaxCode.valueOf(lineTokens(1).trim().capitalize)
    lineTokens(2).trim().toDoubleOption match
      case Some(value) => Right(Receipt(description, taxCode, BigDecimal.decimal(value)))
      case _ => Left(s"Invalid price value on $line_no. ")

def getFileStream(text_file: String): Generator[String] = 
  var samplesDir = os.pwd /  "capstone" / "samples"
  // return a generator
  os.read.lines.stream(samplesDir / text_file).map(_.trim).dropWhile(x => x.isEmpty())
