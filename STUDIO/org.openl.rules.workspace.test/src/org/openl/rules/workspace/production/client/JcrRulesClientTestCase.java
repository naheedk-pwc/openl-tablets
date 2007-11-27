package org.openl.rules.workspace.production.client;

import junit.framework.TestCase;
import org.openl.SmartProps;
import org.openl.rules.workspace.TestHelper;
import static org.openl.rules.workspace.TestHelper.*;
import org.openl.rules.workspace.abstracts.Project;
import org.openl.rules.workspace.deploy.DeployID;
import org.openl.rules.workspace.deploy.DeploymentException;
import org.openl.rules.workspace.deploy.impl.jcr.JcrProductionDeployer;
import org.openl.rules.workspace.mock.MockProject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;
import java.util.Properties;

public class JcrRulesClientTestCase extends TestCase{
    private JcrRulesClient instance;
    private Project project;

    private static final String FOLDER1 = "folder1";
    private static final String FILE1_1 = "file1_1";
    private static final String FILE1_2 = "file1_2";
    private static final String FOLDER2 = "folder2";
    private static final String PROJECT_NAME = "project";
    private static final String PROJECT_NAME2 = "project2";

    @Override
    protected void setUp() throws Exception {
        ensureTestFolderExistsAndClear();

        instance = new JcrRulesClient();

        project = makeProject();
    }

    private Project makeProject() {
        return (Project)
                new MockProject(PROJECT_NAME).
                        addFolder(FOLDER1)
                            .addFile(FILE1_1).setInputStream(new ByteArrayInputStream(new byte[10])).up()
                            .addFile(FILE1_2).setInputStream(new ByteArrayInputStream(new byte[20])).up()
                        .up()
                            .addFolder(FOLDER2)
                        .up();
    }

    private Project makeProject2() {
        return (Project)
                new MockProject(PROJECT_NAME2).
                        addFolder(FOLDER1)
                            .addFile(FILE1_2).setInputStream(new ByteArrayInputStream(new byte[42])).up()
                        .up();
    }

    public void testFetchProject() throws Exception {
        JcrProductionDeployer deployer = getDeployer();
        DeployID id = deployer.deploy(Collections.singletonList(project));

        File destDir = new File(TestHelper.FOLDER_TEST);
        TestHelper.clearDirectory(destDir);

        instance.fetchProject(id, destDir);

        File file1_2 = new File(destDir, PROJECT_NAME + "/" + FOLDER1 + "/" + FILE1_2);
        assertEquals(20L, file1_2.length());
    }

    public void testFetchRedeployedProject() throws Exception {
        JcrProductionDeployer deployer = getDeployer();
        DeployID id = deployer.deploy(Collections.singletonList(project));
        deployer.deploy(id, Collections.singletonList(makeProject2()));

        File destDir = new File(TestHelper.FOLDER_TEST);
        TestHelper.clearDirectory(destDir);

        instance.fetchProject(id, destDir);

        File file1_2 = new File(destDir, PROJECT_NAME2 + "/" + FOLDER1 + "/" + FILE1_2);
        assertEquals(42L, file1_2.length());
    }

    private JcrProductionDeployer getDeployer() throws DeploymentException {
        Properties properties = new Properties();
        properties.put(JcrProductionDeployer.PROPNAME_ZIPFOLDER, FOLDER_TEST);
        return new JcrProductionDeployer(getWorkspaceUser(), new SmartProps(properties));
    }
}
