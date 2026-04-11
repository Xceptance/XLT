## ADDED Requirements

### Requirement: ByteCsvDecoder parses CSV fields from byte arrays
A new `ByteCsvDecoder` class SHALL parse CSV lines directly from `byte[]` arrays without first converting to `char[]` or `String`. It SHALL handle field splitting, comma delimiters, and quoted fields (`""` escaping) at the byte level.

#### Scenario: Simple unquoted line
- **WHEN** `ByteCsvDecoder` receives the bytes for `"R,TName,1234567890,RName,200,1523,442,0,12345,https://example.com"`
- **THEN** it SHALL split the line into the correct fields without creating intermediate `String` or `char[]` objects for numeric fields

#### Scenario: Quoted field with embedded comma
- **WHEN** `ByteCsvDecoder` receives a field value `"\"value,with,commas\""` (byte representation)
- **THEN** it SHALL correctly parse the full quoted value as a single field

#### Scenario: Empty field handling
- **WHEN** `ByteCsvDecoder` encounters consecutive commas `,,` in the byte stream
- **THEN** it SHALL produce an empty field value at that position

### Requirement: Numeric fields parsed directly from bytes
Numeric CSV fields (timestamps, runtimes, response codes, byte counts) SHALL be parsed directly from their ASCII byte representation using `value = value * 10 + (b - '0')` arithmetic. No char or String conversion SHALL occur for purely numeric fields.

#### Scenario: Parsing a timestamp field
- **WHEN** `ByteCsvDecoder` encounters the bytes `[0x31, 0x36, 0x31, 0x38, 0x39, 0x32, 0x33, 0x34, 0x35, 0x36]` (ASCII for "1618923456")
- **THEN** it SHALL return the `long` value `1618923456` without allocating a `String`

#### Scenario: Parsing a zero-value field
- **WHEN** `ByteCsvDecoder` encounters the byte `[0x30]` (ASCII for "0")
- **THEN** it SHALL return the `long` value `0`

### Requirement: String fields convert to XltCharBuffer on demand
Non-numeric fields (names, URLs, error messages) SHALL be converted to `XltCharBuffer` when accessed. The `setBaseValues()` and `setRemainingValues()` methods on `Data` subclasses SHALL continue to accept `XltCharBuffer` parameters — the `Data` model API SHALL NOT change.

#### Scenario: URL field accessed as XltCharBuffer
- **WHEN** a `RequestData.setRemainingValues()` call needs the URL field
- **THEN** the byte-level URL field SHALL be converted to an `XltCharBuffer` and passed to the existing API

### Requirement: DataReaderThread emits byte buffers
`DataReaderThread` SHALL read timer files using `FileInputStream` (or buffered byte stream) and emit `byte[]` line buffers. It SHALL NOT use `InputStreamReader` for the primary data path.

#### Scenario: Reading a timer file
- **WHEN** `DataReaderThread` reads a timer CSV file from disk
- **THEN** it SHALL deliver `byte[]` line buffers to downstream parsers without character-decoding the entire file
