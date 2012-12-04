package hpi;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.Proc;
import hudson.Launcher.ProcStarter;


import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class BtrfsWorkspacePlugin extends BuildWrapper {
	private String baseVolume;
	private boolean destroyAfterBuild;
	private String[] copiedFiles;

	private static final Logger log = Logger.getLogger(BtrfsWorkspacePlugin.class.getName()); 

	@DataBoundConstructor
    public BtrfsWorkspacePlugin(String baseVolume, boolean destroyAfterBuild) {
		this.baseVolume = baseVolume;
		this.destroyAfterBuild = destroyAfterBuild;
    }

    public String getBaseVolume() {
		return baseVolume;
	}

	public boolean getDestroyAfterBuild() {
		return destroyAfterBuild;
	}

	@Override
	public Environment setUp(AbstractBuild build, final Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException 
	{
		FilePath projectWorkspace = build.getWorkspace();

		Hudson hudson = Hudson.getInstance();
		FilePath hudsonRoot = hudson.getRootPath();

		FilePath copyFrom = new FilePath(hudsonRoot, baseVolume);

        //maybe a leftover
        ProcStarter ps = launcher.new ProcStarter();
        ps.cmds("btrfs", "subvolume", "delete", projectWorkspace.toString());
        Proc proc = launcher.launch(ps);
        proc.join();

        //delete the directory
        projectWorkspace.deleteRecursive();

        ps = launcher.new ProcStarter();
        ps.cmds("btrfs", "subvolume", "snapshot", copyFrom.toString(), projectWorkspace.toString());
        proc = launcher.launch(ps);
        int retcode = proc.join();

		return new Environment() {
			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener)
					throws IOException, InterruptedException {
				if (destroyAfterBuild) {
					FilePath projectWorkspace = build.getWorkspace();

                    ProcStarter ps = launcher.new ProcStarter();
                    ps.cmds("btrfs", "subvolume", "delete", projectWorkspace.toString());
                    Proc proc = launcher.launch(ps);
                    proc.join();
				}
				return true;
			}
		};
	}

    @Override
    public Environment setUp(Build build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        return setUp(build, launcher, listener);
    }

    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {
        public DescriptorImpl() {
            super(BtrfsWorkspacePlugin.class);
        }

        public ListBoxModel doFillBaseVolumeItems()
        {
            ListBoxModel items = new ListBoxModel();
            items.add("hurr");
            items.add("durr");
            return items;
        }

        public FormValidation doCheckFolderPath(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            if (0 == value.length()) {
            	return FormValidation.error("Cannot be empty");
            }
        	return FormValidation.ok();
        }

        @Override
        public String getDisplayName() {
            return "Clone workspace from a btrfs subvolume.";
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

    }
}
