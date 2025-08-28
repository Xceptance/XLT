/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.api.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class XltCharBufferTest
{
    @Test
    public void empty_const()
    {
        assertEquals(0, XltCharBuffer.EMPTY.length());
        assertArrayEquals(new char[0], XltCharBuffer.EMPTY.toCharArray());
        assertSame(XltCharBuffer.EMPTY, XltCharBuffer.EMPTY);
        assertEquals(0, XltCharBuffer.EMPTY.hashCode());
    }

    @Test
    public void empty()
    {
        assertEquals(0, XltCharBuffer.empty().length());
        assertArrayEquals(new char[0], XltCharBuffer.empty().toCharArray());
        assertEquals(XltCharBuffer.empty(), XltCharBuffer.empty());
        assertSame(XltCharBuffer.empty(), XltCharBuffer.empty());
        assertEquals(0, XltCharBuffer.empty().hashCode());
    }

    @Test
    public void emptyWhenNull()
    {
        assertEquals(XltCharBuffer.empty(), XltCharBuffer.emptyWhenNull(null));

        var x = XltCharBuffer.valueOf("foo");
        assertEquals(x, XltCharBuffer.emptyWhenNull(x));
    }

    @Test
    public void ctr_chararray()
    {
        {
            var c = new char[] {};
            var x = new XltCharBuffer((char[]) null);
            assertEquals(c.length, x.length());
            assertArrayEquals(c, x.toCharArray());
        }
        {
            var c = new char[] {};
            var x = new XltCharBuffer(c);
            assertEquals(c.length, x.length());
            assertArrayEquals(c, x.toCharArray());
        }
        {
            var c = "a".toCharArray();
            var x = new XltCharBuffer(c);
            assertEquals(c.length, x.length());
            assertArrayEquals(c, x.toCharArray());
        }
        {
            var c = "jhjashdj sjdh".toCharArray();
            var x = new XltCharBuffer(c);
            assertEquals(c.length, x.length());
            assertArrayEquals(c, x.toCharArray());
        }
        {
            // ensure that we don't have a copy of the array for speed
            var c = "012345".toCharArray();
            var x = new XltCharBuffer(c);
            c[0] = '9';
            assertArrayEquals("912345".toCharArray(), x.toCharArray());
        }

        // no futher edge cases
    }

    @Test
    public void ctr_chararray_from_length()
    {
        {
            // null, we will correct the passed numbers, only
            // edge case handling we have
            var x = new XltCharBuffer(null, 1, 8);
            assertEquals(0, x.length());
            assertArrayEquals("".toCharArray(), x.toCharArray());
        }
        {
            var s = "".toCharArray();
            var x = new XltCharBuffer(s, 0, 0);
            assertEquals(s.length, x.length());
            assertArrayEquals(s, x.toCharArray());
        }
        {
            var s = "0123456789".toCharArray();
            var x = new XltCharBuffer(s, 0, 10);
            assertEquals(s.length, x.length());
            assertArrayEquals(s, x.toCharArray());
        }
        {
            var s = "0123456789".toCharArray();
            var x = new XltCharBuffer(s, 0, 9);
            assertEquals("012345678".length(), x.length());
            assertArrayEquals("012345678".toCharArray(), x.toCharArray());
        }
        {
            var s = "0123456789".toCharArray();
            var x = new XltCharBuffer(s, 1, 8);
            assertEquals("12345678".length(), x.length());
            assertArrayEquals("12345678".toCharArray(), x.toCharArray());
        }
        {
            var s = "0123456789".toCharArray();
            var x = new XltCharBuffer(s, 1, 0);
            assertEquals(0, x.length());
            assertArrayEquals("".toCharArray(), x.toCharArray());
        }
    }

    // XltCharBuffer.valueOf(OpenStringBuilder) is currently not supported.
    /*
    @Test
    public void valueof_openstringbuilder()
    {
        {
            var s = "";
            var os = new OpenStringBuilder(10).append(s);
            var x = XltCharBuffer.valueOf(os);
            assertEquals(s.length(), x.length());
            assertArrayEquals(s.toCharArray(), x.toCharArray());
        }
        {
            var s = "012345";
            var os = new OpenStringBuilder(10).ensureCapacity(100).append(s);
            var x = XltCharBuffer.valueOf(os);
            assertEquals(s.length(), x.length());
            assertArrayEquals(s.toCharArray(), x.toCharArray());
        }
        {
            var s = "012345";
            var os = new OpenStringBuilder(6).append(s);
            var x = XltCharBuffer.valueOf(os);
            assertEquals(s.length(), x.length());
            assertArrayEquals(s.toCharArray(), x.toCharArray());
        }
    }
     */

    @Test
    public void valueof_chararray()
    {
        {
            var c = new char[] {};
            var x = XltCharBuffer.valueOf((char[]) null);
            assertEquals(c.length, x.length());
            assertArrayEquals(c, x.toCharArray());
        }
        {
            var c = new char[] {};
            var x = XltCharBuffer.valueOf(c);
            assertEquals(c.length, x.length());
            assertArrayEquals(c, x.toCharArray());
        }
        {
            var c = "a".toCharArray();
            var x = XltCharBuffer.valueOf(c);
            assertEquals(c.length, x.length());
            assertArrayEquals(c, x.toCharArray());
        }
        {
            var c = "jhjashdj sjdh".toCharArray();
            var x = XltCharBuffer.valueOf(c);
            assertEquals(c.length, x.length());
            assertArrayEquals(c, x.toCharArray());
        }
        {
            // ensure that we don't have a copy of the array for speed
            var c = "012345".toCharArray();
            var x = XltCharBuffer.valueOf(c);
            c[0] = '9';
            assertArrayEquals("912345".toCharArray(), x.toCharArray());
        }

    }

    @Test
    public void valueof_string()
    {
        var f = new Consumer<String>()
        {
            @Override
            public void accept(String s)
            {
                var x = XltCharBuffer.valueOf(s);
                assertEquals(s.length(), x.length());
                assertArrayEquals(s.toCharArray(), x.toCharArray());
            }

        };

        f.accept("");
        f.accept("9q389328932");
    }

    @Test
    public void valueof_string_null()
    {
        // to maintain compatbility to String, we keep null strings a null charbuffer

        assertNull(XltCharBuffer.valueOf((String)null));
    }

    @Test
    public void valueof_string_string()
    {
        var f = new BiConsumer<String, String>()
        {
            @Override
            public void accept(String s1, String s2)
            {
                var x = XltCharBuffer.valueOf(s1, s2);
                var e = s1 + s2;
                assertEquals(e.length(), x.length());
                assertTrue(x.equals(XltCharBuffer.valueOf(e)));
                assertArrayEquals(e.toCharArray(), x.toCharArray());
            }
        };

        f.accept("", "");
        f.accept("", "s");
        f.accept("s", "");
        f.accept("12345", "1234asdfasfd");
    }

    @Test
    public void valueof_string_string_string()
    {
        var f = new Object()
        {
            public void accept(String s1, String s2, String s3)
            {
                var x = XltCharBuffer.valueOf(s1, s2, s3);
                var e = s1 + s2 + s3;
                assertEquals(e.length(), x.length());
                assertTrue(x.equals(XltCharBuffer.valueOf(e)));
                assertArrayEquals(e.toCharArray(), x.toCharArray());
            }
        };

        f.accept("", "", "");
        f.accept("", "s", "s");
        f.accept("s", "", "");
        f.accept("12345", "1234asdfasfd", "098765");
    }

    @Test
    public void valueof_string_vargs()
    {
        var f = new Object()
        {
            public void accept(String s1, String s2, String s3, String... vargs)
            {
                var x = XltCharBuffer.valueOf(s1, s2, s3, vargs);
                var e = s1 + s2 + s3;

                if (vargs != null)
                {
                    for (String s : vargs)
                    {
                        e += s;
                    }
                }
                assertEquals(e.length(), x.length());
                assertTrue(x.equals(XltCharBuffer.valueOf(e)));
                assertArrayEquals(e.toCharArray(), x.toCharArray());
            }
        };

        f.accept("", "", "", "");
        f.accept("", "s", "s", "");
        f.accept("s", "", "", "");
        f.accept("12345", "1234asdfasfd", "098765", "sds");
        f.accept("12345", "1234asdfasfd", "098765", "sds", "aa");

        // fallback to three params
        f.accept("12345", "1234asdfasfd", "098765");
        f.accept("12345", "1234asdfasfd", "098765", new String[] {});
    }

    @Test
    public void valueof_buffer_char()
    {
        var f = new Object()
        {
            public void accept(XltCharBuffer x, char c)
            {
                var r = XltCharBuffer.valueOf(x, c);
                var e = x.toString() + c;
                assertEquals(e.length(), r.length());
                assertArrayEquals(e.toCharArray(), r.toCharArray());
            }
        };

        f.accept(XltCharBuffer.valueOf(""), 'a');
        f.accept(XltCharBuffer.valueOf("Tes"), 't');
        f.accept(XltCharBuffer.valueOf("0123456").viewByLength(2, 2), 't');
    }

    @Test
    public void valueof_buffer_buffer()
    {
        var f = new Object()
        {
            public void accept(XltCharBuffer x1, XltCharBuffer x2)
            {
                var r = XltCharBuffer.valueOf(x1, x2);
                var e = x1.toString() + x2.toString();
                assertEquals(e.length(), r.length());
                assertArrayEquals(e.toCharArray(), r.toCharArray());
            }
        };

        f.accept(XltCharBuffer.valueOf(""), XltCharBuffer.valueOf(""));
        f.accept(XltCharBuffer.valueOf("Tes"), XltCharBuffer.valueOf("t"));
        f.accept(
                 XltCharBuffer.valueOf("0123456").viewByLength(2, 2),
                 XltCharBuffer.valueOf("0123456").viewByLength(2, 2));
    }

    @Test
    public void valueof_buffer_buffer_buffer()
    {
        var f = new Object()
        {
            public void accept(XltCharBuffer x1, XltCharBuffer x2, XltCharBuffer x3)
            {
                var r = XltCharBuffer.valueOf(x1, x2, x3);
                var e = x1.toString() + x2.toString() + x3.toString();
                assertEquals(e.length(), r.length());
                assertArrayEquals(e.toCharArray(), r.toCharArray());
            }
        };

        f.accept(XltCharBuffer.valueOf(""), XltCharBuffer.valueOf(""), XltCharBuffer.valueOf(""));
        f.accept(XltCharBuffer.valueOf("Tes"), XltCharBuffer.valueOf("t"), XltCharBuffer.valueOf("0987"));
        f.accept(
                 XltCharBuffer.valueOf("0123456").viewByLength(2, 2),
                 XltCharBuffer.valueOf("0123456").viewByLength(2, 2),
                 XltCharBuffer.valueOf("821821").viewByLength(2, 4));
    }

    @Test
    public void put()
    {
        {
            var b = XltCharBuffer.valueOf("Test");

            b.put(1, 'ä'); Assert.assertEquals("Täst", b.toString());
            b.put(0, '1'); Assert.assertEquals("1äst", b.toString());
            b.put(3, '3'); Assert.assertEquals("1äs3", b.toString());
            b.put(2, '2'); Assert.assertEquals("1ä23", b.toString());
        }
        {
            var b = XltCharBuffer.valueOf("0123456789").viewFromTo(3, 7);

            b.put(1, 'a'); Assert.assertEquals("3a56", b.toString());
            b.put(0, 'b'); Assert.assertEquals("ba56", b.toString());
            b.put(3, 'c'); Assert.assertEquals("ba5c", b.toString());
            b.put(2, 'd'); Assert.assertEquals("badc", b.toString());
        }
    }

    @Test
    public void charAt()
    {
        var b = XltCharBuffer.valueOf("0123456789");
        var b1 = b.viewByLength(0, 4); // 0123
        var b2 = b.viewByLength(3, 3); // 345
        var b3 = b2.viewByLength(1, 2); // 45

        Assert.assertEquals('0', b.charAt(0));
        Assert.assertEquals('5', b.charAt(5));
        Assert.assertEquals('6', b.charAt(6));

        Assert.assertEquals('0', b1.charAt(0));
        Assert.assertEquals('1', b1.charAt(1));
        Assert.assertEquals('2', b1.charAt(2));
        Assert.assertEquals('3', b1.charAt(3));

        Assert.assertEquals('3', b2.charAt(0));
        Assert.assertEquals('4', b2.charAt(1));
        Assert.assertEquals('5', b2.charAt(2));

        Assert.assertEquals('4', b3.charAt(0));
        Assert.assertEquals('5', b3.charAt(1));
    }

    @Test
    public void viewByLength()
    {
        var x = XltCharBuffer.valueOf("0123456");

        assertEquals(XltCharBuffer.valueOf(""), x.viewByLength(0, 0));
        assertEquals(XltCharBuffer.valueOf("0"), x.viewByLength(0, 1));
        assertEquals(XltCharBuffer.valueOf("01"), x.viewByLength(0, 2));
        assertEquals(XltCharBuffer.valueOf("012"), x.viewByLength(0, 3));
        assertEquals(XltCharBuffer.valueOf("0123"), x.viewByLength(0, 4));
        assertEquals(XltCharBuffer.valueOf("01234"), x.viewByLength(0, 5));
        assertEquals(XltCharBuffer.valueOf("012345"), x.viewByLength(0, 6));
        assertEquals(XltCharBuffer.valueOf("0123456"), x.viewByLength(0, 7));

        assertEquals(XltCharBuffer.valueOf("1"), x.viewByLength(1, 1));
        assertEquals(XltCharBuffer.valueOf("123456"), x.viewByLength(1, 6));
        assertEquals(XltCharBuffer.valueOf("6"), x.viewByLength(6, 1));
    }

    @Test
    public void viewByLength_viewByLength()
    {
        var x = XltCharBuffer.valueOf("0123456");

        assertEquals(XltCharBuffer.valueOf(""), x.viewByLength(0, 1).viewByLength(0, 0));

        assertEquals(XltCharBuffer.valueOf("0"), x.viewByLength(0, 2).viewByLength(0, 1));
        assertEquals(XltCharBuffer.valueOf("1"), x.viewByLength(0, 2)/*01*/.viewByLength(1, 1));

        assertEquals(XltCharBuffer.valueOf("012"), x.viewByLength(0, 3).viewByLength(0, 3));
        assertEquals(XltCharBuffer.valueOf("01"), x.viewByLength(0, 3).viewByLength(0, 2));
        assertEquals(XltCharBuffer.valueOf("0"), x.viewByLength(0, 3).viewByLength(0, 1));

        assertEquals(XltCharBuffer.valueOf( "1"), x.viewByLength(1, 3)/*123*/.viewByLength(0, 1));
        assertEquals(XltCharBuffer.valueOf( "2"), x.viewByLength(1, 3)/*123*/.viewByLength(1, 1));
        assertEquals(XltCharBuffer.valueOf( "3"), x.viewByLength(1, 3)/*123*/.viewByLength(2, 1));
        assertEquals(XltCharBuffer.valueOf("23"), x.viewByLength(1, 3)/*123*/.viewByLength(1, 2));
        assertEquals(XltCharBuffer.valueOf("123"), x.viewByLength(1, 3)/*123*/.viewByLength(0, 3));

        assertEquals(XltCharBuffer.valueOf("45"), x.viewByLength(4, 3)/*456*/.viewByLength(0, 2));

        assertEquals(XltCharBuffer.valueOf("b"), XltCharBuffer.valueOf("abc").viewByLength(1, 1).viewByLength(0, 1));
    }


    @Test
    public void viewFromTo()
    {
        {
            var b = XltCharBuffer.valueOf("TestFo2");

            Assert.assertEquals("", b.viewFromTo(0, 0).toString());
            Assert.assertEquals("T", b.viewFromTo(0, 1).toString());
            Assert.assertEquals("TestFo2", b.viewFromTo(0, 7).toString());
            Assert.assertEquals("2", b.viewFromTo(6, 7).toString());
            Assert.assertEquals("", b.viewFromTo(6, 6).toString());

            Assert.assertEquals("est", b.viewFromTo(1, 4).toString());
            Assert.assertEquals("Fo2", b.viewFromTo(4, 7).toString());
        }
        {
            var b = XltCharBuffer.valueOf("TA0123456789A").viewFromTo(2, 12);

            Assert.assertEquals("0123456789", b.toString());
            Assert.assertEquals("", b.viewFromTo(0, 0).toString());
            Assert.assertEquals("0", b.viewFromTo(0, 1).toString());
            Assert.assertEquals("0123456", b.viewFromTo(0, 7).toString());
            Assert.assertEquals("6", b.viewFromTo(6, 7).toString());
            Assert.assertEquals("", b.viewFromTo(6, 6).toString());

            Assert.assertEquals("123", b.viewFromTo(1, 4).toString());
            Assert.assertEquals("456", b.viewFromTo(4, 7).toString());
        }
    }

    @Test
    public void substring_from_to()
    {
        // just to make sure it is exactly like substring of Java
        var a = "0123456789 abcdef";
        Assert.assertEquals(a.substring(0, 7), XltCharBuffer.valueOf(a).substring(0, 7).toString());
        Assert.assertEquals(a.substring(1, 7), XltCharBuffer.valueOf(a).substring(1, 7).toString());
        Assert.assertEquals(a.substring(2, 2), XltCharBuffer.valueOf(a).substring(2, 2).toString());

        // identical code to viewFromTo, so the same stuff here
        {
            var b = XltCharBuffer.valueOf("TestFo2");

            Assert.assertEquals("", b.substring(0, 0).toString());
            Assert.assertEquals("T", b.substring(0, 1).toString());
            Assert.assertEquals("TestFo2", b.substring(0, 7).toString());
            Assert.assertEquals("2", b.substring(6, 7).toString());
            Assert.assertEquals("", b.substring(6, 6).toString());

            Assert.assertEquals("est", b.substring(1, 4).toString());
            Assert.assertEquals("Fo2", b.substring(4, 7).toString());
        }
        {
            var b = XltCharBuffer.valueOf("TA0123456789A").substring(2, 12);

            Assert.assertEquals("0123456789", b.toString());
            Assert.assertEquals("", b.substring(0, 0).toString());
            Assert.assertEquals("0", b.substring(0, 1).toString());
            Assert.assertEquals("0123456", b.substring(0, 7).toString());
            Assert.assertEquals("6", b.substring(6, 7).toString());
            Assert.assertEquals("", b.substring(6, 6).toString());

            Assert.assertEquals("123", b.substring(1, 4).toString());
            Assert.assertEquals("456", b.substring(4, 7).toString());
        }

        final String base = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

        for (int i = 0; i < base.length(); i++)
        {
            var s = base.substring(0, i);
            var x = XltCharBuffer.valueOf(base).substring(0, i);
            Assert.assertEquals(s.length(), x.length());
            Assert.assertTrue(XltCharBuffer.valueOf(s).equals(x));
            Assert.assertEquals(s.hashCode(), x.hashCode());
        }
    }

    @Test
    public void substring_from()
    {
        {
            var b = XltCharBuffer.valueOf("0123456");

            assertEquals("0123456", b.substring(0).toString());
            assertEquals("123456", b.substring(1).toString());
            assertEquals("23456", b.substring(2).toString());
            assertEquals("3456", b.substring(3).toString());
            assertEquals("456", b.substring(4).toString());
            assertEquals("56", b.substring(5).toString());
            assertEquals("6", b.substring(6).toString());
            assertEquals("", b.substring(7).toString());
        }

        final String base = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

        for (int i = 0; i < base.length(); i++)
        {
            var s = base.substring(i);
            var x = XltCharBuffer.valueOf(base).substring(i);
            Assert.assertEquals(s.length(), x.length());
            Assert.assertTrue(XltCharBuffer.valueOf(s).equals(x));
            Assert.assertEquals(s.hashCode(), x.hashCode());
        }

        Assert.assertTrue(XltCharBuffer.valueOf("http://www.foo.bar").substring(7).equals(XltCharBuffer.valueOf("www.foo.bar")));
    }


    @Test
    public void substring_from_2()
    {
        {
            var x1 = XltCharBuffer.valueOf("a").substring(0);
            assertEquals(XltCharBuffer.valueOf("a"), x1);

            var x2 = XltCharBuffer.valueOf("abc").viewByLength(0, 1).substring(0);
            assertEquals(XltCharBuffer.valueOf("a"), x2);

            var x3 = XltCharBuffer.valueOf("abc").viewByLength(1, 1);
            x3 = x3.substring(0);
            assertEquals(XltCharBuffer.valueOf("b"), x3);

            var x4 = XltCharBuffer.valueOf("bca").viewByLength(2, 1).substring(0);
            assertEquals(XltCharBuffer.valueOf("a"), x4);
        }

        {
            var x1 = XltCharBuffer.valueOf("abc").substring(0);
            assertEquals(XltCharBuffer.valueOf("abc"), x1);

            var x2 = XltCharBuffer.valueOf("abc123").viewByLength(0, 3).substring(0);
            assertEquals(XltCharBuffer.valueOf("abc"), x2);

            var x3 = XltCharBuffer.valueOf("abc123").viewByLength(3, 3).substring(0);
            assertEquals(XltCharBuffer.valueOf("123"), x3);

            var x4 = XltCharBuffer.valueOf("abc123").viewByLength(2, 3).substring(0);
            assertEquals(XltCharBuffer.valueOf("c12"), x4);
        }

        {
            var x1 = XltCharBuffer.valueOf("abc").substring(1);
            assertEquals(XltCharBuffer.valueOf("bc"), x1);

            var x2 = XltCharBuffer.valueOf("abc123").viewByLength(0, 3).substring(1);
            assertEquals(XltCharBuffer.valueOf("bc"), x2);

            var x3 = XltCharBuffer.valueOf("abc123").viewByLength(3, 3).substring(1);
            assertEquals(XltCharBuffer.valueOf("23"), x3);

            var x4 = XltCharBuffer.valueOf("abc123").viewByLength(2, 3).substring(1);
            assertEquals(XltCharBuffer.valueOf("12"), x4);
        }
    }

    @Test
    public void toCharArray()
    {
        assertArrayEquals("".toCharArray(), new XltCharBuffer((char[])null).toCharArray());
        assertArrayEquals("".toCharArray(), XltCharBuffer.valueOf("").toCharArray());
        assertArrayEquals("0123456".toCharArray(), XltCharBuffer.valueOf("0123456").toCharArray());

        {
            // XltCharBuffer.valueOf(OpenStringBuilder) is currently not supported.
            //
            // var sb = new OpenStringBuilder(10).append("01234");
            // assertArrayEquals("01234".toCharArray(), XltCharBuffer.valueOf(sb).toCharArray());
        }
        {
            var x = XltCharBuffer.valueOf("copyTestStuff").viewByLength(4,  4);
            assertArrayEquals("Test".toCharArray(), x.toCharArray());
        }
        {
            // Make sure it is a copy
            var x = XltCharBuffer.valueOf("copy");
            assertArrayEquals("0123456".toCharArray(), XltCharBuffer.valueOf("0123456").toCharArray());

            x.put(0, 'A');
            assertArrayEquals("0123456".toCharArray(), XltCharBuffer.valueOf("0123456").toCharArray());
        }
    }


    @Test
    public void test_toString()
    {
        assertEquals("", new XltCharBuffer((char[])null).toString());
        assertEquals("", XltCharBuffer.valueOf("").toString());
        assertEquals("0123456", XltCharBuffer.valueOf("0123456").toString());

        {
            // XltCharBuffer.valueOf(OpenStringBuilder) is currently not supported.
            //
            // var sb = new OpenStringBuilder(10).append("01234");
            // assertEquals("01234", XltCharBuffer.valueOf(sb).toString());
        }
    }

    @Test
    public void peakAhead()
    {
        {
            var b = XltCharBuffer.valueOf("012345");

            Assert.assertEquals('0', b.peakAhead(0));
            Assert.assertEquals('1', b.peakAhead(1));
            Assert.assertEquals('2', b.peakAhead(2));
            Assert.assertEquals('3', b.peakAhead(3));
            Assert.assertEquals('4', b.peakAhead(4));
            Assert.assertEquals('5', b.peakAhead(5));
            Assert.assertEquals(0, b.peakAhead(6));
            Assert.assertEquals(0, b.peakAhead(7));
        }
        {
            var b = XltCharBuffer.valueOf("TA01234X").viewFromTo(2, 7);

            Assert.assertEquals('0', b.peakAhead(0));
            Assert.assertEquals('1', b.peakAhead(1));
            Assert.assertEquals('2', b.peakAhead(2));
            Assert.assertEquals('3', b.peakAhead(3));
            Assert.assertEquals('4', b.peakAhead(4));
            Assert.assertEquals(0, b.peakAhead(5));
            Assert.assertEquals(0, b.peakAhead(6));
        }
    }

    @Test
    public void length()
    {
        assertEquals(0, XltCharBuffer.valueOf("").length());
        assertEquals(1, XltCharBuffer.valueOf("T").length());
        assertEquals(2, XltCharBuffer.valueOf("Ta").length());

        assertEquals(0, XltCharBuffer.valueOf("01234").viewByLength(0, 0).length());
        assertEquals(1, XltCharBuffer.valueOf("01234").viewByLength(1, 1).length());
        assertEquals(5, XltCharBuffer.valueOf("01234").viewByLength(0, 5).length());
        assertEquals(2, XltCharBuffer.valueOf("01234").viewByLength(2, 2).length());

        assertEquals(3, XltCharBuffer.valueOf("01234").viewByLength(1, 4).viewByLength(1, 3).length());
        assertEquals(2, XltCharBuffer.valueOf("01234").viewByLength(0, 4).viewByLength(2, 2).length());
    }

    @Test
    public void indexOf_char()
    {
        var f = new Object()
        {
            public void test(String s, char c)
            {
                var x = XltCharBuffer.valueOf(s);
                assertEquals(s.indexOf(c), x.indexOf(c));
            }
            public void testSubstring(String s, int from, int length, char c)
            {
                var x = XltCharBuffer.valueOf(s).viewByLength(from, length);
                assertEquals(s.substring(from, from + length).indexOf(c), x.indexOf(c));
            }
        };

        f.test("", 'a');
        f.test("abc", 'a');
        f.test("abc", 'b');
        f.test("abc", 'c');
        f.test("aaa", 'c');
        f.test("aaa", 'a');

        f.test("", 'a');
        f.testSubstring("AAAabcBBB", 3, 3, 'b');
        f.testSubstring("abcBBB", 0, 3, 'a');
        f.testSubstring("AAAabc", 3, 3, 'c');
    }

    @Test
    public void indexOf_buffer()
    {
        var f = new Object()
        {
            public void test(String s1, String s2)
            {
                var x1 = XltCharBuffer.valueOf(s1);
                var x2 = XltCharBuffer.valueOf(s2);
                assertEquals(s1.indexOf(s2), x1.indexOf(x2));
            }
            // Test method to cover also substring views aka offset problems
            // To cover all options, we can disable the substring part with from == -1
            public void test(String s1, int from1, int length1,
                             String s2, int from2, int length2)
            {
                var x1 = from1 == -1 ? XltCharBuffer.valueOf(s1) : XltCharBuffer.valueOf(s1).viewByLength(from1, length1);
                var x2 = from2 == -1 ? XltCharBuffer.valueOf(s2) :XltCharBuffer.valueOf(s2).viewByLength(from2, length2);
                var ss1 = from1 == -1 ? s1 : s1.substring(from1, from1 + length1);
                var ss2 = from2 == -1 ? s2 : s2.substring(from2, from2 + length2);

                assertEquals(ss1.indexOf(ss2), x1.indexOf(x2));
            }
        };

        f.test("", "");
        f.test("a", "a");
        f.test("abc", "a");
        f.test("abc", "b");
        f.test("abc", "c");
        f.test("abc", "abc");
        f.test("abc", "abd");

        f.test("0123456789", 0, 10, "0123456789", 0, 10);
        f.test("0123456789", 0, 10, "ABCDEFGHIJ", 0, 10);

        f.test("0123456789", 3, 4, "0123456789", 4, 4);
        f.test("0123456789", 3, 4, "ABCDEFGHIJ", 2, 6);
        f.test("0123456789", 2, 8, "456", 1, 2);

        f.test("0123456789", -1, 0, "0123456789", 4, 4);
        f.test("-0123456789-", 1, 10, "0123456789", -1, 0);
        f.test("0123456789", 3, 7, "ABCDEFGHIJ", -1, 0);
    }

    @Test
    public void indexOf_buffer_from()
    {
        var f = new Object()
        {
            // Test method to cover also substring views aka offset problems
            // To cover all options, we can disable the substring part with from == -1
            public void test(String s1, int from1, int length1,
                             String s2, int from2, int length2,
                             int from)
            {
                var x1 = from1 == -1 ? XltCharBuffer.valueOf(s1) : XltCharBuffer.valueOf(s1).viewByLength(from1, length1);
                var x2 = from2 == -1 ? XltCharBuffer.valueOf(s2) : XltCharBuffer.valueOf(s2).viewByLength(from2, length2);
                var ss1 = from1 == -1 ? s1 : s1.substring(from1, from1 + length1);
                var ss2 = from2 == -1 ? s2 : s2.substring(from2, from2 + length2);

                //System.out.format("[%s] %s / %s - %s / %s%n", from, ss1, ss1.indexOf(ss2, from), ss2, x1.indexOf(x2, from));
                assertEquals(ss1.indexOf(ss2, from), x1.indexOf(x2, from));
            }
        };
        f.test("0123456789", -1, -1, "0123456789", -1, -1, 0);
        IntStream.range(0, 10).forEach(i -> f.test("0123456789", -1, -1, "89", -1, -1, i));
        IntStream.range(0, 10).forEach(i -> f.test("0123456789", -1, -1, "A", -1, -1, i));
        IntStream.range(0, 10).forEach(i -> f.test("012345678B", 1, 9, "ABCDEFGHIJ", 1, 1, i));

        f.test("BAAABA", -1, 0, "BA", -1, 0, 3);
    }

    @Test
    public void hashCode_test() throws NoSuchFieldException, IllegalAccessException
    {
        Assert.assertEquals(new String("").hashCode(), XltCharBuffer.valueOf("").hashCode());
        Assert.assertEquals(new String(" ").hashCode(), XltCharBuffer.valueOf(" ").hashCode());
        Assert.assertEquals(new String("  ").hashCode(), XltCharBuffer.valueOf("  ").hashCode());
        Assert.assertEquals(new String("Foobar").hashCode(), XltCharBuffer.valueOf("Foobar").hashCode());
        Assert.assertEquals(new String("Das ist ein Test.").hashCode(), XltCharBuffer.valueOf("Das ist ein Test.").hashCode());
        Assert.assertEquals(new String("ist").hashCode(), XltCharBuffer.valueOf("Das ist ein Test.").substring(4, 7).hashCode());

        // we run in blocks of 8 because hashCode is vectorized

        // start at 0 for char buffer
        // less then one block
        Assert.assertEquals("0123".hashCode(), XltCharBuffer.valueOf("0123456789 abcdef").substring(0, 4).hashCode());
        // one block
        Assert.assertEquals("01234567".hashCode(), XltCharBuffer.valueOf("0123456789 abcdef").substring(0, 8).hashCode());
        // 1.5 blocks
        Assert.assertEquals("0123456789 a".hashCode(), XltCharBuffer.valueOf("0123456789 abcdef").substring(0, 12).hashCode());
        // 2 blocks
        Assert.assertEquals("0123456789 abc".hashCode(), XltCharBuffer.valueOf("0123456789 abcdef").substring(0, 14).hashCode());
        // more than 2
        Assert.assertEquals("0123456".hashCode(), XltCharBuffer.valueOf("0123456789 abcdef").substring(0, 7).hashCode());

        // start at > 0 for char buffer


        // get all kind of length variations set
        final String BASE = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

        for (int l = 0; l < BASE.length(); l++)
        {
            var b = BASE.substring(0, l);

            for (int i = 0; i < b.length(); i++)
            {
                var s1 = b.substring(0, i);
                Assert.assertEquals(s1.hashCode(), XltCharBuffer.valueOf(s1).hashCode());
                Assert.assertEquals(s1.hashCode(), XltCharBuffer.valueOf(b).substring(0, i).hashCode());

                var s2 = b.substring(i);
                Assert.assertEquals(s2.hashCode(), XltCharBuffer.valueOf(s2).hashCode());
                Assert.assertEquals(s2.hashCode(), XltCharBuffer.valueOf(b).substring(i).hashCode());
            }
        }

        // cached
        var s = XltCharBuffer.valueOf("foobar");
        Assert.assertEquals("foobar".hashCode(), s.hashCode());
        Assert.assertEquals("foobar".hashCode(), s.hashCode());

        // ensure that we remember it, needs private access
        final VarHandle handle = MethodHandles
            .privateLookupIn(XltCharBuffer.class, MethodHandles.lookup())
            .findVarHandle(XltCharBuffer.class, "hashCode", int.class);

        var s1 = XltCharBuffer.valueOf("foobar");
        var hashCode1 = (int) handle.get(s1);
        assertEquals(0, hashCode1);
        var hashCodeRead = s1.hashCode();
        var hashCode2 = (int) handle.get(s1);
        assertNotEquals(0, hashCode2);
        assertEquals("We have not cached our hashcode", hashCode2, hashCodeRead);
    }

    @Test
    public void compare()
    {
        {
            // basics
            assertEquals(0, XltCharBuffer.valueOf("").compareTo(XltCharBuffer.valueOf("")));
            assertEquals(-1, XltCharBuffer.valueOf("a").compareTo(XltCharBuffer.valueOf("b")));
            assertEquals(1, XltCharBuffer.valueOf("b").compareTo(XltCharBuffer.valueOf("a")));
        }
        {
            XltCharBuffer a = XltCharBuffer.valueOf("abcd").substring(0, 1);
            XltCharBuffer b = XltCharBuffer.valueOf("abcd").substring(1, 2);

            assertEquals(0, a.compareTo(a));
            assertEquals(-1, a.compareTo(b));
            assertEquals(1, b.compareTo(a));
        }
        {
            XltCharBuffer x = XltCharBuffer.valueOf("1234abcd1234").substring(4, 8);
            XltCharBuffer a = x.substring(0, 1);
            XltCharBuffer b = x.substring(1, 2);

            assertEquals(0, a.compareTo(a));
            assertEquals(-1, a.compareTo(b));
            assertEquals(1, b.compareTo(a));
        }
    }

    @Test
    public void endsWith()
    {
        var f = new Object()
        {
            // Test method to cover also substring views aka offset problems
            // To cover all options, we can disable the substring part with from == -1
            public void test(boolean exp, String s1, int from1, int length1,
                             String s2, int from2, int length2)
            {
                var x1 = from1 == -1 ? XltCharBuffer.valueOf(s1) : XltCharBuffer.valueOf(s1).viewByLength(from1, length1);
                var x2 = from2 == -1 ? XltCharBuffer.valueOf(s2) : XltCharBuffer.valueOf(s2).viewByLength(from2, length2);

                assertEquals(exp, x1.endsWith(x2));
            }
        };
        f.test(true, "", -1, 0, "", -1, 0);
        f.test(true, "A", -1, 0, "A", -1, 0);
        f.test(true, "AB", -1, 0, "AB", -1, 0);
        f.test(true, "AB", -1, 0, "B", -1, 0);
        f.test(false, "AB", -1, 0, "A", -1, 0);

        f.test(true, "AB-trtzui-CD", -1, 0, "##CD", 2, 2);
        f.test(false, "AB-trtzui-CD", -1, 0, "##AA", 2, 2);
        f.test(true, "AB-012345-CD", 2, 7, "000345000", 3, 3);
        f.test(false, "AB-012345-CD", 2, 7, "000345000", 3, 4);
        f.test(true, "AB-012345-CD", 2, 7, "345", -1, 4);

        // length already fails
        f.test(false, "AB--sadasdfsCD", 2, 7, "IUZTRTZUI", -1, 4);
    }

    @Test
    public void startsWith()
    {
        var f = new Object()
        {
            public void test(boolean exp, String s1, int from1, int length1,
                             String s2, int from2, int length2)
            {
                var x1 = from1 == -1 ? XltCharBuffer.valueOf(s1) : XltCharBuffer.valueOf(s1).viewByLength(from1, length1);
                var x2 = from2 == -1 ? XltCharBuffer.valueOf(s2) : XltCharBuffer.valueOf(s2).viewByLength(from2, length2);

                assertEquals(exp, x1.startsWith(x2));
            }
        };
        f.test(true, "", -1, 0, "", -1, 0);
        f.test(true, "A", -1, 0, "A", -1, 0);
        f.test(true, "ABC", -1, 0, "ABC", -1, 0);
        f.test(false, "ABC", -1, 0, "CBA", -1, 0);

        f.test(true, "---ABC--", 3, 3, "###ABC##", 3, 3);
        f.test(false, "---ABC--", 3, 3, "###66##", 3, 2);
    }

    @Test
    public void lastIndexOf()
    {
        var f = new Object()
        {
            public void test(int exp, String s1, int from1, int length1,
                             String s2, int from2, int length2)
            {
                var x1 = from1 == -1 ? XltCharBuffer.valueOf(s1) : XltCharBuffer.valueOf(s1).viewByLength(from1, length1);
                var x2 = from2 == -1 ? XltCharBuffer.valueOf(s2) : XltCharBuffer.valueOf(s2).viewByLength(from2, length2);

                var ss1 = from1 == -1 ? s1 : s1.substring(from1, from1 + length1);
                var ss2 = from2 == -1 ? s2 : s2.substring(from2, from2 + length2);

                assertEquals(exp, x1.lastIndexOf(x2));
                assertEquals(ss1.lastIndexOf(ss2), x1.lastIndexOf(x2));
            }
        };
        f.test(0, "", -1, 0, "", -1, 0);
        f.test(0, "A", -1, 0, "A", -1, 0);
        f.test(-1, "A", -1, 0, "B", -1, 0);
        f.test(4, "ABCABC", -1, 0, "B", -1, 0);
        f.test(5, "ABCABB", -1, 0, "B", -1, 0);
        f.test(3, "ABCABB", -1, 0, "AB", -1, 0);
        f.test(-1, "ABCABB", -1, 0, "AC", -1, 0);

        f.test(4, "--ABCABC", 2, 6, "B", -1, 0);
        f.test(5, "--ABCABB", 2, 6, "B", -1, 0);
        f.test(3, "--ABCABB", 2, 6, "AB", -1, 0);
        f.test(-1, "--ABCABB", 2, 6, "2B", -1, 0);

        f.test(4, "ABCABC", -1, 0, "-B-", 1, 1);
        f.test(5, "ABCABB", -1, 0, "-B-", 1, 1);
        f.test(3, "ABCABB", -1, 0, "-AB-", 1, 2);
        f.test(-1, "ABCABB", -1, 0, "-1B-", 1, 2);

        f.test(4, "-ABCABC-", 1, 6, "-B-", 1, 1);
        f.test(5, "-ABCABB-", 1, 6, "-B-", 1, 1);
        f.test(3, "-ABCABB-", 1, 6, "-AB-", 1, 2);
        f.test(-1, "-ABCABB-", 1, 6, "-1B-", 1, 2);
    }

    @Test
    public void lastIndexOf_from()
    {
        var f = new Object()
        {
            public void test(int exp, String s1, String s2, int from)
            {
                test(exp, s1, -1, -1, s2, -1, -1, from);
            }

            public void test(int exp, String s1, int from1, int length1,
                             String s2, int from2, int length2, int from)
            {
                var x1 = from1 == -1 ? XltCharBuffer.valueOf(s1) : XltCharBuffer.valueOf(s1).viewByLength(from1, length1);
                var x2 = from1 == -1 ? XltCharBuffer.valueOf(s2) : XltCharBuffer.valueOf(s2).viewByLength(from2, length2);

                var ss1 = from1 == -1 ? s1 : s1.substring(from1, from1 + length1);
                var ss2 = from2 == -1 ? s2 : s2.substring(from2, from2 + length2);

                assertEquals(exp, x1.lastIndexOf(x2, from));
                // String is our benchmark
                assertEquals(exp, ss1.lastIndexOf(ss2, from));
                assertEquals(ss1.lastIndexOf(ss2, from), x1.lastIndexOf(x2, from));
            }
        };

        //        assertEquals(0, "BA".lastIndexOf("BA", 0));
        f.test(-1, "A", "AA", 0);
        f.test(-1, "A", "BB", 0);

        f.test(0, "BA", "BA", 0);
        f.test(0, "BA", "BA", 1);
        f.test(0, "BA", "BA", 2);
        f.test(0, "BA", "BA", 3);

        f.test(-1, "AA", "BA", 0);
        f.test(-1, "AA", "BA", 1);
        f.test(-1, "AA", "BA", 2);
        f.test(-1, "AA", "BA", 3);

        f.test(4, "BAAABA", "BA", 4);
        f.test(0, "BAAABA", "BA", 3);

        f.test(0, "", "", 0);
        f.test(0, "A", "A", 0);
        f.test(-1, "A", "B", 0);
        f.test(-1, "", "B", 0);
        f.test(-1, "A", "BA", 0);
        f.test(-1, "AAAAA", "BA", 10);
        f.test(3, "AAABA", "BA", 10);
        f.test(3, "AAABA", "BA", 4);
        f.test(3, "AAABA", "BA", 3);
        f.test(-1, "AAABA", "BA", 2);

        f.test(-1, "0123456789", 1,  8, "A", 0, 1, 7);
        f.test(1, "0123456789", 1,  8, "23", 0, 2, 7);
        f.test(1, "0123456789", 1,  8, "-23-", 1, 2, 7);
        f.test(6, "0123-0123-0123", 1,  10, "23", 0, 2, 9);

    }

    @Test
    public void equalsTest()
    {
        // itself
        var foo = XltCharBuffer.valueOf("foo");
        assertTrue(foo.equals(foo));

        // null
        assertFalse(foo.equals(null));

        // other class
        assertFalse(foo.equals(""));

        // same content
        {
            var s1 = XltCharBuffer.valueOf("foo");
            var s2 = XltCharBuffer.valueOf("foo");
            assertTrue(s1.equals(s2));
            assertTrue(s2.equals(s1));
        }

        // different content
        var s3 = XltCharBuffer.valueOf("foo");
        var s4 = XltCharBuffer.valueOf("FOO");
        assertFalse(s3.equals(s4));
        assertFalse(s4.equals(s3));

        // different content due to length
        {
            var s1 = XltCharBuffer.valueOf("foo2");
            var s2 = XltCharBuffer.valueOf("foo");
            assertFalse(s1.equals(s2));
            assertFalse(s2.equals(s1));
        }

        // different content due different first char
        {
            var s1 = XltCharBuffer.valueOf("aoo");
            var s2 = XltCharBuffer.valueOf("boo");
            assertFalse(s1.equals(s2));
            assertFalse(s2.equals(s1));
        }

        var s5 = XltCharBuffer.valueOf("foobar");
        var s6 = XltCharBuffer.valueOf("megapp");
        assertFalse(s5.equals(s6));
        assertFalse(s6.equals(s5));

        var s7 = XltCharBuffer.valueOf("asdf asdf");
        var s8 = XltCharBuffer.valueOf("098769as8d98fa9s8d09f8a9s8f");
        assertFalse(s7.equals(s8));
        assertFalse(s8.equals(s7));

        // same content but another view
        var c1 = XltCharBuffer.valueOf("---foo--").viewByLength(3, 3);
        var c2 = XltCharBuffer.valueOf(" foobar--").viewByLength(1, 3);
        assertTrue(c1.equals(c1));
        assertTrue(c2.equals(c1));
    }

    @Test
    public void subSequence()
    {
        {
            var s = XltCharBuffer.valueOf("").subSequence(0, 0);
            assertEquals(XltCharBuffer.valueOf(""), s);
        }
        {
            var s = XltCharBuffer.valueOf("foobar").subSequence(0, 6);
            assertEquals(XltCharBuffer.valueOf("foobar"), s);
        }
        {
            var s = XltCharBuffer.valueOf("foobar").subSequence(1, 5);
            assertEquals(XltCharBuffer.valueOf("ooba"), s);
        }
        {
            var s = XltCharBuffer.valueOf("_foobar_").viewByLength(1, 5).subSequence(1, 5);
            assertEquals(XltCharBuffer.valueOf("ooba"), s);
        }
    }

    @Test
    public void compareTo()
    {
        {
            var s1 = XltCharBuffer.valueOf("");
            var s2 = XltCharBuffer.valueOf("");
            assertTrue(s1.compareTo(s2) == 0);
            assertTrue(s2.compareTo(s1) == 0);
        }
        {
            var s1 = XltCharBuffer.valueOf("abc");
            var s2 = XltCharBuffer.valueOf("def");
            assertTrue(s1.compareTo(s2) < 0);
            assertTrue(s2.compareTo(s1) > 0);
        }
        {
            var s1 = XltCharBuffer.valueOf("abc");
            var s2 = XltCharBuffer.valueOf("abc");
            assertTrue(s1.compareTo(s2) == 0);
            assertTrue(s2.compareTo(s1) == 0);
        }
        {
            var s1 = XltCharBuffer.valueOf("ZZakwjefkajskfjksjdkfjsakkfdjasfd");
            var s2 = XltCharBuffer.valueOf("Alsakdfisudifuaisudifouoisaudf");
            assertTrue(s1.compareTo(s2) > 0);
            assertTrue(s2.compareTo(s1) < 0);
        }
        {
            var s1 = XltCharBuffer.valueOf("aZZZZZa").viewByLength(1, 5);
            var s2 = XltCharBuffer.valueOf("aAAAAAa").viewByLength(1, 5);;
            assertTrue(s1.compareTo(s2) > 0);
            assertTrue(s2.compareTo(s1) < 0);
        }
    }

    @Test
    public void toDebugString()
    {
        assertEquals("Base=\nCurrent=\nfrom=0, length=0", XltCharBuffer.valueOf("").toDebugString());
        assertEquals("Base=foobar\nCurrent=foobar\nfrom=0, length=6", XltCharBuffer.valueOf("foobar").toDebugString());
        assertEquals("Base=foobar\nCurrent=ooba\nfrom=1, length=4", XltCharBuffer.valueOf("foobar").viewByLength(1, 4).toDebugString());
    }

    @Test
    public void split()
    {
        var f = new Object()
        {
            public void test(List<String> exp, String s, char splitChar)
            {
                var r = XltCharBuffer.valueOf(s).split(splitChar)
                    .stream()
                    .map(XltCharBuffer::toString)
                    .collect(Collectors.toList()).toArray();
                assertArrayEquals(exp.toArray(),  r);
            }
        };

        f.test(
               List.of(""),
               "", ',');
        f.test(
               List.of("a"),
               "a", ',');
        f.test(
               List.of("abc"),
               "abc", ',');
        f.test(
               List.of("a", "b", "c"),
               "a,b,c", ',');
        f.test(
               List.of("", "", ""),
               ",,", ',');
        f.test(
               List.of("", "b", "c"),
               ",b,c", ',');
        f.test(
               List.of("a", "b", ""),
               "a,b,", ',');
        f.test(
               List.of("a", "", "c"),
               "a,,c", ',');
        f.test(
               List.of("a", "", "cde"),
               "a,,cde", ',');
    }

    // Search contains testing
    @Test
    public void testContains_Found()
    {
        // 
        {
            XltCharBuffer haystack = XltCharBuffer.valueOf("abcdefg");
            XltCharBuffer needle = XltCharBuffer.valueOf("abc");
            int[] shiftTable = XltCharBuffer.createShiftTable(needle);
            assertTrue(XltCharBuffer.contains(haystack, needle, shiftTable));
        }
        // at beginning
        {
            XltCharBuffer haystack = XltCharBuffer.valueOf("abcdefg");
            XltCharBuffer needle = XltCharBuffer.valueOf("abc");
            int[] shiftTable = XltCharBuffer.createShiftTable(needle);
            assertTrue(XltCharBuffer.contains(haystack, needle, shiftTable));
        }
        // at end
        {
            XltCharBuffer haystack = XltCharBuffer.valueOf("abcdefg");
            XltCharBuffer needle = XltCharBuffer.valueOf("efg");
            int[] shiftTable = XltCharBuffer.createShiftTable(needle);
            assertTrue(XltCharBuffer.contains(haystack, needle, shiftTable));
        }
        // in middle
        {
            XltCharBuffer haystack = XltCharBuffer.valueOf("abcdefg");
            XltCharBuffer needle = XltCharBuffer.valueOf("cde");
            int[] shiftTable = XltCharBuffer.createShiftTable(needle);
            assertTrue(XltCharBuffer.contains(haystack, needle, shiftTable));
        }
    }

    @Test
    public void testContains_NotFound()
    {
        // longer needle
        {
            XltCharBuffer haystack = XltCharBuffer.valueOf("abcdefg");
            XltCharBuffer needle = XltCharBuffer.valueOf("xyz");
            int[] shiftTable = XltCharBuffer.createShiftTable(needle);

            assertFalse(XltCharBuffer.contains(haystack, needle, shiftTable));
        }

        // single needle
        {
            XltCharBuffer haystack = XltCharBuffer.valueOf("abcdefg");
            XltCharBuffer needle = XltCharBuffer.valueOf("x");
            int[] shiftTable = XltCharBuffer.createShiftTable(needle);

            assertFalse(XltCharBuffer.contains(haystack, needle, shiftTable));
        }
    }

    @Test
    public void testContains_EmptyNeedle()
    {
        XltCharBuffer haystack = XltCharBuffer.valueOf("abcdefg");
        XltCharBuffer needle = XltCharBuffer.valueOf("");
        int[] shiftTable = XltCharBuffer.createShiftTable(needle);

        assertTrue(XltCharBuffer.contains(haystack, needle, shiftTable));
    }

    @Test
    public void testContains_EmptyHaystack()
    {
        {
            XltCharBuffer haystack = XltCharBuffer.valueOf("");
            XltCharBuffer needle = XltCharBuffer.valueOf("a");
            int[] shiftTable = XltCharBuffer.createShiftTable(needle);

            assertFalse(XltCharBuffer.contains(haystack, needle, shiftTable));
        }
        {
            XltCharBuffer haystack = XltCharBuffer.valueOf("");
            XltCharBuffer needle = XltCharBuffer.valueOf("");
            int[] shiftTable = XltCharBuffer.createShiftTable(needle);

            assertTrue(XltCharBuffer.contains(haystack, needle, shiftTable));
        }
    }

    @Test
    public void testContains_NeedleEqualsHaystack()
    {
        {
            XltCharBuffer haystack = XltCharBuffer.valueOf("abc");
            XltCharBuffer needle = XltCharBuffer.valueOf("abc");
            int[] shiftTable = XltCharBuffer.createShiftTable(needle);

            assertTrue(XltCharBuffer.contains(haystack, needle, shiftTable));
        }
        {
            XltCharBuffer haystack = XltCharBuffer.valueOf("a");
            XltCharBuffer needle = XltCharBuffer.valueOf("a");
            int[] shiftTable = XltCharBuffer.createShiftTable(needle);

            assertTrue(XltCharBuffer.contains(haystack, needle, shiftTable));
        }
    }

    @Test
    public void testContains_NeedleLongerThanHaystack()
    {
        XltCharBuffer haystack = XltCharBuffer.valueOf("ab");
        XltCharBuffer needle = XltCharBuffer.valueOf("abc");
        int[] shiftTable = XltCharBuffer.createShiftTable(needle);

        assertFalse(XltCharBuffer.contains(haystack, needle, shiftTable));
    }

    @Test
    public void testContains_ContainsUnicode()
    {
        XltCharBuffer haystack = XltCharBuffer.valueOf("98asd (◕‿◕)  sa abǾcd");

        // yes
        {
            XltCharBuffer needle = XltCharBuffer.valueOf("bǾ");
            int[] shiftTable = XltCharBuffer.createShiftTable(needle);
            assertTrue(XltCharBuffer.contains(haystack, needle, shiftTable));
        }

        // no
        {
            XltCharBuffer needle = XltCharBuffer.valueOf("099876(◕‿◕)");
            int[] shiftTable = XltCharBuffer.createShiftTable(needle);
            assertFalse(XltCharBuffer.contains(haystack, needle, shiftTable));
        }
    }

    @Test
    public void testContains_SpecialCharacters()
    {
        XltCharBuffer haystack = XltCharBuffer.valueOf("abc$%#äöüß");
        XltCharBuffer needle = XltCharBuffer.valueOf("äöü");
        int[] shiftTable = XltCharBuffer.createShiftTable(needle);

        assertTrue(XltCharBuffer.contains(haystack, needle, shiftTable));
    }

    @Test
    public void testContains_RepeatedPattern()
    {
        XltCharBuffer haystack = XltCharBuffer.valueOf("ababababab");
        XltCharBuffer needle = XltCharBuffer.valueOf("baba");
        int[] shiftTable = XltCharBuffer.createShiftTable(needle);

        assertTrue(XltCharBuffer.contains(haystack, needle, shiftTable));
    }

    @Test
    public void testContains_CharOutsideShiftTable()
    {
        // Use a char with value > 255 (e.g., 0x400)
        char[] haystackArr = new char[] { 'a', (char) 0x400, 'b', 'c' };
        XltCharBuffer haystack = XltCharBuffer.valueOf(haystackArr);
        XltCharBuffer needle = XltCharBuffer.valueOf(new char[] { (char) 0x400 });
        int[] shiftTable = XltCharBuffer.createShiftTable(needle);

        assertTrue(XltCharBuffer.contains(haystack, needle, shiftTable));
    }

    @Test
    public void testContains_CharOutsideShiftTable_NotFound()
    {
        // Use a char with value > 255 that is not in haystack
        XltCharBuffer haystack = XltCharBuffer.valueOf("abc");
        XltCharBuffer needle = XltCharBuffer.valueOf(new char[] { (char) 0x400 });
        int[] shiftTable = XltCharBuffer.createShiftTable(needle);

        assertFalse(XltCharBuffer.contains(haystack, needle, shiftTable));
    }
}

