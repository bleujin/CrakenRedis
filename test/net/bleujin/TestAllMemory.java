package net.bleujin;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

import net.bleujin.rcraken.StreamTest;
import net.bleujin.rcraken.mapdb.TestMapDB;

@RunWith(JUnitPlatform.class)
@SelectClasses({StreamTest.class, TestMapDB.class})
public class TestAllMemory {

}
