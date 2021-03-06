/*
 * Copyright (C) 2017 Jorge Ruesga
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
package com.ruesga.rview.misc;

import com.ruesga.rview.TestUtils;
import com.ruesga.rview.attachments.Attachment;
import com.ruesga.rview.gerrit.model.ChangeMessageInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(PowerMockRunner.class)
@PrepareForTest({android.util.Log.class, android.text.TextUtils.class})
public class StringHelperTest {

    @Before
    public void setUp() throws Exception {
        TestUtils.mockCommonAndroidClasses();
    }

    @Test
    public void testExtractAllAttachments() throws IOException {
        ChangeMessageInfo message = TestUtils.loadJson(
                ChangeMessageInfo.class, "/com/ruesga/rview/misc/attachments1.txt");
        List<Attachment> attachments = StringHelper.extractAllAttachments(message);

        assertNotNull(attachments);
        assertEquals(2, attachments.size());
        assertEquals("Yosemite Tree", attachments.get(0).mName);
        assertEquals("image/jpeg", attachments.get(0).mMimeType);
        assertEquals(246994, attachments.get(0).mSize);

        message = TestUtils.loadJson(
                ChangeMessageInfo.class, "/com/ruesga/rview/misc/attachments2.txt");
        attachments = StringHelper.extractAllAttachments(message);
        assertEquals(1, attachments.size());
    }

    @Test
    public void testRemoveExtraLines01() throws IOException {
        String testMessage = TestUtils.loadString(
                "/com/ruesga/rview/misc/removeExtraLines01.msg.txt");
        String expectedMessage = TestUtils.loadString(
                "/com/ruesga/rview/misc/removeExtraLines01.expected.txt");
        assertEquals(expectedMessage, StringHelper.removeLineBreaks(testMessage));
    }

    @Test
    public void testRemoveExtraLines02() throws IOException {
        String testMessage = TestUtils.loadString(
                "/com/ruesga/rview/misc/removeExtraLines02.msg.txt");
        String expectedMessage = TestUtils.loadString(
                "/com/ruesga/rview/misc/removeExtraLines02.expected.txt");

        testPrepareForQuotes(testMessage, expectedMessage);
    }

    @Test
    public void testRemoveExtraLines03() throws IOException {
        String testMessage = TestUtils.loadString(
                "/com/ruesga/rview/misc/removeExtraLines03.msg.txt");
        String expectedMessage = TestUtils.loadString(
                "/com/ruesga/rview/misc/removeExtraLines03.expected.txt");

        testPrepareForQuotes(testMessage, expectedMessage);
    }

    private void testPrepareForQuotes(String testMessage, String expectedMessage) {
        String[] paragraphs = StringHelper.obtainParagraphs(testMessage);
        StringBuilder sb = new StringBuilder();
        for (String p : paragraphs) {
            if (StringHelper.isQuote(p)) {
                sb.append(StringHelper.obtainQuote(StringHelper.prepareForQuote(p)));
            } else if (StringHelper.isList(p)) {
                sb.append(p);
            } else if (StringHelper.isPreFormat(p)) {
                sb.append(StringHelper.obtainPreFormatMessage(p));
            } else {
                sb.append(p);
            }
            sb.append("\n\n");
        }

        assertEquals(expectedMessage, sb.toString().trim());
    }
}
