package net.bleujin;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.bleujin.rcraken.ReadMe;
import net.bleujin.rcraken.ReadNodeTest;
import net.bleujin.rcraken.tbase.TestBaseCrakenRedis;

@RunWith(JUnitPlatform.class)
@SelectPackages({"net.bleujin.rcraken"})
public class TestAllRcraken {

}
