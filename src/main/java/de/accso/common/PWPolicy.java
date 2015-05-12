/*
 * Copyright 2015 Accso GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package de.accso.common;

import org.passay.*;

import java.util.Arrays;
import java.util.List;

/**
 * API to generate and check passwords against a simple policy.
 *
 * @author Stefan Schubert
 */
public class PWPolicy {
// ------------------------------ FIELDS ------------------------------

    private PasswordValidator validator;
    private List<CharacterRule> generatorRules;
    private final PasswordGenerator generator = new PasswordGenerator();

// --------------------------- CONSTRUCTORS ---------------------------

    private PWPolicy() {
        // Util Class
    }

    public PWPolicy(final PWStrength pPWStrength) {
        int upperCaseCount = 100;
        int lowerCaseCount = 100;
        int digitCount = 100;
        int specialCharCount = 100;
        int minLength = 100;
        int maxLength = 200;

        if (pPWStrength == PWStrength.MEDIUM) {
            lowerCaseCount = 2;
            upperCaseCount = 2;
            digitCount = 1;
            specialCharCount = 1;
            minLength = 8;
            maxLength = 12;
        }

        if (pPWStrength == PWStrength.HIGH) {
            upperCaseCount = 2;
            lowerCaseCount = 2;
            digitCount = 2;
            specialCharCount = 2;
            minLength = 10;
            maxLength = 16;
        }

        validator = new PasswordValidator(Arrays.asList(
                new LengthRule(minLength, maxLength),
                new UppercaseCharacterRule(upperCaseCount),
                new LowercaseCharacterRule(lowerCaseCount),
                new DigitCharacterRule(digitCount),
                new SpecialCharacterRule(specialCharCount),
                new WhitespaceRule()));

        generatorRules = Arrays.<CharacterRule>asList(
                new UppercaseCharacterRule(upperCaseCount),
                new LowercaseCharacterRule(lowerCaseCount),
                new DigitCharacterRule(digitCount),
                new SpecialCharacterRule(specialCharCount));
    }

// -------------------------- OTHER METHODS --------------------------

    public boolean check(final String pPhrase) {
        final RuleResult result = validator.validate(new PasswordData(pPhrase));
        return result.isValid();
    }

    public String generatePassword() {
        return generator.generatePassword(8, generatorRules);
    }
}
