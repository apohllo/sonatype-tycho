package org.apache.maven.surefire.testng;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import java.io.File;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Map;

import org.apache.maven.surefire.report.ReporterManager;
import org.apache.maven.surefire.testset.SurefireTestSet;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.suite.AbstractDirectoryTestSuite;
import org.apache.maven.surefire.suite.SurefireTestSuite;
import org.testng.internal.ClassHelper;

public class TestNGAdapter implements SurefireTestSuite {
        private SurefireTestSuite suite;

        public TestNGAdapter( File basedir, ArrayList includes, ArrayList excludes){
                suite = new TestNGDirectoryTestSuite(basedir,includes,excludes,basedir.getAbsolutePath(),
                        "5.14.2","",new Properties(),basedir);
        }

        public TestNGAdapter( File basedir, String suiteXmlFile){
                suite = new TestNGXmlTestSuite(new File[]{new File(suiteXmlFile)},basedir.getAbsolutePath(),
                        "5.14.2","",new Properties(),basedir);
        }

        protected SurefireTestSet createTestSet( Class testClass, ClassLoader classLoader ){
                return new TestNGTestSet( testClass );
        }

        public void execute( String testSetName, ReporterManager reporterManager, ClassLoader classLoader )
                throws ReporterException, TestSetFailedException{
                suite.execute(testSetName, reporterManager, classLoader);
        }

        public void execute( ReporterManager reporterManager, ClassLoader classLoader )
                throws ReporterException, TestSetFailedException{
                suite.execute(reporterManager, classLoader);
        }

        /*
         * This is overriden to allow the suite object find the class when the tests
         * are run via xml suite file.
         */
        public Map locateTestSets( ClassLoader classLoader )
                        throws TestSetFailedException{
                ClassHelper.addClassLoader(classLoader);
                return suite.locateTestSets(classLoader);
        }

        public int getNumTests() {
                return suite.getNumTests();
        }

}

