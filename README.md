# Data Prep

![cm-available](https://cdap-users.herokuapp.com/assets/cm-available.svg)
![cdap-transform](https://cdap-users.herokuapp.com/assets/cdap-transform.svg)
[![Build Status](https://travis-ci.org/cdapio/hydrator-plugins.svg?branch=develop)](https://travis-ci.org/cdapio/hydrator-plugins)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/11434/badge.svg)](https://scan.coverity.com/projects/hydrator-wrangler-transform)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cdap.wrangler/wrangler-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cdap.wrangler/wrangler-core)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/io.cdap.wrangler/wrangler-core/badge.svg)](http://www.javadoc.io/doc/io.cdap.wrangler/wrangler-core)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Join CDAP community](https://cdap-users.herokuapp.com/badge.svg?t=wrangler)](https://cdap-users.herokuapp.com?t=1)

A collection of libraries, a pipeline plugin, and a CDAP service for performing data cleansing, transformation, and filtering using a set of data manipulation instructions (directives). These instructions are either generated using an interactive visual tool or are manually created.

- Data Prep defines a few concepts that might be useful if you are just getting started. Learn about them [here](wrangler-docs/concepts.md)
- The Data Prep Transform is [separately documented](wrangler-transform/wrangler-docs/data-prep-transform.md).
- [Data Prep Cheatsheet](wrangler-docs/cheatsheet.md)

## Feature Enhancements

More [here](wrangler-docs/upcoming-features.md) on upcoming features.

- **User Defined Directives (UDD)** allow you to create custom functions to transform records within CDAP DataPrep. CDAP comes with a comprehensive library of functions. However, for some specific use cases, UDDs are ideal. Learn how to build custom directives [here](wrangler-docs/custom-directive.md).
  - Migrating directives from version 1.0 to 2.0 [here](wrangler-docs/directive-migration.md)
  - Grammar information [here](wrangler-docs/grammar/grammar-info.md)
  - `TokenType` reference [here](../api/src/main/java/io/cdap/wrangler/api/parser/TokenType.java)
  - Custom Directive Implementation Internals [here](wrangler-docs/udd-internal.md)

- CDAP Administrators can now **restrict the directives** that are accessible to users. Configuration instructions are [here](wrangler-docs/exclusion-and-aliasing.md)

- **Byte Size and Time Duration Parsing:**
  - **Byte Sizes:** Values like `"10KB"`, `"1.5MB"`, `"512b"`, `"2GiB"` can be parsed by directives expecting byte size arguments. Units (B, KB, MB, GB, TB, PB) are case-insensitive and converted to canonical bytes using 1024-based units (KiB, MiB, etc.).
  - **Time Durations:** Values like `"500ms"`, `"2.5s"`, `"100us"`, `"2h"`, `"3min"` are supported. Units (ns, us, ms, s, min, h) are case-insensitive and converted to canonical nanoseconds.

## Demo Videos and Recipes

Short screencasts and videos demonstrating features of Data Prep. More videos [here](https://www.youtube.com/playlist?list=PLhmsf-NvXKJn-neqefOrcl4n7zU4TWmIr)

### Videos

- [Creating Lookup Dataset and Joining](https://www.youtube.com/watch?v=Nc1b0rsELHQ)
- [Restricted Directives](https://www.youtube.com/watch?v=71EcMQU714U)
- [Parse Excel files in CDAP](https://www.youtube.com/watch?v=su5L1noGlEk)
- [Parse File As AVRO File](https://www.youtube.com/watch?v=tmwAw4dKUNc)
- [Parsing Binary Coded AVRO Messages](https://www.youtube.com/watch?v=Ix_lPo-PDJY)
- [Parsing AVRO & Protobuf with Schema Registry](https://www.youtube.com/watch?v=LVLIdWnUX1k)
- [Quantize a column - Digitize](https://www.youtube.com/watch?v=VczkYX5SRtY)
- [Data Cleansing with send-to-error](https://www.youtube.com/watch?v=aZd5H8hIjDc)
- [Building Data Prep from GitHub source](https://youtu.be/pGGjKU04Y38)
- [End-to-End Demo Video](https://youtu.be/AnhF0qRmn24)
- [Ingesting into Kudu](https://www.youtube.com/watch?v=KBW7a38vlUM)
- [Realtime HL7 CCDA XML into Parquet](https://youtu.be/0fqNmnOnD-0)
- [Parsing JSON file](https://youtu.be/vwnctcGDflE)
- [Flattening arrays](https://youtu.be/SemHxgBYIsY)
- [Data cleansing with send-to-error directive](https://www.youtube.com/watch?v=aZd5H8hIjDc)
- [Publishing to Kafka](https://www.youtube.com/watch?v=xdc8pvvlI48)
- [Fixed length to JSON](https://www.youtube.com/watch?v=3AXu4m1swuM)

### Recipes

- [Parsing Apache Log Files](wrangler-demos/parsing-apache-log-files.md)
- [Parsing CSV Files and Extracting Column Values](wrangler-demos/parsing-csv-extracting-column-values.md)
- [Parsing HL7 CCDA XML Files](wrangler-demos/parsing-hl7-ccda-xml-files.md)

## Available Directives

These directives are currently available:

| Directive | Description |
|----------|-------------|
| **Parsers** | |
| [JSON Path](wrangler-docs/directives/json-path.md) | Uses a DSL (JSON path) to parse JSON records |
| [Parse as AVRO](wrangler-docs/directives/parse-as-avro.md) | Parses AVRO messages (binary or JSON) |
| [Parse as AVRO File](wrangler-docs/directives/parse-as-avro-file.md) | Parses AVRO data files |
| [Parse as CSV](wrangler-docs/directives/parse-as-csv.md) | Parses CSV records |
| [Parse as Date](wrangler-docs/directives/parse-as-date.md) | Parses dates using NLP |
| [Parse as Excel](wrangler-docs/directives/parse-as-excel.md) | Parses Excel files |
| [Parse as Fixed Length](wrangler-docs/directives/parse-as-fixed-length.md) | Parses fixed-width records |
| [Parse as HL7](wrangler-docs/directives/parse-as-hl7.md) | Parses HL7 V2 messages |
| [Parse as JSON](wrangler-docs/directives/parse-as-json.md) | Parses JSON objects |
| [Parse as Log](wrangler-docs/directives/parse-as-log.md) | Parses HTTPD/nginx logs |
| [Parse as Protobuf](wrangler-docs/directives/parse-as-log.md) | Parses Protobuf messages using descriptor |
| [Parse as Simple Date](wrangler-docs/directives/parse-as-simple-date.md) | Parses simple date strings |
| [Parse XML To JSON](wrangler-docs/directives/parse-xml-to-json.md) | Parses XML to JSON |
| [Parse as Currency](wrangler-docs/directives/parse-as-currency.md) | Parses currency strings to numbers |
| [Parse as Datetime](wrangler-docs/directives/parse-as-datetime.md) | Parses datetime strings |

| **Output Formatters** | |
| [Write as CSV](wrangler-docs/directives/write-as-csv.md) | Converts record to CSV |
| [Write as JSON](wrangler-docs/directives/write-as-json-map.md) | Converts record to JSON map |
| [Write JSON Object](wrangler-docs/directives/write-as-json-object.md) | Builds JSON object from fields |
| [Format as Currency](wrangler-docs/directives/format-as-currency.md) | Formats number as currency |

| **Transformations** | |
| [Changing Case](wrangler-docs/directives/changing-case.md) | Changes string case |
| [Cut Character](wrangler-docs/directives/cut-character.md) | Extracts part of string |
| [Set Column](wrangler-docs/directives/set-column.md) | Sets column using expression |
| [Find and Replace](wrangler-docs/directives/find-and-replace.md) | Replaces substrings via regex |
| [Index Split](wrangler-docs/directives/index-split.md) | (_Deprecated_) |
| [Invoke HTTP](wrangler-docs/directives/invoke-http.md) | Calls external HTTP service |
| [Quantization](wrangler-docs/directives/quantize.md) | Quantizes column values |
| [Regex Group Extractor](wrangler-docs/directives/extract-regex-groups.md) | Extracts regex groups |
| [Setting Character Set](wrangler-docs/directives/set-charset.md) | Converts encoding to UTF-8 |
| [Setting Record Delimiter](wrangler-docs/directives/set-record-delim.md) | Sets record delimiter |
| [Split by Separator](wrangler-docs/directives/split-by-separator.md) | Splits column into two |
| [Split Email Address](wrangler-docs/directives/split-email.md) | Splits email into parts |
| [Split URL](wrangler-docs/directives/split-url.md) | Splits URL components |
| [Text Distance](wrangler-docs/directives/text-distance.md) | Fuzzy match (Levenshtein, etc.) |
| [Text Metric](wrangler-docs/directives/text-metric.md) | Fuzzy similarity metric |
| [URL Decode](wrangler-docs/directives/url-decode.md)                            | Decodes from the `application/x-www-form-urlencoded` MIME format |
| [URL Encode](wrangler-docs/directives/url-encode.md)                            | Encodes to the `application/x-www-form-urlencoded` MIME format   |
| [Trim](wrangler-docs/directives/trim.md)                                        | Functions for trimming white spaces around string data           |
| **Encoders and Decoders**                                              |                                                                  |
| [Decode](wrangler-docs/directives/decode.md)                                    | Decodes a column value as one of `base32`, `base64`, or `hex`    |
| [Encode](wrangler-docs/directives/encode.md)                                    | Encodes a column value as one of `base32`, `base64`, or `hex`    |
| **Unique ID**                                                          |                                                                  |
| [UUID Generation](wrangler-docs/directives/generate-uuid.md)                    | Generates a universally unique identifier (UUID) .Recommended to use with Wrangler version 4.4.0 and above due to an important bug fix [CDAP-17732](https://cdap.atlassian.net/browse/CDAP-17732)             |
| **Date Transformations**                                               |                                                                  |
| [Diff Date](wrangler-docs/directives/diff-date.md)                              | Calculates the difference between two dates                      |
| [Format Date](wrangler-docs/directives/format-date.md)                          | Custom patterns for date-time formatting                         |
| [Format Unix Timestamp](wrangler-docs/directives/format-unix-timestamp.md)      | Formats a UNIX timestamp as a date                               |
| **DateTime Transformations**                                                    |                                                                  |
| [Current DateTime](wrangler-docs/directives/current-datetime.md)                | Generates the current datetime using the given zone or UTC by default|
| [Datetime To Timestamp](wrangler-docs/directives/datetime-to-timestamp.md)      | Converts a datetime value to timestamp with the given zone       |
| [Format Datetime](wrangler-docs/directives/format-datetime.md)                  | Formats a datetime value to custom date time pattern strings     |
| [Timestamp To Datetime](wrangler-docs/directives/timestamp-to-datetime.md)      | Converts a timestamp value to datetime                           |
| **Lookups**                                                            |                                                                  |
| [Catalog Lookup](wrangler-docs/directives/catalog-lookup.md)                    | Static catalog lookup of ICD-9, ICD-10-2016, ICD-10-2017 codes   |
| [Table Lookup](wrangler-docs/directives/table-lookup.md)                        | Performs lookups into Table datasets                             |
| **Hashing & Masking**                                                  |                                                                  |
| [Message Digest or Hash](wrangler-docs/directives/hash.md)                      | Generates a message digest                                       |
| [Mask Number](wrangler-docs/directives/mask-number.md)                          | Applies substitution masking on the column values                |
| [Mask Shuffle](wrangler-docs/directives/mask-shuffle.md)                      | Applies shuffle masking on the column values                     |
| **Row Operations**                                                     |                                                                  |
| [Filter Row if Matched](wrangler-docs/directives/filter-row-if-matched.md)      | Filters rows that match a pattern for a column                                         |
| [Filter Row if True](wrangler-docs/directives/filter-row-if-true.md)            | Filters rows if the condition is true.                                                  |
| [Filter Row Empty of Null](wrangler-docs/directives/filter-empty-or-null.md)    | Filters rows that are empty of null.                    |
| [Flatten](wrangler-docs/directives/flatten.md)                                  | Separates the elements in a repeated field                       |
| [Fail on condition](wrangler-docs/directives/fail.md)                           | Fails processing when the condition is evaluated to true.        |
| [Send to Error](wrangler-docs/directives/send-to-error.md)                      | Filtering of records to an error collector                       |
| [Send to Error And Continue](wrangler-docs/directives/send-to-error-and-continue.md) | Filtering of records to an error collector and continues processing                      |
| [Split to Rows](wrangler-docs/directives/split-to-rows.md)                      | Splits based on a separator into multiple records                |
| **Column Operations**                                                  |                                                                  |
| [Change Column Case](wrangler-docs/directives/change-column-case.md)            | Changes column names to either lowercase or uppercase            |
| [Changing Case](wrangler-docs/directives/changing-case.md)                      | Change the case of column values                                 |
| [Cleanse Column Names](wrangler-docs/directives/cleanse-column-names.md)        | Sanatizes column names, following specific rules                 |
| [Columns Replace](wrangler-docs/directives/columns-replace.md)                  | Alters column names in bulk                                      |
| [Copy](wrangler-docs/directives/copy.md)                                        | Copies values from a source column into a destination column     |
| [Drop Column](wrangler-docs/directives/drop.md)                                 | Drops a column in a record                                       |
| [Fill Null or Empty Columns](wrangler-docs/directives/fill-null-or-empty.md)    | Fills column value with a fixed value if null or empty           |
| [Keep Columns](wrangler-docs/directives/keep.md)                                | Keeps specified columns from the record                          |
| [Merge Columns](wrangler-docs/directives/merge.md)                              | Merges two columns by inserting a third column                   |
| [Rename Column](wrangler-docs/directives/rename.md)                             | Renames an existing column in the record                         |
| [Set Column Header](wrangler-docs/directives/set-headers.md)                     | Sets the names of columns, in the order they are specified       |
| [Split to Columns](wrangler-docs/directives/split-to-columns.md)                | Splits a column based on a separator into multiple columns       |
| [Swap Columns](wrangler-docs/directives/swap.md)                                | Swaps column names of two columns                                |
| [Set Column Data Type](wrangler-docs/directives/set-type.md)                    | Convert data type of a column                                    |
| **NLP**                                                                |                                                                  |
| [Stemming Tokenized Words](wrangler-docs/directives/stemming.md)                | Applies the Porter stemmer algorithm for English words           |
| **Transient Aggregators & Setters**                                    |                                                                  |
| [Increment Variable](wrangler-docs/directives/increment-variable.md)            | Increments a transient variable with a record of processing.     |
| [Set Variable](wrangler-docs/directives/set-variable.md)                        | Sets a transient variable with a record of processing.     |
| `aggregate-stats`                                                      | Aggregates byte size and time duration columns, calculating total bytes (Long) and average nanoseconds (Double). Outputs a single summary row. |
| **Functions**                                                          |                                                                  |
| [Data Quality](wrangler-docs/functions/dq-functions.md)                         | Data quality check functions. Checks for date, time, etc.        |
| [Date Manipulations](wrangler-docs/functions/date-functions.md)                 | Functions that can manipulate date                               |
| [DDL](wrangler-docs/functions/ddl-functions.md)                                 | Functions that can manipulate definition of data                 |
| [JSON](wrangler-docs/functions/json-functions.md)                               | Functions that can be useful in transforming your data           |
| [Types](wrangler-docs/functions/type-functions.md)                              | Functions for detecting the type of data                         |

## Directive Details

### aggregate-stats

Calculates aggregate statistics for byte size and time duration columns across all input rows.

**Syntax:**

```wrangler
aggregate-stats <size-column> <time-column> <total-bytes-column> <avg-nanos-column>;

Arguments
<size-column> (ColumnName):
The input column containing byte size values (e.g., "10KB", "1.5MB").
⚠️ Note: Use simple or generic column names (e.g., :data, :col1) rather than names like :size due to parser limitations.

<time-column> (ColumnName):
The input column containing time duration values (e.g., "500ms", "2.5s").
⚠️ Note: Same naming recommendations as <size-column>.

<total-bytes-column> (ColumnName):
The name of the output column that will contain the sum of all valid byte sizes, represented as a Long (total bytes).

<avg-nanos-column> (ColumnName):
The name of the output column that will contain the average of all valid time durations, represented as a Double (average nanoseconds).

Output
A single row containing:

The specified target columns:

total-bytes-column (Long)

avg-nanos-column (Double)

aggregate_count (Long): total number of input rows processed

⚠️ Rows with null or unparseable values in the source columns are skipped for the respective aggregation but included in the overall count.

Example
Assume input rows have columns bytes_in (e.g., "1MB") and latency (e.g., "150ms"):

wrangler
Copy
Edit
aggregate-stats :bytes_in :latency :total_transfer_bytes :average_latency_ns;
