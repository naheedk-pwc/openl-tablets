package org.openl.rules.webstudio.web.repository;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.openl.commons.web.jsf.FacesUtils;
import org.openl.rules.webstudio.web.repository.tree.TreeNode;
import org.richfaces.event.ItemChangeEvent;

@ManagedBean
@ViewScoped
public class ProductionRepositoriesTreeController {
    @ManagedProperty(value="#{repositorySelectNodeStateHolder}")
    private RepositorySelectNodeStateHolder repositorySelectNodeStateHolder;

    @ManagedProperty(value="#{productionRepositoriesTreeState}")
    private ProductionRepositoriesTreeState productionRepositoriesTreeState;

    public ProductionRepositoriesTreeState getProductionRepositoriesTreeState() {
        return productionRepositoriesTreeState;
    }

    public void setProductionRepositoriesTreeState(ProductionRepositoriesTreeState productionRepositoriesTreeState) {
        this.productionRepositoriesTreeState = productionRepositoriesTreeState;
    }

    /**
     * Gets all rules projects from a rule repository.
     *
     * @return list of rules projects
     */
    public List<TreeNode> getRulesProjects() {
        return repositorySelectNodeStateHolder.getSelectedNode().getChildNodes();
    }

    public String selectRulesProject() {
        String projectName = FacesUtils.getRequestParameter("projectName");

        if (repositorySelectNodeStateHolder.getSelectedNode().getType().equals(UiConst.TYPE_PRODUCTION_REPOSITORY)) {
            for (TreeNode node : repositorySelectNodeStateHolder.getSelectedNode().getChildNodes()) {
                if (node.getName().equals(projectName)) {
                    repositorySelectNodeStateHolder.setSelectedNode(node);
                }
            }
        }

        return null;
    }
    
    public void tabChange(ItemChangeEvent event) {
        String newTab = event.getNewItemName();

        switch (newTab) {
            case "Production":
                productionRepositoriesTreeState.initTree();
                break;               
            default: 
                break;
        }
    
    }
    
    public RepositorySelectNodeStateHolder getRepositorySelectNodeStateHolder() {
        return repositorySelectNodeStateHolder;
    }

    public void setRepositorySelectNodeStateHolder(RepositorySelectNodeStateHolder repositorySelectNodeStateHolder) {
        this.repositorySelectNodeStateHolder = repositorySelectNodeStateHolder;
    }
    
    public String refreshTree() {
        productionRepositoriesTreeState.invalidateTree();

        return null;
    }
}
