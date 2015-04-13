package org.paleozogt.gradle.compress

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.BuildLauncher;

import org.junit.Test
import org.junit.BeforeClass
import static org.junit.Assert.*

import java.nio.file.Files

import org.apache.commons.io.FileUtils

import org.slf4j.Logger
import org.gradle.api.logging.Logging

class CommonsCompressPluginTest {
    protected static File testDataDir;
    protected static File tmpDir;

    private Logger logger= Logging.getLogger(getClass());

    @BeforeClass
    public static void setup() {
        // TODO: how to ask gradle what the build dir is?
        testDataDir= new File("build/testData");
        FileUtils.deleteDirectory(testDataDir);
        testDataDir.mkdirs();

        tmpDir= new File("build/tmp/test");
        FileUtils.deleteDirectory(tmpDir);
        tmpDir.mkdirs();

        generateTestData();
    }

    @Test
    public void applyTest() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'org.paleozogt.commons-compress'
    }

    @Test
    public void zipTask() {
        Project project = ProjectBuilder.builder().build()
        def task = project.task('testTask', type: CommonsZipTask) {
            from testDataDir
            into tmpDir
        }
        assertTrue(task instanceof CommonsZipTask)
        task.execute();
    }

    @Test
    public void unzipTask() {
        Project project = ProjectBuilder.builder().build()
        def task = project.task('testTask', type: CommonsUnzipTask)
        assertTrue(task instanceof CommonsUnzipTask)
    }

    @Test
    public void sampleBuildTest() {
        runBuild(new File("src/test/resources/test-build"))
    }

    protected static void generateTestData() {
        new File(testDataDir, "foobar.txt").write("this is a test");

        File subdir= new File(testDataDir, "sub");
        subdir.mkdirs();
        new File(subdir, "foobaz.dat").write("this is also a test");

        File symlink= new File(testDataDir, "sym");
        Files.createSymbolicLink(symlink.toPath(), new File(subdir.getName()).toPath());
    }

    protected void runBuild(File path, String target = "build") {
        GradleConnector connector = GradleConnector.newConnector()
        connector.forProjectDirectory(path)
        ProjectConnection connection = connector.connect()
        try {
            BuildLauncher launcher = connection.newBuild()
            launcher.forTasks(target)
            launcher.run()
        } finally {
            connection.close()
        }
    }
}
