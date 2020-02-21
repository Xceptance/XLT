package com.xceptance.xlt.mastercontroller;

import com.xceptance.xlt.util.FailedAgentControllerCollection;

/**
 * The MasterControllerUI is the API the master controller uses to communicate progress and error information to the
 * user interface.
 */
public interface MasterControllerUI
{
    /**
     * Called after uploading agent files to the given agent controller.
     */
    public void agentFilesUploaded();

    /**
     * Called after starting the agent at the given agent controller.
     */
    public void agentsStarted();

    /**
     * Called after requesting the agent status from the given agent controller. If the operation was successful, the
     * passed results collection is empty. Otherwise, it contains information about which agent controller has failed as
     * well as why it has failed.
     * 
     * @param results
     *            the failed agent controllers
     */
    public void agentStatusReceived(FailedAgentControllerCollection results);

    /**
     * Called after stopping the agent at the given agent controller.
     */
    public void agentsStopped();

    /**
     * Called before downloading test results from the given agent controller.
     * 
     * @param agentController
     *            the respective agent controller
     */
    public void downloadingTestResults();

    /**
     * Called before requesting the agent status from the given agent controller.
     * 
     * @param agentController
     *            the respective agent controller
     */
    public void receivingAgentStatus();

    /**
     * Called before starting the agents.
     */
    public void startingAgents();

    /**
     * Called before stopping the agent at the given agent controller.
     * 
     * @param agentController
     *            the respective agent controller
     */
    public void stoppingAgents();

    /**
     * Called after downloading test results from the given agent controller. If the operation was successful, the
     * passed results collection is empty. Otherwise, it contains information about which agent controller has failed as
     * well as why it has failed.
     * 
     * @param results
     *            the failed agent controllers
     */
    public void testResultsDownloaded(FailedAgentControllerCollection results);

    /**
     * Called before uploading agent files.
     */
    public void uploadingAgentFiles();

    /**
     * Called if the master can not connect to a agent controller. Introduces skipped agent controllers.
     * 
     * @param unconnectedAgentControllers
     *            the agent controllers that could not be connected to
     */
    public void skipAgentControllerConnections(FailedAgentControllerCollection unconnectedAgentControllers);

    /**
     * Returns the number of seconds to wait before the status list is updated again.
     * 
     * @return the update interval
     */
    public int getStatusListUpdateInterval();
}
