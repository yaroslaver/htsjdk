/*
 * The MIT License
 *
 * Date: 2015
 * Author: Pierre Lindenbaum @yokofakun
 *     Institut du Thorax , Nantes, France
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package htsjdk.samtools;

import htsjdk.HtsjdkTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SAMSequenceDictionaryTest extends HtsjdkTest {
    @Test
    public void testAliases() {
        final SAMSequenceRecord ssr1 = new SAMSequenceRecord("1", 1);
        final SAMSequenceRecord ssr2 = new SAMSequenceRecord("2", 1);

        final SAMSequenceDictionary dict = new SAMSequenceDictionary(
                Arrays.asList(ssr1, ssr2));
        Assert.assertEquals(dict.size(), 2);
        dict.addSequenceAlias("1", "chr1");
        dict.addSequenceAlias("1", "01");
        dict.addSequenceAlias("1", "1");
        dict.addSequenceAlias("01", "chr01");
        Assert.assertEquals(dict.size(), 2);
        Assert.assertNotNull(dict.getSequence("chr1"));
        Assert.assertNull(dict.getSequence("chr2"));
    }



    @DataProvider(name="testMergeDictionariesData")
    public Object[][] testMergeDictionariesData(){

        final SAMSequenceRecord rec1, rec2, rec3, rec4, rec5;
        rec1 = new SAMSequenceRecord("chr1", 100);
        rec2 = new SAMSequenceRecord("chr1", 101);
        rec2.setMd5("dummy");
        rec3 = new SAMSequenceRecord("chr1", SAMSequenceRecord.UNKNOWN_SEQUENCE_LENGTH);
        rec3.setMd5("dummy2");

        rec4 = new SAMSequenceRecord("chr1", 100);
        rec4.setAttribute(SAMSequenceRecord.URI_TAG,"file://some/file/name.ok");

        rec5 = new SAMSequenceRecord("chr2", 200);
        rec4.setAttribute(SAMSequenceRecord.URI_TAG,"file://some/file/name.ok");

        return new Object[][]{
                new Object[]{rec1, rec1, true},
                new Object[]{rec2, rec2, true},
                new Object[]{rec3, rec3, true},
                new Object[]{rec4, rec4, true},
                new Object[]{rec1, rec2, false},//since 100 != 101 in Length
                new Object[]{rec1, rec3, true},
                new Object[]{rec1, rec4, true},
                new Object[]{rec2, rec3, false}, // since MD5 is not equal
                new Object[]{rec2, rec4, false}, //length differs
                new Object[]{rec3, rec4, true},
                new Object[]{rec4, rec5, false}, // different name
        };
    }

    @Test(dataProvider = "testMergeDictionariesData", expectedExceptions = IllegalArgumentException.class)
    public void testMergeDictionaries(final SAMSequenceRecord rec1, final SAMSequenceRecord rec2, boolean canMerge) throws Exception {
        final SAMSequenceDictionary dict1 = new SAMSequenceDictionary(Collections.singletonList(rec1));
        final SAMSequenceDictionary dict2 = new SAMSequenceDictionary(Collections.singletonList(rec2));

        try {
            SAMSequenceDictionary.mergeDictionaries(dict1, dict2, SAMSequenceDictionary.DEFAULT_DICTIONARY_EQUAL_TAG);
        } catch (final IllegalArgumentException e) {
            if (canMerge) {
                throw new Exception("Expected to be able to merge dictionaries, but wasn't:" , e);
            } else {
                throw e;
            }
        }
        if (canMerge){
            throw new IllegalArgumentException("Expected to be able to merge dictionaries, and was indeed able to do so.");
        } else {
            throw new Exception("Expected to not be able to merge dictionaries, but was able");
        }
    }

    @DataProvider
    public Object[][] testIsSameDictionaryData() {

        final SAMSequenceRecord rec1, rec2;
        rec1 = new SAMSequenceRecord("chr1", 100);
        rec2 = new SAMSequenceRecord("chr2", 101);

        return new Object[][]{
                new Object[]{Arrays.asList(rec1), Arrays.asList(rec1), true},
                new Object[]{Arrays.asList(rec1), Arrays.asList(rec2), false},
                new Object[]{Arrays.asList(rec1, rec2), Arrays.asList(rec1), false}
        };
    }

    @Test(dataProvider = "testIsSameDictionaryData")
    public void testIsSameDictionary(final List<SAMSequenceRecord> recs1, final List<SAMSequenceRecord> recs2, final boolean isSameDictionary) {

        final SAMSequenceDictionary dict1 = new SAMSequenceDictionary(recs1);
        final SAMSequenceDictionary dict2 = new SAMSequenceDictionary(recs2);

        Assert.assertEquals(dict1.isSameDictionary(dict2), isSameDictionary);
    }
}
