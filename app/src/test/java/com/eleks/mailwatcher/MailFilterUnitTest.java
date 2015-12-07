package com.eleks.mailwatcher;

import com.eleks.mailwatcher.model.Utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class MailFilterUnitTest {

    @Test
    public void SplitAddressesTest() {
        String[] mails = Utils.splitMalList("mail1@server.com mail2@server.com, mail3@server.com; mail4@server.com");
        String[] expected = {"mail1@server.com", "mail2@server.com", "mail3@server.com", "mail4@server.com"};
        assertArrayEquals(expected, mails);
    }
}
