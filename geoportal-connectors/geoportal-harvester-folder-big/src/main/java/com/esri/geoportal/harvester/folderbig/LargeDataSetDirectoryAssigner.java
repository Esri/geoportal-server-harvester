/**
 * This software is Copyright © 2015 The Regents of the University of California. All Rights Reserved.
 *
 * Permission to copy, modify, and distribute this software and its documentation for educational, research and non-profit purposes, without fee, and without a written agreement is hereby granted, provided that the above copyright notice, this paragraph and the following three paragraphs appear in all copies.
 *
 * Permission to make commercial use of this software may be obtained by contacting:
 *
 * Technology Transfer Office 9500 Gilman Drive, Mail Code 0910 University of California La Jolla, CA 92093-0910 (858) 534-5815 invent@ucsd.edu
 *
 * This software program and documentation are copyrighted by The Regents of the University of California. The software program and documentation are supplied “as is”, without any accompanying services from The Regents. The Regents does not warrant that the operation of the program will be uninterrupted or error-free. The end-user understands that the program was developed for research purposes and is advised not to rely exclusively on the program for any reason.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN “AS IS” BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

 */

package com.esri.geoportal.harvester.folderbig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *  * Created by bozyurt on 12/1/15.
 */
public class LargeDataSetDirectoryAssigner {
    private int maxNumOfFilesPerDir = 2000;
    private int fileCountInterval = 100; // every 100 internal counts count the number of files in the last dir;
    private int totalFileCount = 0;
    private int lastDirNo = 1;
    private File rootDir;
    private int dirFileCount = 0;
    private static Map<String, LargeDataSetDirectoryAssigner> instanceMap = new HashMap<String, LargeDataSetDirectoryAssigner>();
    private static String format ="%05d";

    public synchronized static LargeDataSetDirectoryAssigner getInstance(String rootDir) {
        return getInstance(rootDir, 2000);
    }

    public synchronized static LargeDataSetDirectoryAssigner getInstance(String rootDir, int maxNumOfFilesPerDir) {

        return getInstance(rootDir, maxNumOfFilesPerDir, false);
    }

    public synchronized static LargeDataSetDirectoryAssigner getInstance(String rootDir, int maxNumOfFilesPerDir,boolean resetCount) {
        LargeDataSetDirectoryAssigner instance = instanceMap.get(rootDir);

        if (instance == null || resetCount) {
            instance = new LargeDataSetDirectoryAssigner(rootDir, maxNumOfFilesPerDir);
            instanceMap.put(rootDir, instance);
        }
        return instance;
    }

    private LargeDataSetDirectoryAssigner(String rootDir, int maxNumOfFilesPerDir) {
        this.rootDir = new File(rootDir);
        this.maxNumOfFilesPerDir = maxNumOfFilesPerDir;
        if (this.maxNumOfFilesPerDir > this.fileCountInterval) this.fileCountInterval = this.maxNumOfFilesPerDir;
       // Assertion.assertTrue(this.rootDir.isDirectory());
        File firstDir = new File(rootDir, String.format(format, lastDirNo));
        firstDir.mkdir();
       // Assertion.assertTrue(firstDir.isDirectory(), "Cannot create dir:" + firstDir);
    }

    /**
     * not synchronized intentionally to be used in a single thread
     *
     * @return
     */
    public File getNextDirPath() {
        dirFileCount++;
        ++totalFileCount;
        if (dirFileCount > maxNumOfFilesPerDir) {
            ++lastDirNo;
            dirFileCount = 1;
            File currentDir = new File(rootDir, String.format(format, lastDirNo));
             currentDir.mkdir();
          //  Assertion.assertTrue(currentDir.isDirectory(),"Cannot create dir:" + currentDir);
        }
        return new File(rootDir, String.format(format, lastDirNo));
    }



    public static void main(String[] args) {
        LargeDataSetDirectoryAssigner instance = LargeDataSetDirectoryAssigner.getInstance("/tmp/waf/Data.gov",50);

        for (int i = 1; i <= 213; i++) {
            System.out.println(i + ": " + instance.getNextDirPath());
        }
    }

}
