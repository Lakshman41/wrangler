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

package io.cdap.wrangler.api.parser; // Verify this package path is correct

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

// Standard Java imports first
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a parsed time duration token (e.g., "500ms", "1.5h").
* Implements the {@link Token} interface, storing the duration canonically as nanoseconds.
*/
public class TimeDuration implements Token {

    private final String rawValue;
    private final long nanoseconds;

    // Example pattern: number part, then unit (ns, us, ms, s, min, h)
    // Allows optional space. Handles case-insensitivity for units.
    // Updated to handle potential spaces before units better.
    private static final Pattern PATTERN = Pattern.compile(
            "^(-?[0-9]+(?:\\.[0-9]+)?)\\s*(ns|us|ms|s|min|h)$",
            Pattern.CASE_INSENSITIVE
    );

    // Nanoseconds per unit (using BigDecimal for precision during calculation)
    private static final BigDecimal NS_PER_US = BigDecimal.valueOf(1_000L);
    private static final BigDecimal NS_PER_MS = BigDecimal.valueOf(1_000_000L);
    private static final BigDecimal NS_PER_S  = BigDecimal.valueOf(1_000_000_000L);
    private static final BigDecimal NS_PER_MIN = NS_PER_S.multiply(BigDecimal.valueOf(60L));
    private static final BigDecimal NS_PER_H = NS_PER_MIN.multiply(BigDecimal.valueOf(60L));


    /**
     * Constructs a TimeDuration token from a string representation.
    *
    * @param token The raw string token (e.g., "150ms", "2.5s").
    * @throws IllegalArgumentException if the token format is invalid.
    */
    public TimeDuration(String token) {
        this.rawValue = Objects.requireNonNull(token, "Token string cannot be null");

        Matcher matcher = PATTERN.matcher(token.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time duration format: '" + token + "'");
        }

        String numberPart = matcher.group(1);
        String unitPart = matcher.group(2).toLowerCase(); // Convert to lowercase for switch

        BigDecimal value;
        try {
            value = new BigDecimal(numberPart);
        } catch (NumberFormatException e) {
            // Ensure line length is okay
            throw new IllegalArgumentException("Invalid number format in time duration: '" +
                                            numberPart + "' in token '" + token + "'", e);
        }

        BigDecimal multiplier;
        switch (unitPart) {
            case "ns":  multiplier = BigDecimal.ONE; break;
            case "us":  multiplier = NS_PER_US; break;
            case "ms":  multiplier = NS_PER_MS; break;
            case "s":   multiplier = NS_PER_S; break;
            case "min": multiplier = NS_PER_MIN; break;
            case "h":   multiplier = NS_PER_H; break;
            default:
                // Should not happen with the regex match success
                throw new IllegalStateException("Unknown time duration unit matched by regex: '" + unitPart + "'");
        }

        try {
            // Calculation split for clarity if needed, otherwise fine
            this.nanoseconds = value.multiply(multiplier)
                                    .setScale(0, RoundingMode.HALF_UP)
                                    .longValueExact();
        // Fixed RightCurly: catch starts on same line as preceding }
        } catch (ArithmeticException e) {
            // Fixed LineLength: Split the long exception message string
            throw new IllegalArgumentException("Calculated duration exceeds representable range " +
                                            "for nanoseconds in token '" + token + "'", e);
        }
    }

    // --- Methods required by the Token interface ---

    @Override
    public TokenType type() {
        // Make sure TIME_DURATION is added to the TokenType enum
        return TokenType.TIME_DURATION;
    }

    @Override
    public Object value() {
        // Return the canonical nanosecond value as the primary object value.
        return this.nanoseconds;
    }

    @Override
    public JsonElement toJson() {
        // Return the original string value as a JSON string primitive.
        // Gson handles escaping.
        return new JsonPrimitive(this.rawValue);
    }

    // --- Custom methods specific to TimeDuration ---

    /**
     * Gets the duration value represented by this token in nanoseconds.
    *
    * @return The duration in nanoseconds.
    */
    public long getNanoseconds() {
        return this.nanoseconds;
    }

    /**
     * Gets the duration value represented by this token, converted to seconds.
    * Note: This may involve loss of precision if the duration is not an exact number of seconds.
    *
    * @return The duration in seconds as a double.
    */
    public double getSeconds() {
        // Use BigDecimal for division to maintain precision before converting to double
        return BigDecimal.valueOf(this.nanoseconds)
                        .divide(NS_PER_S, 10, RoundingMode.HALF_UP) // Scale 10 for reasonable precision
                        .doubleValue();
    }

    /**
     * Gets the duration value represented by this token, converted to milliseconds.
    * Note: This may involve loss of precision if the duration is not an exact number of milliseconds.
    *
    * @return The duration in milliseconds as a double.
    */
    public double getMilliseconds() {
        return BigDecimal.valueOf(this.nanoseconds)
                        .divide(NS_PER_MS, 10, RoundingMode.HALF_UP)
                        .doubleValue();
    }

    // --- Optional but Recommended Overrides ---

    @Override
    public String toString() {
        // Useful for debugging
        return "TimeDuration{rawValue='" + rawValue + "', nanoseconds=" + nanoseconds + '}';
    }

    @Override
    public boolean equals(Object o) {
        // Braces added for Checkstyle
        if (this == o) {
            return true;
        }
        // Braces added for Checkstyle
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimeDuration that = (TimeDuration) o;
        return nanoseconds == that.nanoseconds;
    }

    @Override
    public int hashCode() {
        // Ensure java.util.Objects is imported
        return Objects.hash(nanoseconds);
    }
}
