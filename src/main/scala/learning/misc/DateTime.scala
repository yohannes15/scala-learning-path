import java.sql.Timestamp
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}

/** DateTimeFormatterBuilder allows us to define optional parts of a string.
  * OffsetDateTime.parse is also more flexible than Instant.parse
  */
def toTimeStamp(s: String): Timestamp =
  // Define a flexible formatter that handles ISO dates with optional time offsets (Z, +01:00, etc.)
  val formatter = new DateTimeFormatterBuilder()
    .append(
      DateTimeFormatter.ISO_LOCAL_DATE_TIME
    ) // Parses the date and time part
    .optionalStart()
    .appendOffsetId() // Handles the 'Z' or offset if present
    .optionalEnd()
    .toFormatter()

  // Parse using the custom formatter and convert to a java.sql.Timestamp
  // Note: OffsetDateTime is used here instead of Instant because it is more 
  // forgiving with varying ISO-8601 formats found in real-world logs.
  Timestamp.from(java.time.OffsetDateTime.parse(s, formatter).toInstant)
