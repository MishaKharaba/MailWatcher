package com.eleks.mailwatcher;

import com.eleks.mailwatcher.model.Utils;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class MailFilterUnitTest {

    @Test
    public void splitAddressesTest() {
        String[] mails = Utils.splitMalList("mail1@server.com mail2@server.com, mail3@server.com; mail4@server.com");
        String[] expected = {"mail1@server.com", "mail2@server.com", "mail3@server.com", "mail4@server.com"};
        assertArrayEquals(expected, mails);
    }

    @Test
    public void checkSubjectTest(){
        Pattern p = Utils.makePattern("test * string");
        Matcher m = p.matcher("test  string");
        assertTrue(m.matches());
        m = p.matcher("Test  String");
        assertTrue(m.matches());
        m = p.matcher("Test    String");
        assertTrue(m.matches());
        m = p.matcher("Test any  String");
        assertTrue(m.matches());

        //
        p = Utils.makePattern("* test * string");
        m = p.matcher(" test  string");
        assertTrue(m.matches());
        m = p.matcher("some test  string");
        assertTrue(m.matches());

        //
        p = Utils.makePattern(" test * string*");
        m = p.matcher(" test  string");
        assertTrue(m.matches());
        m = p.matcher("some test  string");
        assertFalse(m.matches());
        m = p.matcher(" test  strings");
        assertTrue(m.matches());
        m = p.matcher(" test  string xxx");
        assertTrue(m.matches());
    }
}
