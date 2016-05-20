/*
    MIT License

    Copyright (c) 2016  Pierre-Yves Lapersonne (Twitter: @pylapp, Mail: pylapp(dot)pylapp(at)gmail(dot)com)

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */
// ✿✿✿✿ ʕ •ᴥ•ʔ/ ︻デ═一

package pylapp.smoothclicker.android.tools;

import org.junit.Test;

import pylapp.smoothclicker.android.AbstractTest;

import static junit.framework.Assert.fail;

/**
 * Class  to test the JsonFileParser
 *
 * @author pylapp
 * @version 1.0.0
 * @since 20/05/2016
 */
public class UtJsonFileParser extends AbstractTest {

    /**
     * Tests the getPointFromJsonFile method with a null context
     */
    @Test (expected = IllegalArgumentException.class)
    public void getPointFromJsonFile(){

        l(this, "@Test getPointFromJsonFile");

        try {
            JsonFileParser.instance.getPointFromJsonFile(null);
        } catch ( NotSuitableJsonPointsFileException nsjpfe ){
            fail(nsjpfe.getMessage());
        }

    }

    /**
     * Tests the parseConfigFile method with a null context
     */
    @Test (expected = IllegalArgumentException.class)
    public void parseConfigFile(){

        l(this, "@Test parseConfigFile");

        try {
            JsonFileParser.instance.parseConfigFile(null);
        } catch ( NotSuitableJsonConfigFileException nsjcfe ){
            fail(nsjcfe.getMessage());
        }

    }

}
