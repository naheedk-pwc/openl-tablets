package org.openl.rules.rest;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.openl.rules.ui.ProjectModel;
import org.openl.rules.ui.WebStudio;
import org.openl.rules.webstudio.WebStudioFormats;
import org.openl.rules.webstudio.web.admin.ProjectsInHistoryController;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.rules.workspace.lw.impl.FolderHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Path("/history/")
@Produces(MediaType.APPLICATION_JSON)
public class ProjectHistoryService {

    @Autowired
    private HttpSession httpSession;

    @GET
    @Path("project")
    public List<ProjectHistoryItem> getProjectHistory() {
        WebStudio webStudio = WebStudioUtils.getWebStudio(httpSession);
        ProjectModel model = webStudio.getModel();
        String projectHistoryPath = Paths
            .get(webStudio.getWorkspacePath(),
                model.getProject().getFolderPath(),
                FolderHelper.HISTORY_FOLDER,
                model.getModuleInfo().getName())
            .toString();
        File dir = new File(projectHistoryPath);
        String[] historyListFiles = dir.list();
        if (historyListFiles == null || historyListFiles.length == 1) {
            return Collections.emptyList();
        }
        Arrays.sort(historyListFiles, Comparator.reverseOrder());
        List<ProjectHistoryItem> collect = Arrays.stream(historyListFiles)
            .map(this::createItem)
            .collect(Collectors.toList());
        ProjectHistoryItem revisionVersion = collect.remove(0);
        collect.add(revisionVersion);
        return collect;
    }

    private ProjectHistoryItem createItem(String name) {
        String[] parts = name.split("_");
        String type = parts.length == 2 ? parts[1] : null;
        String version = parts[0];
        SimpleDateFormat formatter = new SimpleDateFormat(WebStudioFormats.getInstance().dateTime());
        String modifiedOn;
        try {
            long time = Long.parseLong(version );
            modifiedOn = formatter.format(new Date(time));
        } catch (NumberFormatException e) {
            modifiedOn = version;
        }

        return new ProjectHistoryItem(name, modifiedOn, type);
    }

    @POST
    @Path("restore")
    public void restore(String versionToRestore) throws Exception {
        ProjectModel model = WebStudioUtils.getWebStudio(httpSession).getModel();
        if (model != null) {
            ProjectsInHistoryController.restore(model, versionToRestore);
        }
    }
}
