/*
 * Copyright © 2017-2019 Cask Data, Inc.
 * // Optional: Add your own copyright year/name
 * // Copyright © 2023 Lakshman
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser; // Or the correct package

import com.google.gson.JsonElement; 
import com.google.gson.JsonPrimitive; 

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** // <<< ADDED Javadoc
 * Represents a parsed data size token (e.g., "10KB", "1.5MB").
 * Implements the {@link Token} interface, storing the size canonically as bytes.
 */
public class ByteSize implements Token {

    private final String rawValue;
    private final long bytes;

    // Constructor - Example using regex (adjust pattern as needed)
    private static final Pattern PATTERN = Pattern.compile(
        "^(-?[0-9]+(?:\\.[0-9]+)?)\\s*([KkMmGgTtPp]?)([Bb]?)$",
        Pattern.CASE_INSENSITIVE
    );

    public ByteSize(String token) {
        this.rawValue = token;

        Matcher matcher = PATTERN.matcher(token.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid byte size format: '" + token + "'");
        }

        String numberPart = matcher.group(1);
        String unitPrefix = matcher.group(2).toUpperCase(); // K, M, G, T, P or empty
        String unitBase = matcher.group(3).toUpperCase(); // B or empty

        // Check if unitBase is missing but prefix exists (e.g., "10K")
        if (unitBase.isEmpty() && !unitPrefix.isEmpty()) {
            throw new IllegalArgumentException("Invalid byte size format (missing 'B'): '" + token + "'");
        }
        // If both are empty, or only B is present, treat as bytes
        if (unitBase.isEmpty() && unitPrefix.isEmpty()) {
            // Assume bytes if no unit specified (e.g. "10") - adjust if this assumption is wrong
            unitBase = "B";
            // Consider if "10" without unit should be an error or default to bytes
            // throw new IllegalArgumentException("Missing unit (like B, KB, MB) in byte size: '" + token + "'");
        } else if (unitPrefix.isEmpty() && unitBase.equals("B")) {
            // Standard 'B' unit, prefix is empty string
        }


        BigDecimal value;
        try {
            value = new BigDecimal(numberPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in byte size: '" + numberPart + "'");
        }

        long multiplier = 1L;
        switch (unitPrefix) {
            case "":   multiplier = 1L; break; // Handles 'B' case
            case "K":  multiplier = 1024L; break;
            case "M":  multiplier = 1024L * 1024L; break;
            case "G":  multiplier = 1024L * 1024L * 1024L; break;
            case "T":  multiplier = 1024L * 1024L * 1024L * 1024L; break;
            case "P":  multiplier = 1024L * 1024L * 1024L * 1024L * 1024L; break;
            default:
                // Should not happen with the refined regex logic, but good practice
                throw new IllegalArgumentException("Unknown byte size prefix: '" + unitPrefix + "'");
        }

        // Multiply, using BigDecimal for precision, then convert to long
        this.bytes = value.multiply(BigDecimal.valueOf(multiplier)).longValue();

    }

    // --- Methods required by the Token interface ---

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE; // Assuming you added BYTE_SIZE to TokenType enum
    }

    @Override
    public Object value() {
        // Return the canonical byte value as the primary object value.
        return this.bytes;
    }

    @Override
    public JsonElement toJson() {
        // Return the original string value as a JSON string primitive.
        // Gson handles escaping.
        return new JsonPrimitive(this.rawValue);
    }

    // --- Custom method specific to ByteSize ---

    public long getBytes() {
        return this.bytes;
    }

    // --- Optional but Recommended Overrides ---

    @Override
    public String toString() {
        // Useful for debugging
        return "ByteSize{rawValue='" + rawValue + "', bytes=" + bytes + '}';
    }

    // Optional: Consider implementing equals() and hashCode() based on 'bytes'
    // for consistent behavior if used in Sets or Maps.
    @Override
    public boolean equals(Object o) {
        // CORRECTED Need Braces
        if (this == o) {
            return true;
        }
        // CORRECTED Need Braces
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ByteSize byteSize = (ByteSize) o;
        // Ensure you import java.util.Objects
        return bytes == byteSize.bytes;
    }

    @Override
    public int hashCode() {
        // Ensure you import java.util.Objects
        return Objects.hash(bytes);
    }

    // REMOVED stringValue() and length() as they are not in the Token interface.
    // REMOVED escapeJson() helper as it's not needed with JsonPrimitive.
}
