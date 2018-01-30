package net.bleujin;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.ExcludePackages;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectPackages({"net.bleujin.rcraken"})
@ExcludePackages("net.bleujin.rcraken.redis")
public class TestStdMethod {

}
