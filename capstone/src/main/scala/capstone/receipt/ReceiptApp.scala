package capstone.receipt

/** Capstone 1 — boss project (see `capstone/README.md` — Boss project).
  *
  * Run: `sbt "runMain capstone.receipt.ReceiptApp capstone/samples/receipt-good.txt"`
  */
@main def ReceiptApp(args: String*): Unit =
  println(
    "capstone.receipt — implement parser per capstone/README.md (Boss project)."
  )
  if args.isEmpty then
    println("Usage: pass path to a receipt .txt file as first argument.")
