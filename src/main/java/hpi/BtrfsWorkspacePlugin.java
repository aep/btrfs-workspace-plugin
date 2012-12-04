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
        launcher.launch().cmds("btrfs", "subvolume", "delete", projectWorkspace.toString()).masks(true).join();

        //delete the directory
        projectWorkspace.deleteRecursive();

        int retcode = launcher.launch().cmds(
                "btrfs", "subvolume", "snapshot", copyFrom.toString(), projectWorkspace.toString()
                ).stdout(listener).join();
        if (retcode != 0) {
            return null;
        }

        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener)
            throws IOException, InterruptedException {
            if (destroyAfterBuild) {
                FilePath projectWorkspace = build.getWorkspace();

                launcher.launch().cmds("btrfs", "subvolume", "delete", projectWorkspace.toString()).join();
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

        public FormValidation doCheckBaseVolume(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {

            Hudson hudson = Hudson.getInstance();
            FilePath hudsonRoot = hudson.getRootPath();
            FilePath fp = new FilePath(hudsonRoot, value);

            try {
                if (!fp.exists())
                    return FormValidation.error("Does not exist.");
            } catch (java.lang.InterruptedException e) {
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
