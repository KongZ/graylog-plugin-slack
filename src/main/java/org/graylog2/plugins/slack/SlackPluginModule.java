package org.graylog2.plugins.slack;

import org.graylog2.plugin.PluginModule;
import org.graylog2.plugins.slack.callback.SlackAlarmCallback;
import org.graylog2.plugins.slack.output.SlackMessageOutput;
import org.graylog2.plugins.slack.rest.SlackActionCallback;

public class SlackPluginModule extends PluginModule {
  @Override
  protected void configure() {
    addAlarmCallback(SlackAlarmCallback.class);
    addMessageOutput(SlackMessageOutput.class);
    addRestResource(SlackActionCallback.class);
  }
}
