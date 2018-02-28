package hudson.plugins.mantis;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.SCM;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

/**
 * Parses changelog for Mantis issue IDs and updates Mantis issues.
 *
 * @author Seiji Sogabe
 */
public final class MantisIssueUpdater extends Recorder implements SimpleBuildStep {

    private boolean keepNotePrivate;

    private boolean recordChangelog;
    private String version;

    private SCM scm;

    @DataBoundConstructor
    public MantisIssueUpdater() {
    }

    @DataBoundSetter
    public void setVersion(String version) {
    	this.version = version;
    }

    @DataBoundSetter
    public void setKeepNotePrivate(boolean keepNotePrivate) {
    	this.keepNotePrivate = keepNotePrivate;
    }
    
    @DataBoundSetter
    public void setRecordChangelog(boolean recordChangelog) {
    	this.recordChangelog = recordChangelog;
    }

    @DataBoundSetter
    public void setSCM(SCM scm) {
    	this.scm = scm;
    }

    public boolean getKeepNotePrivate() {
        return keepNotePrivate;
    }

    public boolean getRecordChangelog() {
        return recordChangelog;
    }

    public String getVersion() {
        return version;
    }

    public SCM getSCM() {
        return scm;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public void perform(final Run<?, ?> run, FilePath workspace, final Launcher launcher, final TaskListener listener)
            throws InterruptedException, IOException {

        if (run instanceof MatrixRun) {
            return;
        } else if (run instanceof AbstractBuild<?, ?>) {
            AbstractBuild<?, ?> abstractBuild = (AbstractBuild<?, ?>) run;
            final Updater updater = new Updater(abstractBuild.getParent().getScm(), getKeepNotePrivate(),
                    getRecordChangelog(), getVersion());
            updater.perform(run, listener);
        } else {
            final Updater updater = new Updater(scm, getKeepNotePrivate(), getRecordChangelog(), getVersion());
            updater.perform(run, listener);
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            super(MantisIssueUpdater.class);
        }

        @Override
        public String getDisplayName() {
            return Messages.MantisIssueUpdater_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/mantis/help.html";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public Publisher newInstance(final StaplerRequest req, final JSONObject formData) {
            return req.bindJSON(MantisIssueUpdater.class, formData);
        }
    }
}
