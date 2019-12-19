/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.timedetector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.test.runner.AndroidJUnit4;

import com.android.internal.util.IndentingPrintWriter;
import com.android.server.timezonedetector.ReferenceWithHistory;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.StringWriter;

@RunWith(AndroidJUnit4.class)
public class ReferenceWithHistoryTest {

    @Test
    public void testBasicReferenceBehavior() {
        // Create a reference that will retain 2 history values.
        ReferenceWithHistory<String> referenceWithHistory =
                new ReferenceWithHistory<>(2 /* history */);
        TestRef<String> reference = new TestRef<>();

        // Check unset behavior.
        compareGet(referenceWithHistory, reference, null);
        assertNotNull(dumpReferenceWithHistory(referenceWithHistory));
        compareToString(referenceWithHistory, reference, "null");

        // Try setting null.
        setAndCompareReturnValue(referenceWithHistory, reference, null);
        compareGet(referenceWithHistory, reference, null);
        assertNotNull(dumpReferenceWithHistory(referenceWithHistory));
        compareToString(referenceWithHistory, reference, "null");

        // Try setting a non-null value.
        setAndCompareReturnValue(referenceWithHistory, reference, "Foo");
        compareGet(referenceWithHistory, reference, "Foo");
        assertNotNull(dumpReferenceWithHistory(referenceWithHistory));
        compareToString(referenceWithHistory, reference, "Foo");

        // Try setting null again.
        setAndCompareReturnValue(referenceWithHistory, reference, "Foo");
        compareGet(referenceWithHistory, reference, "Foo");
        assertNotNull(dumpReferenceWithHistory(referenceWithHistory));
        compareToString(referenceWithHistory, reference, "Foo");

        // Try a non-null value again.
        setAndCompareReturnValue(referenceWithHistory, reference, "Bar");
        compareGet(referenceWithHistory, reference, "Bar");
        assertNotNull(dumpReferenceWithHistory(referenceWithHistory));
        compareToString(referenceWithHistory, reference, "Bar");
    }

    @Test
    public void testValueHistoryBehavior() {
        // Create a reference that will retain 2 history values.
        ReferenceWithHistory<String> referenceWithHistory =
                new ReferenceWithHistory<>(2 /* history */);
        TestRef<String> reference = new TestRef<>();

        // Assert behavior before anything is set.
        assertEquals(0, referenceWithHistory.getHistoryCount());

        // Set a value (1).
        setAndCompareReturnValue(referenceWithHistory, reference, "V1");
        assertEquals(1, referenceWithHistory.getHistoryCount());

        // Set a value (2).
        setAndCompareReturnValue(referenceWithHistory, reference, "V2");
        assertEquals(2, referenceWithHistory.getHistoryCount());

        // Set a value (3).
        // We should have hit the limit of "2 history values retained per key".
        setAndCompareReturnValue(referenceWithHistory, reference, "V3");
        assertEquals(2, referenceWithHistory.getHistoryCount());
    }

    /**
     * A simple class that has the same behavior as ReferenceWithHistory without the history. Used
     * in tests for comparison.
     */
    private static class TestRef<V> {
        private V mValue;

        public V get() {
            return mValue;
        }

        public V set(V value) {
            V previous = mValue;
            mValue = value;
            return previous;
        }

        public String toString() {
            return String.valueOf(mValue);
        }
    }

    private static void compareGet(
            ReferenceWithHistory<?> referenceWithHistory, TestRef<?> reference, Object value) {
        assertEquals(reference.get(), referenceWithHistory.get());
        assertEquals(value, reference.get());
    }

    private static <T> void setAndCompareReturnValue(
            ReferenceWithHistory<T> referenceWithHistory, TestRef<T> reference, T newValue) {
        assertEquals(reference.set(newValue), referenceWithHistory.set(newValue));
    }

    private static void compareToString(
            ReferenceWithHistory<?> referenceWithHistory, TestRef<?> reference, String expected) {
        assertEquals(reference.toString(), referenceWithHistory.toString());
        assertEquals(expected, referenceWithHistory.toString());
    }

    private static String dumpReferenceWithHistory(ReferenceWithHistory<?> referenceWithHistory) {
        StringWriter stringWriter = new StringWriter();
        try (IndentingPrintWriter ipw = new IndentingPrintWriter(stringWriter, " ")) {
            referenceWithHistory.dump(ipw);
            return stringWriter.toString();
        }
    }
}
