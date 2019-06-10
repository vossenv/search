package com.dm.search.test;


import com.dm.search.preprocessor.SLProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TestSLProcessor {

    private SLProcessor slp;

    TestSLProcessor() {
        slp = new SLProcessor();
        slp.setMinFuzzyLen(0);
    }

    @Test
    void TestSimple() throws Exception {

        String q0 = slp.format("");
        String q1 = slp.format("a b");
        String q2 = slp.format("a b c c");
        String q3 = slp.format("c a b c");
        String q4 = slp.format("a   b     c");
        String q5 = slp.format("    a   b     c ");
        String q6 = slp.format("    ");

        assertEquals(q0, "*");
        assertEquals(q1, "a~ b~");
        assertEquals(q2, "a~ b~ c~ c~");
        assertEquals(q3, "c~ a~ b~ c~");
        assertEquals(q4, "a~ b~ c~");
        assertEquals(q5, "a~ b~ c~");
        assertEquals(q6, "*");
    }

    @Test
    void TestQuotes() throws Exception {

        String q1 = slp.format("\"a b\"");
        String q2 = slp.format("\"a b\" c d");
        String q3 = slp.format("\" \\\" test quoted term in quotes \\\" \"");
        String q4 = slp.format("\" a \\\"");
        String q5 = slp.format("\" a and b \" \"c and d\"");
        String q6 = slp.format("\" a and  \"c and d\"");
        String q7 = slp.format("\" a and  \" \" c and d\"");

        assertEquals(q1, "\"a b\"~");
        assertEquals(q2, "\"a b\"~ c~ d~");
        assertEquals(q3, "\" \\\" test quoted term in quotes \\\" \"~");
        assertEquals(q4, "\"~ a~ \\\"~");
        assertEquals(q5, "\" a and b \"~ \"c and d\"~");
        assertEquals(q6, "\" a and  \"c and d\"~");
        assertEquals(q7, "\" a and  \"~ \" c and d\"~");
    }

    @Test
    void TestAndOrOr() throws Exception {

        String q0 = slp.format("a AND b");
        String q1 = slp.format("a AND b AND c");
        String q2 = slp.format("AND a AND b AND c");
        String q5 = slp.format("a AND bANDc ANDd");
        String q6 = slp.format("a ANDb c");
        String q7 = slp.format("OR a OR b OR c OR");

        //    assertThrows(ParseException.class, () -> slp.format("AND AND AND"));
        //     assertThrows(ParseException.class, () -> slp.format("ANDANDAND"));

        assertEquals(q0, "a~ AND b~");
        assertEquals(q1, "a~ AND b~ AND c~");
        assertEquals(q2, "a~ AND b~ AND c~");
        assertEquals(q5, "a~ AND bANDc~ ANDd~");
        assertEquals(q5, "a~ AND bANDc~ ANDd~");
        assertEquals(q7, "a~ OR b~ OR c~");

    }


    @Test
    void TestKeyword() throws Exception {

        String q0 = slp.format("author : a");
        String q1 = slp.format("author:a");
        String q2 = slp.format("author :a");
        String q3 = slp.format("author :a ");
        String q5 = slp.format("author : content : a");
        String q7 = slp.format(": a ");
        String q8 = slp.format("b :: a ");
        String q9 = slp.format(": b : a : ");
        String q11 = slp.format("a : \"x y z\"");
        String q12 = slp.format("\"a b c\" : \"x y z\"");
        String q13 = slp.format("\"a b c\" : x");
        String q15 = slp.format("a : b : c : d :");
        String q16 = slp.format(": a : b : c : d :");

        assertEquals(q0, "author:a~");
        assertEquals(q1, "author:a~");
        assertEquals(q2, "author:a~");
        assertEquals(q3, "author:a~");
        assertEquals(q5, "author:content~ : a~");
        assertEquals(q7, ": a~");
        assertEquals(q8, "b::~ a~");
        assertEquals(q9, ": b:a~ :");
        assertEquals(q11, "a:\"x y z\"~");
        assertEquals(q12, "\"a b c\":\"x y z\"~");
        assertEquals(q13, "\"a b c\":x~");
        assertEquals(q15, "a:b~ : c:d~ :");
        assertEquals(q16, ": a:b~ : c:d~ :");

    }

    @Test
    void TestFull() throws Exception {

        String q0 = slp.format("\"a a\" \"a a\" \"b b\"");
        String q1 = slp.format("\"a a\" AND \"b b\" AND \"c c\"");
        String q2 = slp.format("OR \"a a\" AND b OR \"c c\"");
        String q3 = slp.format("(this OR that) (and this) AND abcd");
        String q4 = slp.format("((a nested) group)");
        String q5 = slp.format("((\"a nested\" planet) group)");
        String q6 = slp.format("this OR (a:keyword in)");
        String q7 = slp.format("this OR (a : keyword in)");
        String q8 = slp.format("this OR (a : \"keyword in\" here)");

        assertEquals(q0, "\"a a\"~ \"a a\"~ \"b b\"~");
        assertEquals(q1, "\"a a\"~ AND \"b b\"~ AND \"c c\"~");
        assertEquals(q2, "\"a a\"~ AND b~ OR \"c c\"~");
        assertEquals(q3, "(this~ OR that~) (and~ this~) AND abcd~");
        assertEquals(q4, "((a~ nested~) group~)");
        assertEquals(q5, "((\"a nested\"~ planet~) group~)");
        assertEquals(q6, "this~ OR (a:keyword~ in~)");
        assertEquals(q7, "this~ OR (a:keyword~ in~)");
        assertEquals(q8, "this~ OR (a:\"keyword in\"~ here~)");

    }

    @Test
    void TestSpecialChars() {

        // assertThrows(ParseException.class, () -> slp.format("!# $() #&(^ AND &^%????\\\\\\ "));
//        assertThrows(ParseException.class, () -> slp.format("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\""));
//        assertThrows(ParseException.class, () -> slp.format("~!#$%^&()_+-/-+<>?:{}|\\]`[';/.,']"));
//        assertThrows(ParseException.class, () -> slp.format("\"\\\\\""));
//        assertThrows(ParseException.class, () -> slp.format("\""));
//        assertThrows(ParseException.class, () -> slp.format(":"));
//        assertThrows(ParseException.class, () -> slp.format(":::: :"));

    }

}