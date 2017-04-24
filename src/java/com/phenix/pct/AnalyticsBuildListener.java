package com.phenix.pct;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Task;

public class AnalyticsBuildListener implements BuildListener {
    private static final Map<String, Integer> taskCount = new HashMap<>();

    private boolean isPCTTask(Task task) {
        if (task.getTaskType().toLowerCase().startsWith("pct"))
            return true;
        if ("DlcVersion".equalsIgnoreCase(task.getTaskType())
                || "RestGen".equalsIgnoreCase(task.getTaskType())
                || "ClassDocumentation".equalsIgnoreCase(task.getTaskType())
                || "ABLUnit".equalsIgnoreCase(task.getTaskType()))
            return true;
        return false;
    }

    @Override
    public void taskFinished(BuildEvent event) {
        Task task = event.getTask();
        if (task == null) {
            return;
        }
        String taskName = task.getTaskType();
        if (taskName == null) {
            return;
        }
        if (isPCTTask(task)) {
            Integer i = taskCount.get(taskName.toLowerCase());
            if (i == null)
                taskCount.put(taskName.toLowerCase(), 1);
            else
                taskCount.put(taskName.toLowerCase(), i + 1);
        }
    }

    @Override
    public void buildFinished(BuildEvent event) {
        StringBuilder sb = new StringBuilder("pct version=\"");
        sb.append(ResourceBundle.getBundle(Version.BUNDLE_NAME).getString("PCTVersion")).append('"');
        for (Entry<String, Integer> entry : taskCount.entrySet()) {
            sb.append(',').append(entry.getKey() + "=" + entry.getValue());
        }

        try {
            final URL url = new URL("http://sonar-analytics.rssw.eu/write?db=sonar");
            HttpURLConnection connx = (HttpURLConnection) url.openConnection();
            connx.setRequestMethod("POST");
            connx.setConnectTimeout(2000);
            connx.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connx.getOutputStream());
            wr.writeBytes(sb.toString());
            wr.flush();
            wr.close();
            connx.getResponseCode();
        } catch (IOException uncaught) {
            // No-op
        }
    }

    @Override
    public void buildStarted(BuildEvent event) {
        // No-op
    }

    @Override
    public void targetStarted(BuildEvent event) {
        // No-op
    }

    @Override
    public void targetFinished(BuildEvent event) {
        // No-op
    }

    @Override
    public void taskStarted(BuildEvent event) {
        // No-op
    }

    @Override
    public void messageLogged(BuildEvent event) {
        // No-op
    }

}