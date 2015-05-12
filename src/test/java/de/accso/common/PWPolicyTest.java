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


import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests some of the Password Policy
 *
 * @author Stefan Schubert
 */
public class PWPolicyTest {


    @Test
    public void testWeakPasswordShouldFailCheck() {

        PWPolicy pwPolicy = new PWPolicy(PWStrength.MEDIUM);

        final String dictionaryWord = "Apple";
        final boolean result = pwPolicy.check(dictionaryWord);
        assertFalse("This pw should not be accepted", result);

    }

    @Test
    public void testCoolPasswordShouldPassCheck() {

        PWPolicy pwPolicy = new PWPolicy(PWStrength.MEDIUM);

        String dictionaryWord = "SanSi#Bar2";
        boolean result = pwPolicy.check(dictionaryWord);
        assertTrue("This pw should be accepted", result);
    }

    @Test
    public void testGeneratedPasswordShouldPassCheck() {

        PWPolicy pwPolicy = new PWPolicy(PWStrength.MEDIUM);

        String newPassword = pwPolicy.generatePassword();
        boolean result = pwPolicy.check(newPassword);
        assertTrue("This pw should be accepted", result);
    }

    @Test
    public void testPasswordGeneratorQuality() {
        PWPolicy pwPolicy = new PWPolicy(PWStrength.MEDIUM);

        int generateNo = 0;
        boolean doublePW = false;
        Set<String> generatedSet = new HashSet<String>(generateNo);
        while (generateNo < 1000) {
            generateNo++;
            String newPassword = pwPolicy.generatePassword();
            if (generatedSet.contains(newPassword)) {
                doublePW = true;
                break;
            } else {
                generatedSet.add(newPassword);
            }
        }

        assertFalse("Generated double password at iteration: " + generateNo, doublePW);

        System.out.println(generatedSet);
    }

}