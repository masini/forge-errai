package org.jboss.errai.forge;

import org.jboss.errai.forge.facet.ErraiInstalled;
import org.jboss.errai.forge.facet.ui.ErraiUIFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.*;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * @author pslegr
 */
@Alias("errai-ui")
@RequiresProject
public class ErraiUIPlugin implements Plugin {

    private final Project project;
    private final Event<InstallFacets> installFacets;

    @Inject
    private ShellPrompt prompt;


    public boolean isModuleInstalled() {
		return ErraiInstalled.getInstance().isInstalled();
	}

	public void setModuleInstalled(boolean isModuleInstalled) {
		ErraiInstalled.getInstance().setInstalled(isModuleInstalled);
	}

	@Inject
    public ErraiUIPlugin(final Project project, final Event<InstallFacets> event) {
        this.project = project;
        this.installFacets = event;
    }

    public Project getProject() {
		return project;
	}

	public Event<InstallFacets> getInstallFacets() {
		return installFacets;
	}

	@DefaultCommand
    public void status(final PipeOut out) {
        if (project.hasFacet(ErraiUIFacet.class)) {
            out.println("Errai-UI is installed.");
        } else {
            out.println("Errai-UI is not installed. Use 'errai-ui setup' to get started.");
        }
    }

    // confirmed working
    @Command("setup")
    public void setup(final PipeOut out) {
        if (!project.hasFacet(ErraiUIFacet.class)) {
            installFacets.fire(new InstallFacets(ErraiUIFacet.class));
        }
        if (project.hasFacet(ErraiUIFacet.class)) {
            ShellMessages.success(out, "ErraiUIFacet is configured.");
        }
    	
		this.setModuleInstalled(false);
		
		//TODO implement here logic for istalling only one facet at the time, once one facet is isntalled the others
		// won't be used 
		
    	
    }

    @Command("help")
    public void exampleDefaultCommand(@Option final String opt, final PipeOut pipeOut) {
        pipeOut.println(ShellColor.BLUE, "Use the following commands:");
        pipeOut.println(ShellColor.MAGENTA, "\t\tsetup: prepare the project for errai-ui");
        pipeOut.println(ShellColor.MAGENTA, "\t\tcomponent: create a CRUD interface of the giving JPA entity");
        //pipeOut.println(ShellColor.BLUE, "add-binding: prepare the project for errai-ui");
    }

}